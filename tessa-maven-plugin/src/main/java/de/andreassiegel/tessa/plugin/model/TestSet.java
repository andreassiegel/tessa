package de.andreassiegel.tessa.plugin.model;

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

import static de.andreassiegel.tessa.plugin.model.ModelUtil.valueOrFallback;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * The data model for test sets. A test set is the representation of a test class that contains test
 * cases.
 *
 * @see TestCase
 */
@Builder
public class TestSet {

  // region Field Declarations

  /** The path of the source file the test is documented and implemented in. */
  private Path path;

  /** The name of the test class. */
  private String className;

  /**
   * The title of the test.
   *
   * <p>The value is read from the {@code DisplayName} annotation of the test class (at class
   * level).
   */
  private String title;

  /**
   * The description of the test.
   *
   * <p>The content is read from the Javadoc comment of the test class.
   */
  private String description;

  /**
   * The status of the test, e.g., {@code DRAFT}, {@code IN REVIEW}, {@code DOCUMENTED}, or {@code
   * IMPLEMENTED}.
   *
   * <p>The value is read from the {@code Status} annotation of the test class (at class level).
   */
  private String status;

  /**
   * The optional warning for disabled tests.
   *
   * <p>The value is read from the {@code Status} annotation of the test class (at class level).
   */
  private String disabledNote;

  /**
   * Further details about the test, e.g., prerequisites or other additional information.
   *
   * <p>The information is read from the first block comment inside the test class, outside the
   * method scope.
   */
  private String furtherInformation;

  /**
   * The setup information or steps of the test.
   *
   * <p>The content is read from the Javadoc comment of the method annotated with
   * {@code @BeforeAll}.
   */
  @Default private String setup = "None";

  /**
   * The cleanup information or steps of the test.
   *
   * <p>The content is read from the Javadoc comment of the method annotated with {@code @AfterAll}.
   */
  @Default private String cleanup = "None";

  /**
   * The specific test cases. Test cases are organized in categories, e.g., "Happy Cases" or
   * "Failure Cases".
   *
   * <p>Categories are derived from regions in the test file that are used to organize the
   * individual test methods annotated with {@code @Test} or {@code ParameterizedTest}. A region is
   * defined by line comments surrounding the tests:
   *
   * <pre>{@code
   * // region Happy Cases
   *
   *  @Test
   *  void testSomething() { ... }
   *
   *  // endregion
   * }</pre>
   *
   * <p>If no regions are used in a test file, test cases get added to a default region.
   */
  @Getter @Default private Map<String, List<TestCase>> testCases = new HashMap<>();

  /** Flag indicating whether test cases are organized in categories. */
  @Getter private boolean categorized;

  /** The base URL for links to the test implemntation. */
  private String linkBaseUrl;

  // endregion

  // region Helper Methods

  /**
   * Injects the base URL for links into the model.
   *
   * <p>The URL usually is read from the plugin configuration.
   *
   * @param linkBaseUrl the base URL for links
   * @return the updated test set data model
   */
  public TestSet injectLinkBaseUrl(String linkBaseUrl) {
    this.linkBaseUrl = linkBaseUrl;
    return this;
  }

  // endregion

  // region Getters

  /**
   * Returns the test set title. If none is set, the class name is used as fallback value.
   *
   * @return the title.
   */
  public String getTitle() {
    return valueOrFallback(title, getClassName());
  }

  /**
   * Returns the class name of the test class. If none is set in the data model, the filename is
   * used as fallback value.
   *
   * @return the class name
   */
  public String getClassName() {
    return valueOrFallback(className, path.getFileName().toString());
  }

  /**
   * Returns the description of the test set. If none is set, "N/A" is used as fallback.
   *
   * @return the description
   */
  public String getDescription() {
    return valueOrFallback(description, "N/A");
  }

  /**
   * Returns the status of the overall test set. If none is defined, the result will be {@code
   * null}.
   *
   * @return the status
   */
  public String getStatus() {
    return valueOrFallback(status, null);
  }

  /**
   * Returns the note about a disabled test set. If none is defined (tests are not disabled), {@code
   * null} is returned.
   *
   * @return the note
   */
  public String getDisabledNote() {
    return valueOrFallback(disabledNote, null);
  }

  /**
   * Returns the setup information for the test set. If none is defined, "None" is used as fallback.
   *
   * @return the setup information
   */
  public String getSetup() {
    return valueOrFallback(setup, "None");
  }

  /**
   * Returns the cleanup information for the test set. If none is defined, "None" is used as
   * fallback.
   *
   * @return the cleanup information
   */
  public String getCleanup() {
    return valueOrFallback(cleanup, "None");
  }

  /**
   * Returns further information about the test set. If there are none, {@code null} is returned.
   *
   * @return the additional information
   */
  public String getFurtherInformation() {
    return valueOrFallback(furtherInformation, null);
  }

  /**
   * Returns the file path of the file containing the test class.
   *
   * @return the path
   */
  public String getPath() {
    return path.toString();
  }

  /**
   * Constructs and returns a link to the test file.
   *
   * <p>If no base URL for links is defined in the data model, no link ({@code null}) is returned.
   *
   * @return the link
   */
  public String getLink() {
    var baseLink = valueOrFallback(linkBaseUrl, null);
    return baseLink != null ? linkBaseUrl + "/" + path : null;
  }

  // endregion
}
