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

/** Utilities for data models. */
public class ModelUtil {

  private ModelUtil() {}

  /**
   * Returns the value if it neither {@code null}, nor empty, nor blank.
   *
   * <p>Note that the returned fallback value may be {@code null} if provided accordingly.
   *
   * @param value the value
   * @param fallbackValue the fallback value
   * @return either the provided value or the fallback
   */
  static String valueOrFallback(String value, String fallbackValue) {
    return value == null || value.isEmpty() || value.isBlank() ? fallbackValue : value;
  }
}
