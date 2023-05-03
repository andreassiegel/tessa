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

import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.List;
import java.util.Optional;

/** Utilities for handling methods in the code, and for retrieving information from them. */
class MethodDeclarationUtil {

  private static final List<String> TEST_ANNOTATION_NAMES = List.of("Test", "ParameterizedTest");

  private MethodDeclarationUtil() {}

  /**
   * Checks whether a method is a test method, i.e., it is annotated with either {@code @Test} or
   * {@code @ParameterizedTest}.
   *
   * @param methodDeclaration the method declaration
   * @return {@code true} if the method is a test method, {@code false} otherwise
   */
  static boolean isTestMethod(MethodDeclaration methodDeclaration) {
    if (methodDeclaration == null) {
      return false;
    }

    return TEST_ANNOTATION_NAMES.stream()
        .map(methodDeclaration::getAnnotationByName)
        .anyMatch(Optional::isPresent);
  }
}
