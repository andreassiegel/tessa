package de.andreassiegel.tessa.plugin.parser;

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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import de.andreassiegel.tessa.plugin.model.TestSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * Representation of a parsed Java test file. It provides access to test class declarations within
 * the parsed Java files as well as relevant elements of those classes, e.g., comments, test
 * methods, etc.
 *
 * @see ParsedTestClass
 */
public class ParsedTestFile {

  private final CompilationUnit compilationUnit;
  private final Map<String, ParsedTestClass> testClasses;

  @Getter private final Path filePath;
  @Getter private final Path basePath;

  /**
   * Instantiates the parsed test file.
   *
   * @param filePath the path of the file
   * @param basePath the base path of the project which will be used to relativize file paths
   * @throws IOException if the file cannot be parsed
   */
  public ParsedTestFile(Path filePath, Path basePath) throws IOException {
    this.filePath = filePath;
    this.basePath = basePath;
    this.compilationUnit = StaticJavaParser.parse(filePath);
    this.testClasses = parseTestClasses();
  }

  /**
   * Returns the names of all relevant test classes that have been retrieved from the parsed Java
   * file.
   *
   * @return the collection of class names.
   */
  public Collection<String> getTestClassNames() {
    return testClasses.keySet();
  }

  /**
   * Retrieves a class declaration with the provided name from the compilation unit of the parsed
   * Java file.
   *
   * <p>The method intentionally does not use the method {@link
   * CompilationUnit#getClassByName(String)} because the internal method {@link #parseTestClasses()}
   * filters relevant classes from the file.
   *
   * @param className the name of the class declaration to return
   * @return an {@link Optional} containing the class declaration, or {@code Optional.empty()} if no
   *     class with the provided name was found
   */
  public Optional<ParsedTestClass> getTestClass(String className) {
    return Optional.ofNullable(testClasses.get(className));
  }

  /**
   * Retrieves a stream of relevant test class declarations.
   *
   * @return the stream of class declarations
   */
  Stream<ParsedTestClass> streamTestClasses() {
    return testClasses.values().stream();
  }

  /**
   * Reads all relevant class declarations from the compilation unit.
   *
   * <p>Only actual classes are taken into account, i.e., interface declarations are filtered.
   *
   * @return a map pf class declarations, using the class name as the key
   */
  Map<String, ParsedTestClass> parseTestClasses() {
    return compilationUnit.getChildNodes().stream()
        .filter(node -> node instanceof ClassOrInterfaceDeclaration)
        .map(node -> (ClassOrInterfaceDeclaration) node)
        .filter(classDeclaration -> !classDeclaration.isInterface())
        .collect(Collectors.toMap(NodeWithSimpleName::getNameAsString, ParsedTestClass::new));
  }

  /**
   * Flag indicating whether any of the classes in the file is a test class, i.e., it contains test
   * methods.
   *
   * @return {@code true} if any class includes tests, {@code false} otherwise
   */
  public boolean containsTests() {
    return streamTestClasses().anyMatch(ParsedTestClass::hasTestMethods);
  }

  /**
   * Converts the parsed test file into a list of test sets that can then be used to generate
   * documentation files.
   *
   * <p>From this point on, no interaction with the parsed file and the specifics of a parsed Java
   * file should be necessary anymore, i.e., the intention of the classes in the {@code model}
   * package is to provide an abstraction layer for test documentation generation that purely
   * focuses on the content of the test documentation.
   *
   * @return the list of test sets
   */
  public List<TestSet> toDocumentDataModel() {
    var path = getFilePath();
    return streamTestClasses()
        .map(classDeclaration -> classDeclaration.toDocumentDataModel(path, basePath))
        .toList();
  }
}
