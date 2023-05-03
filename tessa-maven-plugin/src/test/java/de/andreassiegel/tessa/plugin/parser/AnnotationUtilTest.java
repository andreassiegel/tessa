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

import static de.andreassiegel.tessa.plugin.parser.AnnotationUtil.annotationValue;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class AnnotationUtilTest {

  // region annotationValue()

  @ParameterizedTest
  @CsvSource({
    "testMethodDisplayName1, DisplayName, This is display name without explicit value attribute.",
    "testMethodDisplayName2, DisplayName, This is display name in the value attribute."
  })
  void annotationValue_withAnnotatedMethods_returnsStringFromAnnotation(
      String methodName, String annotationName, String expectedDisplayName) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    MethodDeclaration methodDeclaration =
        new ParsedTestFile(path)
                .getTestClass("AnnotationTest")
                .map(ParsedTestClass::getClassDeclaration)
                .map(d -> d.getMethodsByName(methodName))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .get();

    // Act
    String displayName = annotationValue(methodDeclaration, annotationName);

    // Assert
    assertEquals(expectedDisplayName, displayName);
  }

  @ParameterizedTest
  @CsvSource({
    "testMethod, DisplayName",
    "testMethodDisplayName3, DisplayName",
    "testMethodDisplayName4, DisplayName",
    "testMethodDisplayName5, DisplayName"
  })
  void annotationValue_withInvalidMethods_returnsEmptyString(
      String methodName, String annotationName) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    MethodDeclaration methodDeclaration =
        new ParsedTestFile(path)
                .getTestClass("AnnotationTest")
                .map(ParsedTestClass::getClassDeclaration)
                .map(d -> d.getMethodsByName(methodName))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .get();

    // Act
    String displayName = annotationValue(methodDeclaration, annotationName);

    // Assert
    assertNotNull(displayName);
    assertEquals("", displayName);
  }

  @Test
  void annotationValue_withNull_returnsEmptyString() {
    // Act
    String displayName = annotationValue(null, "DisplayName");

    // Assert
    assertNotNull(displayName);
    assertEquals("", displayName);
  }

  @ParameterizedTest
  @MethodSource("annotationExpressionData")
  void annotationValue_withAnnotationExpression_returnsAnnotationValue(
      AnnotationExpr annotationExpression, String expectedValue) {
    // Act
    String value = annotationValue(annotationExpression);

    // Assert
    assertNotNull(value);
    assertEquals(expectedValue, value);
  }

  static Stream<Arguments> annotationExpressionData() {
    Function<String, AnnotationExpr> normalAnnotationExpression =
        v ->
            new NormalAnnotationExpr(
                new Name("TestAnnotation"),
                new NodeList<>(List.of(new MemberValuePair("value", new StringLiteralExpr(v)))));

    Function<String, AnnotationExpr> singleMemberAnnotationExpr =
        v -> new SingleMemberAnnotationExpr(new Name("TestAnnotation"), new StringLiteralExpr(v));

    return Stream.of(
        Arguments.of(normalAnnotationExpression.apply("My value"), "My value"),
        Arguments.of(
            singleMemberAnnotationExpr.apply("This is right in the annotation"),
            "This is right in the annotation"));
  }

  // endregion
}
