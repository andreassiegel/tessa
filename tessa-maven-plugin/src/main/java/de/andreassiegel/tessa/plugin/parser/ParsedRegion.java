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
import com.github.javaparser.ast.comments.LineComment;
import java.util.List;
import lombok.Getter;

/**
 * The abstract abstraction of some region in parsed code.
 *
 * <p>It is defined by some range, marked using begin and end line numbers in the source file, and a
 * list of nodes in that range.
 *
 * <p>The abstraction provides some commonly used functionality.
 *
 * @param <T> the type of relevant nodes within the region
 */
@Getter
abstract class ParsedRegion<T> {

  protected final String name;
  protected final LineComment begin;
  protected final LineComment end;
  protected final List<Node> childNodes;

  protected final List<T> relevantChildren;

  /**
   * Instantiates a new parsed region.
   *
   * @param name the name of the region
   * @param begin the begin region line comment
   * @param end the end region line comment
   * @param childNodes the nodes in the region
   */
  ParsedRegion(String name, LineComment begin, LineComment end, List<Node> childNodes) {
    this.name = name;
    this.begin = begin;
    this.end = end;
    this.childNodes = childNodes;
    this.relevantChildren = initializeRelevantChildren(childNodes);
  }

  /**
   * Instantiates a new parsed region without line numbers.
   *
   * <p>This constructor should only be used in exceptional cases, e.g., as a fallback when there
   * are no actual regions, and the instance is only used to group child nodes.
   *
   * @param name the name of the region
   * @param childNodes the nodes in the region
   */
  ParsedRegion(String name, List<Node> childNodes) {
    this(name, null, null, childNodes);
  }

  /**
   * Returns the begin line number that is retrieved from the begin region comment.
   *
   * @return the line number
   */
  Integer getBeginLine() {
    return lineNumber(begin);
  }

  /**
   * Returns the end line number that is retrieved from the endregion comment.
   *
   * @return the line number
   */
  Integer getEndLine() {
    return lineNumber(end);
  }

  /**
   * Boolean flag indicating whether the region includes any relevant child nodes.
   *
   * <p>What "relevant" means is determined by initialization of relevant children, see {@link
   * #initializeRelevantChildren(List)}.
   *
   * @return {@code true} if there are relevant children, {@code false} otherwise
   */
  boolean isNotEmpty() {
    return !relevantChildren.isEmpty();
  }

  /**
   * Converts a line comment into its line number.
   *
   * @param comment the line comment
   * @return the line number
   */
  private Integer lineNumber(LineComment comment) {
    return comment != null ? comment.getBegin().map(p -> p.line).orElse(null) : null;
  }

  /**
   * Initializes the relevant children from the list of child nodes.
   *
   * <p>The implementation of this abstract method defines what the concrete instance of a parsed
   * region is about.
   *
   * <p>Implementations of this method could also pre-process the child nodes and covert them to
   * whatever type is relevant for the processing of the region.
   *
   * @param childNodes the child nodes
   * @return the filtered and potentially converted list of child nodes.
   */
  abstract List<T> initializeRelevantChildren(List<Node> childNodes);
}
