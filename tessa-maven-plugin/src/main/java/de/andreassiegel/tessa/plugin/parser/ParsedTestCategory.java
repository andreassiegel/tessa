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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import de.andreassiegel.tessa.plugin.model.TestCase;
import java.util.List;
import lombok.Getter;

/**
 * Concrete representation of a parsed region of a test class containing test methods.
 *
 * @see ParsedTestMethod
 */
@Getter
class ParsedTestCategory extends ParsedRegion<ParsedTestMethod> {

  ParsedTestCategory(String name, List<Node> childNodes) {
    this(name, null, null, childNodes);
  }

  ParsedTestCategory(String name, LineComment begin, LineComment end, List<Node> childNodes) {
    super(name, begin, end, childNodes);
  }

  /**
   * Filters test methods from the provided child nodes and converts them to {@link
   * ParsedTestMethod} instances.
   *
   * @param childNodes the child nodes
   * @return the list of parsed methods
   */
  @Override
  List<ParsedTestMethod> initializeRelevantChildren(List<Node> childNodes) {
    return childNodes.stream()
        .filter(node -> node instanceof MethodDeclaration)
        .map(node -> (MethodDeclaration) node)
        .filter(MethodDeclarationUtil::isTestMethod)
        .map(ParsedTestMethod::new)
        .toList();
  }

  /**
   * Converts and returns the parsed methods as test cases.
   *
   * @return the test cases
   */
  List<TestCase> getTestCases() {
    return relevantChildren.stream().map(ParsedTestMethod::toTestCase).toList();
  }
}
