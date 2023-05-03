package de.andreassiegel.tessa.plugin.generator;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Utility for document generation. */
class FileUtil {

  private FileUtil() {}

  /**
   * Initializes a directory at the given path, i.e., the directory is created if it does not exist,
   * and its {@link Path} is returned.
   *
   * @param directoryPath the path to the directory
   * @return the path of the initialized directory
   * @throws RuntimeException if the directory could not be initialized.
   */
  static Path initializeDirectory(String directoryPath) {
    var path = Paths.get(directoryPath);
    if (!Files.exists(path)) {
      try {
        return Files.createDirectories(path);
      } catch (Exception e) {
        throw new RuntimeException("Directory could not be initialized", e);
      }
    }

    if (Files.exists(path) && Files.isDirectory(path)) {
      return path;
    }

    throw new RuntimeException(
        "Path " + directoryPath + " already exists but it is not a directory");
  }
}
