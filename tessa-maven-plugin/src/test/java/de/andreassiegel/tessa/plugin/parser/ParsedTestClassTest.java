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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import de.andreassiegel.tessa.plugin.model.TestSet;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParsedTestClassTest {

  static final Path BASE_PATH = Paths.get("");

  // region streamAnnotatedMethods()

  @ParameterizedTest
  @CsvSource({
    "BeforeAll, setup, 1",
    "AfterAll, cleanup, 1",
    "Test, testMethod, 6",
    "ParameterizedTest, testMethodWithParameters, 1"
  })
  void streamAnnotatedMethods_withValidAnnotationName_returnsMethodStream(
      String annotationName, String methodName, Integer expectedSize) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("AnnotationTest").get();

    // Act
    List<String> methodNames =
        parsedClass
            .streamAnnotatedMethods(annotationName)
            .map(NodeWithSimpleName::getNameAsString)
            .toList();

    // Assert
    assertNotNull(methodNames);
    assertEquals(expectedSize, methodNames.size());
    assertTrue(methodNames.contains(methodName));
  }

  @Test
  void streamAnnotatedMethods_withAbsentAnnotation_returnsEmptyStream() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("AnnotationTest").get();

    // Act
    List<MethodDeclaration> methods = parsedClass.streamAnnotatedMethods("Order").toList();

    // Assert
    assertNotNull(methods);
    assertTrue(methods.isEmpty());
  }

  // endregion

  // region initializeTestCategories()

  @ParameterizedTest
  @CsvSource({
    "SimpleRegionTest, 1",
    "MultiRegionTest, 2",
    "NestedRegionTest, 4",
    "NestedMethodRegionTest, 1"
  })
  void initializeTestCategories_withValidClass_returnsExpectedNumberOfCategories(
      String className, Integer expectedCategoryCount) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/RegionTest.java");
    ParsedTestClass parsedClass = new ParsedTestFile(path, BASE_PATH).getTestClass(className).get();

    // Act
    Map<String, ParsedTestCategory> testCategories = parsedClass.initializeTestCategories();

    // Assert
    assertNotNull(testCategories);
    assertEquals(expectedCategoryCount, testCategories.keySet().size());
  }

  @ParameterizedTest
  @MethodSource("categoryBoundaryData")
  void initializeTestCategories_withValidClass_returnsCorrectCategoryBoundaries(
      String className, Map<String, Map<String, Integer>> expectedCategories) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/RegionTest.java");
    ParsedTestClass parsedClass = new ParsedTestFile(path, BASE_PATH).getTestClass(className).get();

    // Act
    Map<String, Map<String, Integer>> categories =
        parsedClass.initializeTestCategories().values().stream()
            .collect(
                Collectors.toMap(
                    ParsedTestCategory::getName,
                    r -> Map.of("begin", r.getBeginLine(), "end", r.getEndLine())));

    // Assert
    assertNotNull(categories.values());
    assertEquals(expectedCategories.keySet(), categories.keySet());
    categories.forEach(
        (key, value) -> {
          assertEquals(expectedCategories.get(key), value);
        });
  }

  static Stream<Arguments> categoryBoundaryData() {
    return Stream.of(
        Arguments.of(
            "SimpleRegionTest",
            Map.of("First Region", Map.ofEntries(Map.entry("begin", 5), Map.entry("end", 9)))),
        Arguments.of(
            "MultiRegionTest",
            Map.of(
                "First Region",
                Map.ofEntries(Map.entry("begin", 14), Map.entry("end", 18)),
                "Second Region",
                Map.ofEntries(Map.entry("begin", 20), Map.entry("end", 24)))),
        Arguments.of(
            "NestedRegionTest",
            Map.of(
                "First Region",
                Map.ofEntries(Map.entry("begin", 30), Map.entry("end", 44)),
                "Nested Region 1",
                Map.ofEntries(Map.entry("begin", 32), Map.entry("end", 36)),
                "Nested Region 2",
                Map.ofEntries(Map.entry("begin", 38), Map.entry("end", 42)),
                "Second Region",
                Map.ofEntries(Map.entry("begin", 46), Map.entry("end", 50)))),
        Arguments.of(
            "NestedMethodRegionTest",
            Map.of("Class Region", Map.ofEntries(Map.entry("begin", 55), Map.entry("end", 65)))));
  }

  @ParameterizedTest
  @MethodSource("categoryMemberData")
  void initializeTestCategories_withValidClass_returnsCorrectCategoryMembers(
      String className, Map<String, List<String>> expectedCategories) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/RegionTest.java");
    ParsedTestClass parsedClass = new ParsedTestFile(path, BASE_PATH).getTestClass(className).get();

    // Act
    Map<String, List<String>> categories =
        parsedClass.initializeTestCategories().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    entry ->
                        entry.getValue().getChildNodes().stream()
                            .map(Node::getTokenRange)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(TokenRange::toString)
                            .toList()));

    // Assert
    assertNotNull(categories.values());
    assertEquals(expectedCategories.keySet(), categories.keySet());
    categories.forEach(
        (key, value) -> {
          assertEquals(expectedCategories.get(key).size(), value.size());
          assertEquals(expectedCategories.get(key), value);
        });
  }

  static Stream<Arguments> categoryMemberData() {
    return Stream.of(
        Arguments.of(
            "SimpleRegionTest", Map.of("First Region", List.of("void firstRegionMethod() {}"))),
        Arguments.of(
            "MultiRegionTest",
            Map.of(
                "First Region",
                List.of("void firstRegionMethod() {}"),
                "Second Region",
                List.of("void secondRegionMethod() {}"))),
        Arguments.of(
            "NestedRegionTest",
            Map.of(
                "First Region",
                List.of(
                    "// region Nested Region 1",
                    "void firstRegionMethod1() {}",
                    "// endregion",
                    "// region Nested Region 2",
                    "void firstRegionMethod2() {}",
                    "// endregion"),
                "Nested Region 1",
                List.of("void firstRegionMethod1() {}"),
                "Nested Region 2",
                List.of("void firstRegionMethod2() {}"),
                "Second Region",
                List.of("void secondRegionMethod() {}"))),
        Arguments.of(
            "NestedMethodRegionTest",
            Map.of(
                "Class Region",
                List.of(
                    """
                    void method() {
                        // region Method Region

                        var foo = "bar";

                        // endregion
                      }"""))));
  }

  // endregion

  // region hasCategories()

  @Test
  void hasCategories_withValidClassWithRegions_returnsTrue() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("SampleTest").get();

    // Act
    boolean result = parsedClass.hasCategories();

    // Assert
    assertTrue(result);
  }

  @Test
  void hasCategories_withValidClassWithoutRegions_returnsFalse() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("SampleNoRegionTest").get();

    // Act
    boolean result = parsedClass.hasCategories();

    // Assert
    assertFalse(result);
  }

  // endregion

  // region toDocumentDataModel()

  @Test
  void toDocumentDataModel_withValidClassWithRegions_returnsExpectedDataModel() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("SampleTest").get();

    // Act
    TestSet dataModel = parsedClass.toDocumentDataModel(path, BASE_PATH);

    // Assert
    assertNotNull(dataModel);
    assertEquals(path.toString(), dataModel.getPath());
    assertEquals("SampleTest", dataModel.getClassName());
    assertEquals(
        "MIT 00 - Example Test for new Module Test Implementation Structure", dataModel.getTitle());
    assertEquals(
        """
        This test implementation is intended as a proof of concept to get a better idea of how the module
        test implementation using the new concept could look like.""",
        dataModel.getDescription());
    assertEquals(
        """
        This is something that needs to be done as test setup:
        <ul>
          <li>Foo</li>
          <li>Bar</li>
          <li>Bla</li>
          <li>Blubb</li>
        </ul>""",
        dataModel.getSetup());
    assertEquals(
        """
        These are the necessary cleanup steps:
        <ul>
          <li>Remove this</li>
          <li>Reset that</li>
        </ul>""",
        dataModel.getCleanup());
    assertEquals(
        "This is the first block comment in the test class.", dataModel.getFurtherInformation());
    assertTrue(dataModel.isCategorized());
    assertNotNull(dataModel.getTestCases());
    assertEquals(2, dataModel.getTestCases().keySet().size());

    var happyTestCases = dataModel.getTestCases().get("Happy Cases");
    assertNotNull(happyTestCases);
    assertEquals(2, happyTestCases.size());
    assertEquals("Parameterized Test Case 1", happyTestCases.get(0).getTitle());
    assertEquals("Normal Test Case 2", happyTestCases.get(1).getTitle());
  }

  @Test
  void toDocumentDataModel_withValidClassWithoutRegions_returnsExpectedDataModel()
      throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass("SampleNoRegionTest").get();

    // Act
    TestSet dataModel = parsedClass.toDocumentDataModel(path, BASE_PATH);

    // Assert
    assertNotNull(dataModel);
    assertEquals(path.toString(), dataModel.getPath());
    assertEquals("SampleNoRegionTest", dataModel.getClassName());
    assertEquals(
        "MIT 00 - Example Test for new Module Test Implementation Structure", dataModel.getTitle());
    assertEquals(
        """
        This test implementation is intended as a proof of concept to get a better idea of how the module
        test implementation using the new concept could look like.""",
        dataModel.getDescription());
    assertEquals(
        """
        This is something that needs to be done as test setup:
        <ul>
          <li>Foo</li>
          <li>Bar</li>
          <li>Bla</li>
          <li>Blubb</li>
        </ul>""",
        dataModel.getSetup());
    assertEquals(
        """
        These are the necessary cleanup steps:
        <ul>
          <li>Remove this</li>
          <li>Reset that</li>
        </ul>""",
        dataModel.getCleanup());
    assertEquals(
        "This is the first block comment in the test class.", dataModel.getFurtherInformation());
    assertFalse(dataModel.isCategorized());
    assertNotNull(dataModel.getTestCases());
    assertEquals(1, dataModel.getTestCases().keySet().size());

    var happyTestCases = dataModel.getTestCases().get("Default");
    assertNotNull(happyTestCases);
    assertEquals(2, happyTestCases.size());
    assertEquals("Parameterized Test Case 1", happyTestCases.get(0).getTitle());
    assertEquals("Normal Test Case 2", happyTestCases.get(1).getTitle());
  }

  // endregion

  // region hasTestMethods()

  @ParameterizedTest
  @ValueSource(strings= {"TestAnnotationTest", "ParameterizedTestAnnotationTest"})
  void hasTestMethods_withValidAnnotations_returnsTrue(String className)
      throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass(className).get();

    // Act
    var result = parsedClass.hasTestMethods();

    // Assert
    assertTrue(result);
  }

  @Test
  void hasTestMethods_withoutValidAnnotations_returnsFalse()
      throws IOException {
    // Arrange
    var className = "NoAnnotationTest";
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    ParsedTestFile parsedTestFile = new ParsedTestFile(path, BASE_PATH);
    ParsedTestClass parsedClass = parsedTestFile.getTestClass(className).get();

    // Act
    var result = parsedClass.hasTestMethods();

    // Assert
    assertFalse(result);
  }

  // endregion
}
