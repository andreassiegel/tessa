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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/** Mojo for the plugin goal to clean the test documentation output directory. */
@Mojo(name = "clean-test-docs", defaultPhase = LifecyclePhase.CLEAN)
public class CleanTestDocsMojo extends AbstractMojo {

  /** The target directory for generated documentation. */
  @Parameter(
      property = "outputDirectory",
      defaultValue = "${project.build.directory}/test-documentation")
  private String outputDirectory;

  /**
   * Deletes all files in the output directory as well as the output directory itself.
   *
   * @throws MojoExecutionException if the directory could not be deleted
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var path = Paths.get(outputDirectory);

    // Delete all the files in the output directory
    try (Stream<Path> pathStream = Files.walk(path)) {
      pathStream.filter(Predicate.not(Files::isDirectory)).forEach(this::deleteFileIfExists);

      // Delete the output directory itself
      Files.deleteIfExists(path);
    } catch (Exception e) {
      getLog().error("Could not delete output directory because of an error", e);
      throw new MojoExecutionException("Could not delete output directory because of an error", e);
    }

    getLog().info("Output directory " + outputDirectory + " was deleted");
  }

  /**
   * Deletes the file at the provided path.
   *
   * @param file the file path
   * @throws RuntimeException if the file could not be deleted
   */
  void deleteFileIfExists(Path file) {
    var fileName = file.toString();
    try {
      if (Files.deleteIfExists(file)) {
        getLog().info("Deleted file " + fileName);
      }
    } catch (IOException e) {
      getLog().error("Could not delete file " + fileName + " because of an error", e);
      throw new RuntimeException("Could not delete file " + fileName + " because of an error", e);
    }
  }
}
