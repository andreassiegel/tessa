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

import de.andreassiegel.tessa.plugin.model.TestSet;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.codehaus.plexus.util.cli.Arg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  // region containsTests()

  @Test
  void containsTests_withoutClasses_returnsFalse() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/Empty.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.containsTests();

    // Assert
    assertFalse(result);
  }

  @Test
  void containsTests_withoutTests_returnsFalse() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/NoCommentTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.containsTests();

    // Assert
    assertFalse(result);
  }

  @Test
  void containsTests_withTestClasses_returnsTrue() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.containsTests();

    // Assert
    assertTrue(result);
  }

  // endregion

  // streamTestClasses()

  @Test
  void streamTestClasses_withoutClasses_returnsEmptyStream() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/Empty.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.streamTestClasses();

    // Assert
    assertNotNull(result);
    assertTrue(result.toList().isEmpty());
  }

  @ParameterizedTest
  @MethodSource("streamTestClassesData")
  void streamTestClasses_withClasses_returnsStream(String fileName, List<String> expectedClassNames) throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/" + fileName);
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.streamTestClasses();

    // Assert
    assertNotNull(result);
    assertEquals(expectedClassNames, result.map(ParsedTestClass::getClassName).toList());
  }

  static Stream<Arguments> streamTestClassesData() {
    return Stream.of(
      Arguments.of("SampleTest.java", List.of("SampleTest", "SampleNoRegionTest")),
      Arguments.of("NoCommentTest.java", List.of("LineCommentTest", "BlockCommentTest", "MethodTest", "NoCommentTest"))
    );
  }

  // endregion

  // region toDocumentDataModel()

  @Test
  void toDocumentDataModel_withEmptyFile_returnsEmptyList() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/Empty.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.toDocumentDataModel();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void toDocumentDataModel_withTestFile_returnsTestSetList() throws IOException {
    // Arrange
    Path path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);

    // Act
    var result = parsedTestFile.toDocumentDataModel();

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());

    List<String> testSetClassNames = result.stream().map(TestSet::getClassName).toList();
    assertTrue(testSetClassNames.contains("SampleTest"));
    assertTrue(testSetClassNames.contains("SampleNoRegionTest"));
  }

  // endregion
}
