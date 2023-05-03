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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * The data model for test cases. A test case is the representation of a test method may contain
 * several steps.
 *
 * @see TestStep
 */
@Builder
public class TestCase {

  // region Field Declarations

  /** The name of the test method. */
  @Getter private String methodName;

  /**
   * The title of the test case.
   *
   * <p>The value is read from the {@code DisplayName} annotation of the test method (at method
   * level).
   */
  private String title;

  /**
   * The description of the test.
   *
   * <p>The content is read from the Javadoc comment of the test method.
   */
  private String description;

  /**
   * The status of the test case, e.g., {@code DRAFT}, {@code IN REVIEW}, {@code DOCUMENTED}, or
   * {@code IMPLEMENTED}.
   *
   * <p>The value is read from the {@code Status} annotation of the test method (at method level).
   */
  private String status;

  /**
   * The optional warning for a disabled test case.
   *
   * <p>The value is read from the {@code Disabled} annotation of the test method (at method level).
   */
  private String disabledNote;

  /**
   * Further details about the tes case, e.g., additional information.
   *
   * <p>The information is read from the first block comment inside the test method.
   */
  private String furtherInformation;

  /** Flag indicating whether test steps are organized in sections. */
  @Getter private boolean sectioned;

  /**
   * The specific test steps. Test steps are organized in sections, e.g., "Arrange", "Act" or
   * "Assert".
   *
   * <p>Sections are derived from regions in the test methods that are used to structure the test
   * implementation in the method. A region is defined by line comments:
   *
   * <pre>{@code
   * // region Arrange
   *
   * doSomething();
   *
   * // endregion
   * }</pre>
   *
   * <p>If no regions are used in a test method, test steps get added to a default section.
   */
  @Getter @Default private Map<String, List<TestStep>> sections = new HashMap<>();

  /** The line number of the begin of the test method. */
  @Getter private Integer beginLine;

  /** The line number of the end of the test method. */
  @Getter private Integer endLine;

  // endregion

  // region Getters

  /**
   * Returns the test case title. If none is set, the method name is used as fallback value.
   *
   * @return the title.
   */
  public String getTitle() {
    return ModelUtil.valueOrFallback(title, methodName);
  }

  /**
   * Returns the description of the test case. If none is set, "N/A" is used as fallback.
   *
   * @return the description
   */
  public String getDescription() {
    return ModelUtil.valueOrFallback(description, "N/A");
  }

  /**
   * Returns the status of the test case. If none is defined, the result will be {@code null}.
   *
   * @return the status
   */
  public String getStatus() {
    return ModelUtil.valueOrFallback(status, null);
  }

  /**
   * Returns the note about a disabled test case. If none is defined (the test is not disabled),
   * {@code null} is returned.
   *
   * @return the note
   */
  public String getDisabledNote() {
    return ModelUtil.valueOrFallback(disabledNote, null);
  }

  /**
   * Returns further information about the test case. If there are none, {@code null} is returned.
   *
   * @return the additional information
   */
  public String getFurtherInformation() {
    return ModelUtil.valueOrFallback(furtherInformation, null);
  }

  // region
}
