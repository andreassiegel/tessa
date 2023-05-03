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

import de.andreassiegel.tessa.plugin.generator.AsciiDocGenerator;
import de.andreassiegel.tessa.plugin.parser.ParsedTestFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/** Mojo for the plugin goal to generate test documentation. */
@Mojo(name = "generate-test-docs", defaultPhase = LifecyclePhase.SITE)
public class GenerateTestDocsMojo extends AbstractMojo {

  /** The Maven project. */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /** The target directory for generated documentation. */
  @Parameter(
      property = "outputDirectory",
      defaultValue = "${project.build.directory}/test-documentation")
  private String outputDirectory;

  /** The directories to read the test files from. */
  @Parameter(property = "inputDirectories")
  private List<String> inputDirectories;

  /**
   * The regular expression that is applied to filter the files found in the input directories so
   * that documentation is only generated for relevant (test) files.
   */
  @Parameter(property = "filenameRegex", defaultValue = "\\w+(IT|Test)\\.java$")
  private String filenameRegex;

  /** The index configuration. */
  @Parameter(property = "index")
  private Index index = new Index();

  /**
   * The base URL of the source files in the repository. If this parameter is set, the generated
   * documentation will include links to the source files it was generated from.
   */
  @Parameter(property = "linkBaseUrl", defaultValue = "")
  private String linkBaseUrl;

  /** Default constructor used by Maven when the plugin goal is executed. */
  public GenerateTestDocsMojo() {
    // nothing specific here
  }

  /**
   * Constructor used in tests.
   *
   * @param project the Maven project
   * @param inputDirectories the input directories parameter
   */
  GenerateTestDocsMojo(MavenProject project, List<String> inputDirectories) {
    this.project = project;
    this.inputDirectories = inputDirectories;
  }

  /**
   * Generates the test documentation.
   *
   * <p>Files in the input directories are parsed and then test documentation files get generated
   * for the test files.
   *
   * @throws MojoExecutionException if generating the test documentation failed
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    var generator = new AsciiDocGenerator(outputDirectory, index);
    Consumer<Path> generateDocs = p -> this.generateDocs(generator, p);

    try {
      directoryStream().map(Paths::get).forEach(generateDocs);
    } catch (Exception e) {
      getLog().error("Generating test documentation failed", e);
      throw new MojoExecutionException("Generating test documentation failed", e);
    }
  }

  /**
   * Checks the provided {@code directories} parameter configuration: If nothing is provided in the
   * plugin configuration, the test source directory of the project is used as fallback
   * configuration.
   *
   * @return the stream of directories
   */
  Stream<String> directoryStream() {
    if (inputDirectories == null || inputDirectories.isEmpty()) {
      var defaultDirectory = project.getBuild().getTestSourceDirectory();
      return Stream.of(defaultDirectory);
    }

    return inputDirectories.stream();
  }

  /**
   * Generates documentation for files found in the provided path.
   *
   * @param docGenerator the document generator
   * @param path the input path to scan for test files
   */
  void generateDocs(AsciiDocGenerator docGenerator, Path path) {
    getLog().info("Reading directory " + path.toAbsolutePath());
    try (Stream<Path> pathStream = Files.walk(path)) {
      pathStream
          .filter(p -> p.toString().endsWith(".java"))
          .filter(p -> p.getFileName().toString().matches(filenameRegex))
          .map(this::parseTestClass)
          .filter(ParsedTestFile::containsTests)
          .map(ParsedTestFile::toDocumentDataModel)
          .flatMap(Collection::stream)
          .map(t -> t.injectLinkBaseUrl(linkBaseUrl))
          .forEach(docGenerator::generate);

      docGenerator.generateIndex();
    } catch (IOException e) {
      getLog().error("Unable to read files in directory " + path, e);
      throw new RuntimeException("Unable to read files in directory" + path, e);
    }
  }

  /**
   * Parses a file at the given path and returns an instance of {@link ParsedTestFile} for further
   * processing.
   *
   * @param path the file path
   * @return the parsed file
   * @throws RuntimeException if the file cannot be parsed
   */
  ParsedTestFile parseTestClass(Path path) {
    try {
      getLog().info("Parsing test file: " + path);
      return new ParsedTestFile(path);
    } catch (IOException e) {
      getLog().error("Error parsing test file " + path, e);
      throw new RuntimeException("Error parsing test file " + path, e);
    }
  }
}
