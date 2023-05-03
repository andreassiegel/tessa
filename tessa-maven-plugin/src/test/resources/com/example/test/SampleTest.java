package com.example.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test implementation is intended as a proof of concept to get a better idea of how the module
 * test implementation using the new concept could look like.
 */
@DisplayName("MIT 00 - Example Test for new Module Test Implementation Structure")
class SampleTest {

  /*
  This is the first block comment in the test class.
   */

  /*
  This is some other block comment.
   */

  /**
   * This is something that needs to be done as test setup:
   * <ul>
   *   <li>Foo</li>
   *   <li>Bar</li>
   *   <li>Bla</li>
   *   <li>Blubb</li>
   * </ul>
   */
  @BeforeAll
  static void setup() {

  }

  /**
   * These are the necessary cleanup steps:
   * <ul>
   *   <li>Remove this</li>
   *   <li>Reset that</li>
   * </ul>
   */
  @AfterAll
  static void cleanup() {

  }

  // region Happy Cases

  /** Test cases focussing on different happy case scenarios. */
  @DisplayName("Parameterized Test Case 1")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = { "a", "b", "c" })
  void testCase1(String value) {
    // Arrange

    // Act

    // Assert
  }

  @DisplayName("Normal Test Case 2")
  @Test
  void testCase2() {

    /*
    This is a block comment in the method.
     */

    // Arrange

    // Act

    // Assert
  }

  // endregion

  // region Failure Cases

  @DisplayName("Failure Test Case 1")
  @Test
  void testFailureCase1() {}

  // endregion
}

/**
 * This test implementation is intended as a proof of concept to get a better idea of how the module
 * test implementation using the new concept could look like.
 */
@DisplayName("MIT 00 - Example Test for new Module Test Implementation Structure")
class SampleNoRegionTest {

  /*
  This is the first block comment in the test class.
   */

  /*
  This is some other block comment.
   */

  /**
   * This is something that needs to be done as test setup:
   * <ul>
   *   <li>Foo</li>
   *   <li>Bar</li>
   *   <li>Bla</li>
   *   <li>Blubb</li>
   * </ul>
   */
  @BeforeAll
  static void setup() {

  }

  /**
   * These are the necessary cleanup steps:
   * <ul>
   *   <li>Remove this</li>
   *   <li>Reset that</li>
   * </ul>
   */
  @AfterAll
  static void cleanup() {

  }

  /** Test cases focussing on different happy case scenarios. */
  @DisplayName("Parameterized Test Case 1")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = { "a", "b", "c" })
  void testCase1(String value) {
    // Arrange

    // Act

    // Assert
  }

  @DisplayName("Normal Test Case 2")
  @Test
  void testCase2() {

    /*
    This is a block comment in the method.
     */

    // Arrange

    // Act

    // Assert
  }
}

interface SampleInterface {
}
