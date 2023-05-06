package com.example.test;

import lombok.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AnnotationTest {

  /**
   * This is the setup comment.
   */
  @BeforeAll
  static void setup() {}

  /**
   * This is the cleanup comment.
   */
  @AfterAll
  static void cleanup() {}

  /**
   * This is a test.
   */
  @Test
  void testMethod() {}

  @Test
  @DisplayName("This is display name without explicit value attribute.")
  void testMethodDisplayName1() {}

  @Test
  @DisplayName(value = "This is display name in the value attribute.")
  void testMethodDisplayName2() {}

  @Test
  @DisplayName(name = "This is an invalid display name.")
  void testMethodDisplayName3() {}

  @Test
  @DisplayName(value = "")
  void testMethodDisplayName4() {}

  @Test
  @DisplayName("")
  void testMethodDisplayName5() {}

  /**
   * This is a test with parameters.
   * @param input the input
   */
  @ParameterizedTest
  @ValueSource(strings = { "a", "b", "c" })
  @NullSource
  void testMethodWithParameters(String input) {}
}

class TestAnnotationTest {

  @Test
  void someMethod() {}
}

class ParameterizedTestAnnotationTest {

  @ParameterizedTest
  void someMethod() {}
}

class NoAnnotationTest {

  void someMethod() {}
}
