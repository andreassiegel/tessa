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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParsedTestFileTest {

  static final Path BASE_PATH = Paths.get("src/test");

  static final String TEST_CLASS_NAME = "SampleTest";
  static final Path SAMPLE_TEST_PATH =
      Paths.get("src/test/resources/com/example/test/SampleTest.java");

  // region Constructor

  @Test
  void constructor_canReadFile() {
    try {
      new ParsedTestFile(SAMPLE_TEST_PATH, BASE_PATH);
    } catch (Throwable throwable) {
      fail("Constructor threw unexpected exception: " + throwable);
    }
  }

  // endregion

  // region getTestClassNames()

  @Test
  void getTestClassNames_findsSampleTest() throws IOException {
    // Arrange
    ParsedTestFile parsedTestFile = new ParsedTestFile(SAMPLE_TEST_PATH, BASE_PATH);

    // Act
    Collection<String> classNames = parsedTestFile.getTestClassNames();

    // Assert
    assertNotNull(classNames);
    assertTrue(classNames.size() >= 1);
    assertTrue(classNames.contains(TEST_CLASS_NAME));
  }

  @Test
  void getTestClassNames_ignoresSampleInterface() throws IOException {
    // Arrange
    ParsedTestFile parsedTestFile = new ParsedTestFile(SAMPLE_TEST_PATH, BASE_PATH);

    // Act
    Collection<String> classNames = parsedTestFile.getTestClassNames();

    // Assert
    assertNotNull(classNames);
    assertFalse(classNames.contains("SampleInterface"));
  }

  // endregion

  // region getTestClass

  @Test
  void getTestClass_withValidClassName_returnsClassDeclaration() throws IOException {
    // Arrange
    ParsedTestFile parsedTestFile = new ParsedTestFile(SAMPLE_TEST_PATH, BASE_PATH);

    // Act
    Optional<ParsedTestClass> classDeclaration = parsedTestFile.getTestClass(TEST_CLASS_NAME);

    // Assert
    assertNotNull(classDeclaration);
    assertTrue(classDeclaration.isPresent());
    assertEquals(TEST_CLASS_NAME, classDeclaration.get().getClassName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "SampleInterface", "DoesNotExist", " "})
  @NullSource
  void getTestClass_withInvalidClassName_returnsEmpty(String className) throws IOException {
    // Arrange
    ParsedTestFile parsedTestFile = new ParsedTestFile(SAMPLE_TEST_PATH, BASE_PATH);

    // Act
    Optional<ParsedTestClass> classDeclaration = parsedTestFile.getTestClass(className);

    // Assert
    assertNotNull(classDeclaration);
    assertTrue(classDeclaration.isEmpty());
  }

  // endregion
}
