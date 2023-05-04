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

import static de.andreassiegel.tessa.plugin.generator.FileUtil.initializeDirectory;
import static java.nio.charset.StandardCharsets.UTF_8;

import de.andreassiegel.tessa.plugin.Index;
import de.andreassiegel.tessa.plugin.model.TestSet;
import de.andreassiegel.tessa.plugin.model.index.DocumentIndex;
import de.andreassiegel.tessa.plugin.model.index.DocumentIndexItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Generator for Asciidoctor documents using Freemarker templates. */
public class AsciiDocGenerator {

  private static final String TEMPLATE_DIRECTORY = "/templates";
  private static final String TEST_TEMPLATE = "test.ftlh";
  private static final String INDEX_TEMPLATE = "index.ftlh";

  private final String outputDirectory;
  private final Index indexConfiguration;
  private final Template testTemplate;
  private final Template indexTemplate;

  private final Optional<DocumentIndex> index;

  /**
   * Instantiates the Asciidoc generator.
   *
   * <p>It configures Freemarker, prepares the output directory, and loads the file templates that
   * will be used to generate documents.
   *
   * @param outputDirectory the output directory
   * @param indexConfiguration configuration for index creation
   */
  public AsciiDocGenerator(String outputDirectory, Index indexConfiguration) {
    this.outputDirectory = outputDirectory;
    this.indexConfiguration = indexConfiguration;

    // Configure FreeMarker
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
    cfg.setClassForTemplateLoading(AsciiDocGenerator.class, TEMPLATE_DIRECTORY);

    // Load the template
    testTemplate = initializeTemplate(cfg, TEST_TEMPLATE);
    indexTemplate = initializeTemplate(cfg, INDEX_TEMPLATE);

    // Prepare the output directory
    initializeDirectory(outputDirectory);

    // Initialize the document index
    index = initializeIndex(indexConfiguration);
  }

  /**
   * Generates a document for a test set in the output directory.
   *
   * <p>Depending on whether index creation is enabled in the configuration, the generated files are
   * placed directly in the output directory or in the configured subdirectory for files linked in
   * the index.
   *
   * <p>This method does not generate the index document. Documents are only collected to be
   * included in the index document later on (see {@link #generateIndex()}).
   *
   * @param testSet the test set
   */
  public void generate(TestSet testSet) {
    AtomicReference<String> itemOutputDirectory = new AtomicReference<>(outputDirectory);
    var filename = testSet.getClassName() + ".adoc";
    index.ifPresent(
        index -> {
          var indexItem =
              DocumentIndexItem.builder()
                  .title(testSet.getTitle())
                  .filename(filename)
                  .description(testSet.getDescription())
                  .status(testSet.getStatus())
                  .build();
          itemOutputDirectory.set(fullIndexItemDirectory(index.getDirectory()));
          index.addToIndex(indexItem);
        });

    // Generate the output
    try (Writer out = new FileWriter(itemOutputDirectory + "/" + filename, UTF_8)) {
      testTemplate.process(testSet, out);
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(
          "Could not process the test documentation template and data model", e);
    }
  }

  /**
   * Generates the index document that contains an overview of all generated documents.
   *
   * <p>If index creation is disabled in the configuration, the method does nothing.
   */
  public void generateIndex() {
    index.ifPresent(
        index -> {
          try (Writer out =
              new FileWriter(
                  fullIndexItemDirectory(indexConfiguration.getName()) + ".adoc", UTF_8)) {
            indexTemplate.process(index, out);
          } catch (IOException | TemplateException e) {
            throw new RuntimeException("Could not process the index template and data model", e);
          }
        });
  }

  /**
   * Initializes the index, i.e., the item output directory is created if it does not exist yet, and
   * the index data model is prepared so that documents can get added.
   *
   * @param indexConfiguration the index configuration.
   * @return an {@link Optional} containing the prepared index model if index creation is enabled in
   *     the configuration, {@code Optional.empty()} otherwise.
   */
  Optional<DocumentIndex> initializeIndex(Index indexConfiguration) {
    if (indexConfiguration == null || !indexConfiguration.getGenerateIndex()) {
      return Optional.empty();
    }

    // relative path starting at index file path
    var relativeItemOutputDirectory = indexConfiguration.getName();

    // full path for directory initialization
    var itemOutputDirectory = fullIndexItemDirectory(relativeItemOutputDirectory);
    initializeDirectory(itemOutputDirectory);

    return Optional.of(
        new DocumentIndex(indexConfiguration.getTitle(), relativeItemOutputDirectory));
  }

  private String fullIndexItemDirectory(String relativeItemOutputDirectory) {
    return outputDirectory + "/" + relativeItemOutputDirectory;
  }

  /**
   * Initializes the template from the given file using the Freemarker configuration.
   *
   * @param config the configuration
   * @param templateFile the path to the template file within the template base directory from the
   *     Freemarker configuration
   * @return the template
   */
  Template initializeTemplate(Configuration config, String templateFile) {
    try {
      return config.getTemplate(templateFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
