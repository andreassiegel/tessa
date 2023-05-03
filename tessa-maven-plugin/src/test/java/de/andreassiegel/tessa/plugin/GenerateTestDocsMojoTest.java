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

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateTestDocsMojoTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  MavenProject mockProject;

  @AfterEach
  void cleanup() {
    Mockito.reset(mockProject);
  }

  // region directoryStream()

  @Test
  void directoryStream_withNullDirectories_returnsDefault() {
    // Arrange
    List<String> directories = null;
    var defaultDirectory = "src/test/java";
    when(mockProject.getBuild().getTestSourceDirectory()).thenReturn(defaultDirectory);

    GenerateTestDocsMojo mojo = new GenerateTestDocsMojo(mockProject, directories);

    // Act
    List<String> returnedDirectories = mojo.directoryStream().toList();

    // Assert
    assertNotNull(returnedDirectories);
    assertEquals(1, returnedDirectories.size());
    assertTrue(returnedDirectories.contains(defaultDirectory));
  }

  @Test
  void directoryStream_withEmptyDirectories_returnsDefault() {
    // Arrange
    List<String> directories = emptyList();
    var defaultDirectory = "src/test/java";
    when(mockProject.getBuild().getTestSourceDirectory()).thenReturn(defaultDirectory);

    GenerateTestDocsMojo mojo = new GenerateTestDocsMojo(mockProject, directories);

    // Act
    List<String> returnedDirectories = mojo.directoryStream().toList();

    // Assert
    assertNotNull(returnedDirectories);
    assertEquals(1, returnedDirectories.size());
    assertTrue(returnedDirectories.contains(defaultDirectory));
  }

  @Test
  void directoryStream_withDirectories_returnsParameter() {
    // Arrange
    List<String> directories =
        List.of("src/system-integration-test/java", "src/integration-test/java");

    GenerateTestDocsMojo mojo = new GenerateTestDocsMojo(mockProject, directories);

    // Act
    List<String> returnedDirectories = mojo.directoryStream().toList();

    // Assert
    assertNotNull(returnedDirectories);
    assertEquals(directories.size(), returnedDirectories.size());
    assertEquals(directories, returnedDirectories);

    verifyNoInteractions(mockProject);
  }

  // endregion

}
