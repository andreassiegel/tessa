package de.andreassiegel.tessa.examples;

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

import de.andreassiegel.tessa.annotations.Status;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test acts as a showcase to illustrate how documentation in the test class is turned into a
 * test documentation file.
 */
@Status("Documented")
@DisplayName("Sample Test")
@Disabled("These tests are currently not working because there is no implementation.")
class SampleTest {

  /*
  == Prerequisites

  In order to get nice and meaningful test documentation out of a Java file,
  a few things need to be taken into account when writing the file:

  - @DisplayName annotations need to be used
  - test cases should be organized in regions
  - the class itself as well as individual test cases need Javadoc comments
  - additional information is set in a block comment at class level
  */

  // region Setup and Cleanup

  /** This needs to be done before all the tests can run. */
  @BeforeAll
  static void setup() {}

  /** This needs to be done after all tests ran. */
  @AfterAll
  static void cleanup() {}

  // endregion

  // region Happy Cases

  /** Test cases focussing on different happy case scenarios that are defined by parameters. */
  @DisplayName("Happy Case 1")
  @Status("Documented")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"a", "b", "c"})
  void happyCase1(String input) {

    // region Arrange

    // endregion

    // region Act

    // endregion

    // region Assert

    // endregion
  }

  // endregion

  // region Failure Cases

  /** This is an individual and specific failure case that does not use any parameters. */
  @DisplayName("Failure Case 1")
  @Status("Documented")
  @Disabled("This test is currently not working because of reasons.")
  @Test
  void failureCase1() {

    /*
    There might also be further information for test cases.
     */

    // This gets ignored because it is outside of a region.

    // region Arrange

    // Initialize the DB

    // Do the mock API calls

    // endregion

    // region Act

    /*
    |===
    |Header 1 |Header 2

    |Some table content
    |Other content

    |Even more content
    |_This seems italic!_
    |===
     */

    // Do the request

    // endregion

    // region Assert

    // - Check the response

    // - Verify mock interactions

    // endregion
  }

  // endregion
}
