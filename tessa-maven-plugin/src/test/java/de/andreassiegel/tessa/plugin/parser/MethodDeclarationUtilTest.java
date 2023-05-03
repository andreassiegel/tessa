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

import static de.andreassiegel.tessa.plugin.parser.MethodDeclarationUtil.isTestMethod;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MethodDeclarationUtilTest {

  // isTestMethod()

  @ParameterizedTest
  @ValueSource(strings = {"Test", "ParameterizedTest"})
  void isTestMethod_withTestAnnotation_returnsTrue(String annotationName) {
    // Arrange
    MethodDeclaration methodDeclaration = new MethodDeclaration();
    methodDeclaration.addAnnotation(annotationName);

    // Act
    var result = isTestMethod(methodDeclaration);

    // Assert
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DisplayName", "Order"})
  void isTestMethod_withoutTestAnnotation_returnsFalse(String annotationName) {
    // Arrange
    MethodDeclaration methodDeclaration = new MethodDeclaration();
    methodDeclaration.addAnnotation(annotationName);

    // Act
    var result = isTestMethod(methodDeclaration);

    // Assert
    assertFalse(result);
  }

  @Test
  void isTestMethod_withoutAnnotation_returnsFalse() {
    // Arrange
    MethodDeclaration methodDeclaration = new MethodDeclaration();

    // Act
    var result = isTestMethod(methodDeclaration);

    // Assert
    assertFalse(result);
  }

  @Test
  void isTestMethod_withNullMethodDeclaration_returnsFalse() {
    // Act
    var result = isTestMethod(null);

    // Assert
    assertFalse(result);
  }

  // endregion
}
