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
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/** Utilities for handling annotations in the code, and for retrieving information from them. */
class AnnotationUtil {

  private AnnotationUtil() {}

  /**
   * Retrieves the value of an annotation of an annotated node, i.e., a class or method..
   *
   * <p>The implementation takes different options for defining the annotation value into account:
   *
   * <ul>
   *   <li>the value is provided immediately, without the {@code value} attribute, e.g.:
   *       {@code @DisplayName("My name")}
   *   <li>the value is provided as the {@code value} attribute, e.g.: {@code @DisplayName(value =
   *       "My name")}
   * </ul>
   *
   * @param node the node
   * @param annotationName the name of the annotation
   * @return the value of the annotation, or an empty string if the annotation is missing or invalid
   */
  static <N extends Node> String annotationValue(
      NodeWithAnnotations<N> node, String annotationName) {
    if (node == null) {
      return "";
    }

    return node.getAnnotationByName(annotationName)
        .map(AnnotationExpr::asAnnotationExpr)
        .map(AnnotationUtil::annotationValue)
        .orElse("");
  }

  /**
   * Retrieves the value from an annotation. This can either be the value of the {@code value}
   * attribute or the only content of the annotation.
   *
   * @param annotationExpression the annotation expression
   * @return the value, or an empty string if the value is missing or invalid
   */
  static String annotationValue(AnnotationExpr annotationExpression) {
    var value = "\"\"";
    if (annotationExpression instanceof NormalAnnotationExpr normalAnnotationExpr) {
      value =
          normalAnnotationExpr.getPairs().stream()
              .filter(p -> "value".equals(p.getNameAsString()))
              .map(MemberValuePair::getValue)
              .map(Node::toString)
              .findFirst()
              .orElse(value);
    }

    if (annotationExpression instanceof SingleMemberAnnotationExpr singleMemberExpression) {
      value = singleMemberExpression.getMemberValue().toString();
    }

    return value.substring(1, value.length() - 1);
  }
}
