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
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Utilities for handling comments in the code, and for retrieving information from them. */
class CommentUtil {

  private static final String BEGIN_REGION_REGEX = "^region\\s+";
  private static final String END_REGION_REGEX = "^endregion";

  private CommentUtil() {}

  /**
   * Retrieves the Javadoc comment of the node. Non-Javadoc comments are not taken into account.
   *
   * <p>The output only includes the main description from the Javadoc, i.e., documentation of
   * parameters, return values, throws declarations, etc. are omitted in the returned result.
   *
   * @param node the node
   * @return the text of the Javadoc comment, if any, and an empty string otherwise.
   */
  static String javadocComment(Node node) {
    if (node == null) {
      return "";
    }

    return node.getComment()
        .filter(Comment::isJavadocComment)
        .map(Comment::asJavadocComment)
        .map(JavadocComment::parse)
        .map(Javadoc::getDescription)
        .map(JavadocDescription::toText)
        .map(String::trim)
        .orElse("");
  }

  /**
   * Retrieves the first block comment from a node.
   *
   * <p>Leading whitespaces are removed for each line of the block comment, in order to remove the
   * indentation of the comment. Linebreaks, however, are left intact.
   *
   * @param node the node
   * @return the comment or an empty string if no block comment is found.
   */
  static String firstBlockComment(Node node) {
    if (node == null) {
      return "";
    }

    if (node instanceof MethodDeclaration methodDeclaration) {
      node = methodDeclaration.getBody().orElse(null);
    }

    return Optional.ofNullable(node)
        .map(Node::getChildNodes)
        .orElse(Collections.emptyList())
        .stream()
        .filter(n -> n instanceof BlockComment)
        .map(n -> (BlockComment) n)
        .findFirst()
        .map(Comment::getContent)
        .map(s -> s.replaceAll("(?m)^[ \\t]+", "")) // remove indentation
        .map(String::trim)
        .orElse("");
  }

  /**
   * Filters the provided list of comments to return only line comments that either mark the begin
   * or end of a region in the code.
   *
   * @param comments the list of comments
   * @return the filtered list of comments
   * @throws RuntimeException if the region comments are invalid, i.e., there is no {@code
   *     endregion} line comment for each {@code region} comment, or vice-versa.
   */
  static List<LineComment> regionLineComments(List<Comment> comments) {
    var regionComments =
        comments.stream()
            .filter(Comment::isLineComment)
            .map(Comment::asLineComment)
            .filter(CommentUtil::isRegionComment)
            .collect(Collectors.toList());

    var regionCommentCount = regionComments.size();
    if (regionCommentCount > 0 && regionCommentCount % 2 != 0) {
      throw new RuntimeException("Invalid regions: region and endregion do not match");
    }

    return regionComments;
  }

  /**
   * Finds the begin comment of a specific named region in a list of line comments.
   *
   * @param comments the list of line comments
   * @param regionName the name of the region
   * @return the index in the list, or {@code -1} if there is no region begin comment for the
   *     desired name in the list (or it is {@code null} or empty.
   */
  static int findNamedRegionBeginIndex(List<LineComment> comments, String regionName) {
    if (comments == null
        || comments.isEmpty()
        || regionName == null
        || regionName.isEmpty()
        || regionName.isBlank()) {
      return -1;
    }

    for (int i = 0; i < comments.size(); i++) {
      var comment = comments.get(i);
      if (isBeginRegionComment(comment) && regionName(comment).equals(regionName.trim())) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Finds the index of the last region begin comment in a list of line comments.
   *
   * @param comments the list of line comments
   * @return the index in the list, or {@code -1} if there is no region begin comment in the list
   *     (or it is {@code null} or empty.
   */
  static int findLastRegionBeginIndex(List<LineComment> comments) {
    if (comments == null || comments.isEmpty()) {
      return -1;
    }

    int lastBeginIndex = -1;
    for (int i = comments.size() - 1; i >= 0; i--) {
      if (isBeginRegionComment(comments.get(i))) {
        lastBeginIndex = i;
        break;
      }
    }

    return lastBeginIndex;
  }

  /**
   * Checks whether a line comment marks either the begin or the end of a region.
   *
   * @param lineComment the line comment
   * @return {@code true} if the line comment marks the boundary of a region, {@code false}
   *     otherwise (or if the comment is {@code null}).
   */
  static boolean isRegionComment(LineComment lineComment) {
    return lineComment != null
        && (isBeginRegionComment(lineComment)
            || lineComment.getContent().trim().matches(END_REGION_REGEX));
  }

  /**
   * Checks whether a line comment marks the begin of a region. It also must include a region name,
   * i.e., the comment must not end with just the {@code region} keyword and whitespaces.
   *
   * @param lineComment the line comment
   * @return {@code true} if the comment is the begin of a region, {@code false} otherwise (or if
   *     the comment is {@code null}).
   */
  static boolean isBeginRegionComment(LineComment lineComment) {
    return lineComment != null
        && lineComment.getContent().trim().matches(BEGIN_REGION_REGEX + ".*");
  }

  /**
   * Extracts the name of the regin from a line comment.
   *
   * @param lineComment the line comment
   * @return the name of the region, or an empty string if no name can be found in the comment.
   */
  static String regionName(LineComment lineComment) {
    if (!isBeginRegionComment(lineComment)) {
      return "";
    }

    return lineComment.getContent().trim().replaceFirst(BEGIN_REGION_REGEX, "");
  }
}
