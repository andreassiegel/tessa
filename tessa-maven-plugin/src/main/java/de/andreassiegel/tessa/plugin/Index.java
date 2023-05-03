package de.andreassiegel.tessa.plugin;

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

import lombok.Getter;

/** Configuration for index generation in the {@link GenerateTestDocsMojo}. */
@Getter
public class Index {

  /** Defines whether an index file should be generated (an overview of all tests). */
  private final Boolean generateIndex = true;

  /**
   * The name of the subdirectory to create the test documentation files in, as well as the filename
   * of the index file without file ending.
   */
  private final String name = "tests";

  /** The title of the test documentation index file. */
  private final String title = "Test Overview";
}
