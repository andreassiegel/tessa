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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.nodeTypes.NodeWithMembers;
import de.andreassiegel.tessa.plugin.model.TestCase;
import de.andreassiegel.tessa.plugin.model.TestSet;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Representation of a parsed Java test class. It provides access to test method declarations within
 * the parsed class as well as retrieved information from the class.
 *
 * @see ParsedTestCategory
 */
@Getter(AccessLevel.PACKAGE)
public class ParsedTestClass {

  private static final String DEFAULT_CATEGORY = "Default";

  private final ClassOrInterfaceDeclaration classDeclaration;

  private final String className;
  private final String displayName;
  private final String status;
  private final String description;
  private final String furtherInformation;

  private final String setup;
  private final String cleanup;

  private final Map<String, ParsedTestCategory> categories;

  private final List<MethodDeclaration> testMethods;
  private final String disabledNote;

  /**
   * Creates an instance of a parsed test class based on the provided class declaration.
   *
   * <p>The constructor reads all relevant information from the class that is later on needed to
   * generate the test documentation for the test cases defined in the class.
   *
   * @param classDeclaration the class declaration
   */
  public ParsedTestClass(ClassOrInterfaceDeclaration classDeclaration) {
    this.classDeclaration = classDeclaration;

    this.className = classDeclaration.getNameAsString();
    this.displayName = AnnotationUtil.annotationValue(classDeclaration, "DisplayName");
    this.status = AnnotationUtil.annotationValue(classDeclaration, "Status");
    this.disabledNote = AnnotationUtil.annotationValue(classDeclaration, "Disabled");
    this.description = CommentUtil.javadocComment(classDeclaration);
    this.furtherInformation = CommentUtil.firstBlockComment(classDeclaration);
    this.setup = firstAnnotatedMethod("BeforeAll").map(CommentUtil::javadocComment).orElse("");
    this.cleanup = firstAnnotatedMethod("AfterAll").map(CommentUtil::javadocComment).orElse("");

    this.categories = initializeTestCategories();
    this.testMethods =
        classDeclaration.getMethods().stream().filter(MethodDeclarationUtil::isTestMethod).toList();
  }

  /**
   * Checks whether the test class includes any test methods/cases.
   *
   * @return {@code true} if the test class includes any tests, {@code false} otherwise
   */
  public boolean hasTestMethods() {
    return !testMethods.isEmpty();
  }

  /**
   * Retrieves a map of categories, i.e., sections in the Java file that are marked with {@code
   * //region My region} and {@code //endregion} comments.
   *
   * <p>Only regions within the class are taken into account. Regions inside methods are out of
   * scope for this method.
   *
   * @return the map of categories
   */
  Map<String, ParsedTestCategory> initializeTestCategories() {
    try {
      List<LineComment> comments =
          CommentUtil.regionLineComments(classDeclaration.getOrphanComments());
      if (comments.size() > 1) {
        return categorizedTestCases(comments);
      }
    } catch (Exception e) {
      // swallow the exception, we fall back to uncategorized test cases
    }
    return uncategorizedTestCases();
  }

  /**
   * Initializes the categories without actual categories, i.e., all child nodes from the class
   * declaration are put into the default category that is ignored when the parsed class is
   * converted to the data model.
   *
   * @return the map containing the default section
   */
  private Map<String, ParsedTestCategory> uncategorizedTestCases() {
    return Map.of(
        DEFAULT_CATEGORY,
        new ParsedTestCategory(DEFAULT_CATEGORY, classDeclaration.getChildNodes()));
  }

  /**
   * Initializes the categories using the region line comments.
   *
   * <p>The method first finds the last begin of a region (because it could be a nested region),
   * takes the next endregion comment and creates a category with all the nodes between these
   * comments.
   *
   * <p>The categories also get ordered by their begin in the code.
   *
   * @param comments the region comments
   * @return the map of categories
   */
  private Map<String, ParsedTestCategory> categorizedTestCases(List<LineComment> comments) {
    List<ParsedTestCategory> testCategories = new ArrayList<>();
    while (!comments.isEmpty()) {
      int lastBeginIndex = CommentUtil.findLastRegionBeginIndex(comments);

      LineComment begin = comments.get(lastBeginIndex);
      LineComment end = comments.get(lastBeginIndex + 1);

      comments.remove(lastBeginIndex + 1); // remove end
      comments.remove(lastBeginIndex); // remove end

      var name = CommentUtil.regionName(begin);
      var region =
          new ParsedTestCategory(
              name,
              begin,
              end,
              NodeUtil.childrenInRegion(classDeclaration.getChildNodes(), begin, end));
      testCategories.add(region);
    }

    Map<String, ParsedTestCategory> orderedTestCategories = new LinkedHashMap<>();
    testCategories.stream()
        .sorted(Comparator.comparingInt(ParsedRegion::getBeginLine))
        .forEach(s -> orderedTestCategories.put(s.getName(), s));

    return orderedTestCategories;
  }

  /**
   * Checks whether the tests in the class are organized in categories, i.e., there are regions in
   * the code that group the test methods.
   *
   * @return {@code true} if categories are used, {@code false} otherwise
   */
  boolean hasCategories() {
    return !categories.isEmpty()
        && !(categories.size() == 1 && categories.containsKey(DEFAULT_CATEGORY));
  }

  /**
   * Retrieves the first method declaration in the class that is annotated with the given
   * annotation.
   *
   * @param annotationName the name of the annotation
   * @return an {@link Optional} containing the matching method declaration, {@code
   *     Optional.empty()} otherwise
   */
  Optional<MethodDeclaration> firstAnnotatedMethod(String annotationName) {
    return streamAnnotatedMethods(annotationName).findFirst();
  }

  /**
   * Retrieves a stream of relevant method declarations from the class declaration, i.e., methods
   * that are annotated with the given annotation.
   *
   * @param annotationName the name of the annotation to filter for
   * @return a stream of method declarations
   */
  Stream<MethodDeclaration> streamAnnotatedMethods(String annotationName) {
    return Optional.ofNullable(classDeclaration).map(NodeWithMembers::getMethods).stream()
        .flatMap(Collection::stream)
        .filter(methodDeclaration -> methodDeclaration.isAnnotationPresent(annotationName));
  }

  /**
   * Converts a single class from the test file into a test documentation data model.
   *
   * @param filePath the path of the original test file
   * @return the test data model
   */
  TestSet toDocumentDataModel(Path filePath) {
    Map<String, List<TestCase>> testCases = new LinkedHashMap<>();
    categories.values().stream()
        .filter(ParsedTestCategory::isNotEmpty)
        .forEach(c -> testCases.put(c.getName(), c.getTestCases()));

    return TestSet.builder()
        .path(filePath)
        .className(className)
        .title(displayName)
        .status(status)
        .disabledNote(disabledNote)
        .description(description)
        .furtherInformation(getFurtherInformation())
        .setup(setup)
        .cleanup(cleanup)
        .testCases(testCases)
        .categorized(hasCategories())
        .build();
  }
}
