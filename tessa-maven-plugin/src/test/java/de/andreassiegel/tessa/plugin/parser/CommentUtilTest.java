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

import static de.andreassiegel.tessa.plugin.parser.CommentUtil.findLastRegionBeginIndex;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.findNamedRegionBeginIndex;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.firstBlockComment;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.isBeginRegionComment;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.isRegionComment;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.javadocComment;
import static de.andreassiegel.tessa.plugin.parser.CommentUtil.regionLineComments;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CommentUtilTest {

  static final Path BASE_PATH = Paths.get("src/test");

  // region javadocComment()

  @Test
  void javadocComment_withValidClass_returnsStringFromJavadoc() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ClassOrInterfaceDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("SampleTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get();

    // Act
    String comment = javadocComment(declaration);

    // Assert
    assertEquals(
        """
        This test implementation is intended as a proof of concept to get a better idea of how the module
        test implementation using the new concept could look like.""",
        comment);
  }

  @ParameterizedTest
  @ValueSource(strings = {"NoCommentTest", "BlockCommentTest", "LineCommentTest", "MissingClass"})
  @NullSource
  void javadocComment_withInvalidClass_returnsEmptyString(String className) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/NoCommentTest.java");
    ClassOrInterfaceDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass(className)
            .map(ParsedTestClass::getClassDeclaration)
            .orElse(null);

    // Act
    String comment = javadocComment(declaration);

    // Assert
    assertEquals("", comment);
  }

  @ParameterizedTest
  @CsvSource({
    "setup, This is the setup comment.",
    "cleanup, This is the cleanup comment.",
    "testMethod, This is a test.",
    "testMethodWithParameters, This is a test with parameters."
  })
  void javadocComment_withValidClass_returnsStringFromJavadoc(
      String methodName, String expectedComment) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/AnnotationTest.java");
    MethodDeclaration methodDeclaration =
        new ParsedTestFile(path, BASE_PATH)
                .getTestClass("AnnotationTest")
                .map(ParsedTestClass::getClassDeclaration)
                .map(d -> d.getMethodsByName(methodName))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .get();

    // Act
    String comment = javadocComment(methodDeclaration);

    // Assert
    assertEquals(expectedComment, comment);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "methodWithoutComment",
        "methodWithLineComment",
        "methodWithBlockComment",
        "methodThatDoesNotExist"
      })
  @NullSource
  void javadocComment_withMethod_returnsEmptyString(String methodName) throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/NoCommentTest.java");
    MethodDeclaration methodDeclaration =
        new ParsedTestFile(path, BASE_PATH)
                .getTestClass("MethodTest")
                .map(ParsedTestClass::getClassDeclaration)
                .map(d -> d.getMethodsByName(methodName))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .orElse(null);

    // Act
    String comment = javadocComment(methodDeclaration);

    // Assert
    assertEquals("", comment);
  }

  // endregion

  // region firstBlockComment()

  @Test
  void firstBlockComment_withValidClass_returnsStringFromBlock() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    ClassOrInterfaceDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("SampleTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get();

    // Act
    String comment = firstBlockComment(declaration);

    // Assert
    assertNotNull(comment);
    assertEquals("This is the first block comment in the test class.", comment);
  }

  @Test
  void firstBlockComment_withInvalidClass_returnsEmptyString() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/NoCommentTest.java");
    ClassOrInterfaceDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("BlockCommentTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get();

    // Act
    String comment = firstBlockComment(declaration);

    // Assert
    assertNotNull(comment);
    assertEquals("", comment);
  }

  @Test
  void firstBlockComment_withNull_returnsEmptyString() {
    // Act
    String comment = firstBlockComment(null);

    // Assert
    assertNotNull(comment);
    assertEquals("", comment);
  }

  @Test
  void firstBlockComment_withValidMethod_returnsStringFromBlock() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/SampleTest.java");
    MethodDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("SampleTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get()
            .getMethodsByName("testCase2")
            .get(0);

    // Act
    String comment = firstBlockComment(declaration);

    // Assert
    assertNotNull(comment);
    assertEquals("This is a block comment in the method.", comment);
  }

  @Test
  void firstBlockComment_withValidAsciiDocCommentInClass_returnsBlockWithoutIndentation()
      throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/CommentTest.java");
    ClassOrInterfaceDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("AsciiDocCommentTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get();

    // Act
    String comment = firstBlockComment(declaration);

    // Assert
    assertNotNull(comment);
    assertEquals(
        """
        = Some Asciidoc Content

        This is a sample block comment containing Asciidoc documentation.
        This is used to illustrate that indentation is removed but linebreaks stay intact.

        - Lists
        - can
        - be
        - used

        [source,json]
        ----
        {
        "message": "Hello World"
        }
        ----

        |===
        |Header 1 |Header 2

        |Foo
        |Bar

        |Bla
        |Blubb
        |===""",
        comment);
  }

  @Test
  void firstBlockComment_withValidAsciiDocCommentInMethod_returnsBlockWithoutIndentation()
      throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/CommentTest.java");
    MethodDeclaration declaration =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("AsciiDocCommentTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get()
            .getMethodsByName("someMethod")
            .get(0);

    // Act
    String comment = firstBlockComment(declaration);

    // Assert
    assertNotNull(comment);
    assertEquals(
        """
        = Asciidoc Content

        This also uses Asciidoc.
        It works the same way as at class level.""",
        comment);
  }

  // endregion

  // region regionLineComments()

  @Test
  void regionLineComments_withInvalidRegionComments_throwsException() throws IOException {
    // Arrange
    var path = Paths.get("src/test/resources/com/example/test/CommentTest.java");
    List<Comment> comments =
        new ParsedTestFile(path, BASE_PATH)
            .getTestClass("InvalidRegionTest")
            .map(ParsedTestClass::getClassDeclaration)
            .get()
            .getOrphanComments();

    // Act & Assert
    assertThrows(RuntimeException.class, () -> regionLineComments(comments));
  }

  // endregion

  // region findNamedRegionBeginIndex()

  @ParameterizedTest
  @MethodSource("findNamedRegionInvalidData")
  void findNamedRegionBeginIndex_withInvalidInput_returnsMinusOne(
      List<String> commentContent, String regionName) {
    // Arrange
    List<LineComment> comments =
        commentContent != null ? commentContent.stream().map(LineComment::new).toList() : null;

    // Act
    int index = findNamedRegionBeginIndex(comments, regionName);

    // Assert
    assertEquals(-1, index);
  }

  static Stream<Arguments> findNamedRegionInvalidData() {
    return Stream.of(
        Arguments.of((List) null, "My Region"),
        Arguments.of(emptyList(), "My Region"),
        Arguments.of(
            List.of("region", "endregion", "something else", "region ", "endregion"), "My Region"),
        Arguments.of(
            List.of(
                "region My Region",
                "endregion",
                "something else",
                "region Other Region",
                "endregion"),
            null),
        Arguments.of(
            List.of(
                "region My Region",
                "endregion",
                "something else",
                "region Other Region",
                "endregion"),
            ""),
        Arguments.of(
            List.of(
                "region My Region",
                "endregion",
                "something else",
                "region Other Region",
                "endregion"),
            "   "),
        Arguments.of(List.of("foo", "bar", "bla", "blubb", "My Region"), "My Region"));
  }

  @ParameterizedTest
  @MethodSource("findNamedRegionValidData")
  void findNamedRegionBeginIndex_withValidInput_returnsExpectedIndex(
      List<String> commentContent, String regionName, int expectedIndex) {
    // Arrange
    List<LineComment> comments = commentContent.stream().map(LineComment::new).toList();

    // Act
    int index = findNamedRegionBeginIndex(comments, regionName);

    // Assert
    assertEquals(expectedIndex, index);
  }

  static Stream<Arguments> findNamedRegionValidData() {
    return Stream.of(
        Arguments.of(
            List.of(
                "region Region 1",
                "Some other comment",
                "endregion",
                "region Region 2",
                "endregion",
                "region Region 3",
                "Again something else",
                "endregion",
                "region Last Region",
                "endregion"),
            "Last Region",
            8),
        Arguments.of(
            List.of(
                "region Region 1",
                "Some other comment",
                "endregion",
                "region Region 2",
                "endregion",
                "region Region 3",
                "Again something else",
                "endregion",
                "region Last Region",
                "endregion"),
            " Last Region ",
            8),
        Arguments.of(List.of("region Region 1", "endregion"), "Region 1", 0),
        Arguments.of(List.of("region Region 1", "endregion"), " Region 1 ", 0),
        Arguments.of(List.of("something else", "region Region 1", "endregion"), "Region 1", 1),
        Arguments.of(List.of("something else", "region Region 1", "endregion"), " Region 1 ", 1),
        Arguments.of(List.of("something else", "region Region 1 ", "endregion"), " Region 1 ", 1));
  }

  // endregion

  // region findLastRegionBeginIndex()

  @ParameterizedTest
  @MethodSource("findLastRegionInvalidData")
  void findLastRegionBeginIndex_withInvalidInput_returnsMinusOne(List<String> commentContent) {
    // Arrange
    List<LineComment> comments =
        commentContent != null ? commentContent.stream().map(LineComment::new).toList() : null;

    // Act
    int index = findLastRegionBeginIndex(comments);

    // Assert
    assertEquals(-1, index);
  }

  static Stream<Arguments> findLastRegionInvalidData() {
    return Stream.of(
        Arguments.of((List) null),
        Arguments.of(emptyList()),
        Arguments.of(List.of("region", "endregion", "something else", "region ", "endregion")),
        Arguments.of(List.of("foo", "bar", "bla", "blubb")));
  }

  @ParameterizedTest
  @MethodSource("findLastRegionValidData")
  void findLastRegionBeginIndex_withValidInput_returnsExpectedIndex(
      List<String> commentContent, int expectedIndex) {
    // Arrange
    List<LineComment> comments = commentContent.stream().map(LineComment::new).toList();

    // Act
    int index = findLastRegionBeginIndex(comments);

    // Assert
    assertEquals(expectedIndex, index);
  }

  static Stream<Arguments> findLastRegionValidData() {
    return Stream.of(
        Arguments.of(
            List.of(
                "region Region 1",
                "Some other comment",
                "endregion",
                "region Region 2",
                "endregion",
                "region Region 3",
                "Again something else",
                "endregion",
                "region Last Region",
                "endregion"),
            8),
        Arguments.of(List.of("region Region 1", "endregion"), 0),
        Arguments.of(List.of("something else", "region Region 1", "endregion"), 1));
  }

  // endregion

  // region isRegionComment()

  @ParameterizedTest
  @ValueSource(
      strings = {
        " region My Region",
        "region My Region",
        "region My Region ",
        "endregion",
        " endregion",
        " endregion ",
        "endregion "
      })
  void isRegionComment_withValidRegionComment_returnsTrue(String content) {
    // Arrange
    LineComment lineComment = new LineComment(content);

    // Act
    boolean result = isRegionComment(lineComment);

    // Assert
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"region", "Some other comment", "regionWithMissingSpace"})
  void isRegionComment_withInvalidComment_returnsFalse(String content) {
    // Arrange
    LineComment lineComment = new LineComment(content);

    // Act
    boolean result = isRegionComment(lineComment);

    // Assert
    assertFalse(result);
  }

  @Test
  void isRegionComment_withNull_returnsFalse() {
    // Act & Assert
    assertFalse(isRegionComment(null));
  }

  // endregion

  // region isBeginRegionComment()

  @ParameterizedTest
  @ValueSource(strings = {" region My Region", "region My Region", "region My Region "})
  void isBeginRegionComment_withValidRegionBegin_returnsTrue(String content) {
    // Arrange
    LineComment lineComment = new LineComment(content);

    // Act
    boolean result = isBeginRegionComment(lineComment);

    // Assert
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        " endregion",
        "endregion",
        "endregion ",
        "Some other comment",
        " region",
        "region",
        "region "
      })
  void isBeginRegionComment_withInvalidRegionBegin_returnsFalse(String content) {
    // Arrange
    LineComment lineComment = new LineComment(content);

    // Act
    boolean result = isBeginRegionComment(lineComment);

    // Assert
    assertFalse(result);
  }

  @Test
  void isBeginRegionComment_withNull_returnsFalse() {
    // Act & Assert
    assertFalse(isBeginRegionComment(null));
  }

  // endregion

  // region regionName()

  @ParameterizedTest
  @ValueSource(strings = {"region ", " region "})
  void regionName_withValidPrefixes_returnsExpectedRegionName(String prefix) {
    // Arrange
    String expectedRegionName = "My region";
    LineComment lineComment = new LineComment(prefix + expectedRegionName);

    // Act
    String regionName = CommentUtil.regionName(lineComment);

    // Assert
    assertNotNull(regionName);
    assertEquals(expectedRegionName, regionName);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"region", "This is some other comment", "regionWithOutSpace", "endregion"})
  void regionName_withInvalidComment_returnsEmptyString(String content) {
    // Arrange
    LineComment lineComment = new LineComment(content);

    // Act
    String regionName = CommentUtil.regionName(lineComment);

    // Assert
    assertNotNull(regionName);
    assertEquals("", regionName);
  }

  @Test
  void regionName_withNull_returnsEmptyString() {
    // Act
    String regionName = CommentUtil.regionName(null);

    // Assert
    assertNotNull(regionName);
    assertEquals("", regionName);
  }

  // endregion
}
