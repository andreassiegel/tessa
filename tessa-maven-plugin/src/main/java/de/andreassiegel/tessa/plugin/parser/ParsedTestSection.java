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
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import de.andreassiegel.tessa.plugin.model.TestStep;
import java.util.List;

/**
 * Concrete representation of a parsed region containing comments that document the subsection of a
 * test.
 */
class ParsedTestSection extends ParsedRegion<Comment> {

  ParsedTestSection(String name, List<Node> childNodes) {
    this(name, null, null, childNodes);
  }

  ParsedTestSection(String name, LineComment begin, LineComment end, List<Node> childNodes) {
    super(name, begin, end, childNodes);
  }

  /**
   * Filters comments from the provided child nodes.
   *
   * @param childNodes the child nodes
   * @return the list of comments
   */
  @Override
  List<Comment> initializeRelevantChildren(List<Node> childNodes) {
    return childNodes.stream()
        .filter(node -> node instanceof Comment)
        .map(node -> (Comment) node)
        .toList();
  }

  /**
   * Converts and returns the comments as test steps.
   *
   * <p>The method extracts the content of the comment, regardless of comment types, and removes
   * indentation.
   *
   * @return the test steps
   */
  List<TestStep> getTestSteps() {
    return relevantChildren.stream()
        .map(Comment::getContent)
        .map(content -> content.replaceAll("(?m)^[ \\t]+", "")) // remove indentation
        .map(content -> TestStep.builder().info(content).build())
        .toList();
  }
}
