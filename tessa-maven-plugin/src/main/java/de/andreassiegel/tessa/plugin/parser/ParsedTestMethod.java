package de.andreassiegel.tessa.plugin.parser;

/*-
 * Copyright © 2023 Andreas Siegel (mail@andreassiegel.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static de.andreassiegel.tessa.plugin.parser.CommentUtil.findLastRegionBeginIndex;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.regionLineComments;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.regionName;
import static de.andreassiegel.tessa.plugin.parser.NodeUtil.childrenInRegion;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import de.andreassiegel.tessa.plugin.model.TestCase;
import de.andreassiegel.tessa.plugin.model.TestStep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a parsed Java test method. It provides access to nodes within the parsed method
 * as well as retrieved information from the method.
 *
 * @see ParsedTestSection
 */
public class ParsedTestMethod {

  private static final String DEFAULT_SECTION = "Default";

  private final MethodDeclaration methodDeclaration;
  private final String methodName;
  private final String displayName;
  private final String description;
  private final String status;
  private final String disabledNote;
  private final String furtherInformation;

  private final Map<String, ParsedTestSection> sections;

  /**
   * Creates an instance of a parsed test method based on the provided method declaration.
   *
   * <p>The constructor reads all relevant information from the method that is later on needed to
   * generate the test documentation for the test case defined in the method.
   *
   * @param methodDeclaration the method declaration
   */
  ParsedTestMethod(MethodDeclaration methodDeclaration) {
    this.methodDeclaration = methodDeclaration;

    this.methodName = methodDeclaration.getNameAsString();
    this.displayName = AnnotationUtil.annotationValue(methodDeclaration, "DisplayName");
    this.description = CommentUtil.javadocComment(methodDeclaration);
    this.status = AnnotationUtil.annotationValue(methodDeclaration, "Status");
    this.disabledNote = AnnotationUtil.annotationValue(methodDeclaration, "Disabled");
    this.furtherInformation = CommentUtil.firstBlockComment(methodDeclaration);

    this.sections = initializeSections();
  }

  /**
   * Retrieves a map of sections, i.e., sections in the method that are marked with {@code //region
   * My region} and {@code //endregion} comments.
   *
   * @return the map of categories
   */
  Map<String, ParsedTestSection> initializeSections() {
    if (methodDeclaration.getBody().isEmpty()) {
      return Collections.emptyMap();
    }

    BlockStmt methodBody = methodDeclaration.getBody().get();
    try {
      List<LineComment> comments = regionLineComments(methodBody.getOrphanComments());
      if (comments.size() > 1) {
        return categorizedChildren(methodBody, comments);
      }
    } catch (Exception e) {
      // swallow the exception, we fall back to uncategorized test cases
    }
    return uncategorizedChildren(methodBody);
  }

  /**
   * Initializes the sections without actual sections, i.e., all child nodes from the method body
   * are put into the default section that is ignored when the parsed method is converted to the
   * data model.
   *
   * @param methodBody the method body.
   * @return the map containing the default section
   */
  private Map<String, ParsedTestSection> uncategorizedChildren(BlockStmt methodBody) {
    return Map.of(
        DEFAULT_SECTION, new ParsedTestSection(DEFAULT_SECTION, methodBody.getChildNodes()));
  }

  /**
   * Initializes the sections using the region line comments.
   *
   * <p>The method first finds the last begin of a region (because it could be a nested region),
   * takes the next endregion comment and creates a section with all the nodes between these
   * comments.
   *
   * <p>The sections also get ordered by their begin in the code.
   *
   * @param methodBody the method body
   * @param comments the region comments
   * @return the map of sections
   */
  private Map<String, ParsedTestSection> categorizedChildren(
      BlockStmt methodBody, List<LineComment> comments) {
    List<Node> nodes = methodBody.getChildNodes();

    List<ParsedTestSection> sections = new ArrayList<>();
    while (!comments.isEmpty()) {
      int lastBeginIndex = findLastRegionBeginIndex(comments);

      LineComment begin = comments.get(lastBeginIndex);
      LineComment end = comments.get(lastBeginIndex + 1);

      comments.remove(lastBeginIndex + 1); // remove end
      comments.remove(lastBeginIndex); // remove end

      var name = regionName(begin);
      var section = new ParsedTestSection(name, begin, end, childrenInRegion(nodes, begin, end));
      sections.add(section);
    }

    Map<String, ParsedTestSection> orderedSections = new LinkedHashMap<>();
    sections.stream()
        .sorted(Comparator.comparingInt(ParsedRegion::getBeginLine))
        .forEach(s -> orderedSections.put(s.getName(), s));

    return orderedSections;
  }

  /**
   * Checks whether the steps in the method are organized in sections, i.e., there are regions in
   * the code that group the test steps.
   *
   * @return {@code true} if sections are used, {@code false} otherwise
   */
  boolean hasSections() {
    return !sections.isEmpty() && !(sections.size() == 1 && sections.containsKey(DEFAULT_SECTION));
  }

  /**
   * Converts a single method from the test file into a test documentation data model.
   *
   * @return the test data model
   */
  public TestCase toTestCase() {
    Map<String, List<TestStep>> steps = new LinkedHashMap<>();
    sections.values().stream()
        .filter(ParsedTestSection::isNotEmpty)
        .forEach(s -> steps.put(s.getName(), s.getTestSteps()));

    return TestCase.builder()
        .methodName(methodName)
        .beginLine(methodDeclaration.getBegin().get().line)
        .endLine(methodDeclaration.getEnd().get().line)
        .title(displayName)
        .description(description)
        .status(status)
        .disabledNote(disabledNote)
        .furtherInformation(furtherInformation)
        .sections(steps)
        .sectioned(hasSections())
        .build();
  }
}
