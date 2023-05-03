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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ModelUtilTest {

  // region valueOrFallback()

  @Test
  void valueOrFallback_withValidValue_returnsValue() {
    // Arrange
    String value = "my value";

    // Act
    String returnedValue = valueOrFallback(value, "fallback");

    // Assert
    assertNotNull(returnedValue);
    assertEquals(value, returnedValue);
  }

  @Test
  void valueOrFallback_withInvalidValueAndNullFallback_returnsNull() {
    // Arrange
    String value = "";
    String fallbackValue = null;

    // Act
    String returnedValue = valueOrFallback(value, fallbackValue);

    // Assert
    assertNull(returnedValue);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "\n"})
  @NullSource
  void valueOrFallback_withInvalidValue_returnsFallback(String value) {
    // Arrange
    String fallbackValue = "fallback";

    // Act
    String returnedValue = valueOrFallback(value, fallbackValue);

    // Assert
    assertNotNull(returnedValue);
    assertEquals(fallbackValue, returnedValue);
  }

  // endregion
}
