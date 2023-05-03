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

import lombok.Builder;
import lombok.Getter;

/**
 * The data model for a test step. A test step represents something inside a test case.
 *
 * @see TestCase
 */
@Builder
public class TestStep {

  /**
   * The information about the test step.
   *
   * <p>The value is retrieved from line comments and block comments inside a test method.
   */
  @Getter private String info;
}
