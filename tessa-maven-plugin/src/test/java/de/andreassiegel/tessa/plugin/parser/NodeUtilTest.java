package de.andreassiegel.tessa.plugin.parser;

import static de.andreassiegel.tessa.plugin.parser.NodeUtil.childrenInRegion;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.EmptyStmt;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NodeUtilTest {

  // region childrenInRegion()

  @Test
  void childrenInRegion_beginWithoutRange_returnsEmptyList() {
    // Arrange
    List<Node> childNodes = List.of(new EmptyStmt(), new EmptyStmt());

    var beginNode = new EmptyStmt();
    beginNode.setRange(null);

    var endNode = new EmptyStmt();
    endNode.setRange(new Range(new Position(1, 1), new Position(3, 1)));

    // Act
    List<Node> regionChildNodes = childrenInRegion(childNodes, beginNode, endNode);

    // Assert
    assertNotNull(regionChildNodes);
    assertEquals(emptyList(), regionChildNodes);
  }

  @Test
  void childrenInRegion_endWithoutRange_returnsEmptyList() {
    // Arrange
    List<Node> childNodes = List.of(new EmptyStmt(), new EmptyStmt());

    var beginNode = new EmptyStmt();
    beginNode.setRange(new Range(new Position(1, 1), new Position(3, 1)));

    var endNode = new EmptyStmt();
    endNode.setRange(null);

    // Act
    List<Node> regionChildNodes = childrenInRegion(childNodes, beginNode, endNode);

    // Assert
    assertNotNull(regionChildNodes);
    assertEquals(emptyList(), regionChildNodes);
  }

  @Test
  void childrenInRegion_beginAndEndWithoutRange_returnsEmptyList() {
    // Arrange
    List<Node> childNodes = List.of(new EmptyStmt(), new EmptyStmt());

    var beginNode = new EmptyStmt();
    beginNode.setRange(null);

    var endNode = new EmptyStmt();
    endNode.setRange(null);

    // Act
    List<Node> regionChildNodes = childrenInRegion(childNodes, beginNode, endNode);

    // Assert
    assertNotNull(regionChildNodes);
    assertEquals(emptyList(), regionChildNodes);
  }

  @ParameterizedTest
  @MethodSource("childrenInRegionData")
  void childrenInRegion_withBeginAndEnd_returnsExpectedChildNodes(Integer beginLine, Integer endLine, List<Node> childNodes, List<Node> expectedChildNodes) {
    // Arrange
    Function<Integer, Node> nodeFunction = line -> {
      var node = new EmptyStmt();
      node.setRange(new Range(new Position(line, 1), new Position(line, 30)));
      return node;
    };

    var beginNode = nodeFunction.apply(beginLine);
    var endNode = nodeFunction.apply(endLine);

    // Act
    List<Node> regionChildNodes = childrenInRegion(childNodes, beginNode, endNode);

    // Assert
    assertNotNull(regionChildNodes);
    assertEquals(expectedChildNodes, regionChildNodes);
  }

  static Stream<Arguments> childrenInRegionData() {
    BiFunction<Position, Position, Node> nodeFunction = (begin, end) -> {
      var node = new EmptyStmt();
      node.setRange(new Range(begin, end));
      return node;
    };
    Node node1 = nodeFunction.apply(new Position(1, 1), new Position(1, 3));
    Node node2 = nodeFunction.apply(new Position(3, 10), new Position(3, 30));
    Node node3 = nodeFunction.apply(new Position(5, 10), new Position(5, 15));

    return Stream.of(
        Arguments.of(3, 5, emptyList(), emptyList()),
        Arguments.of(3, 5, List.of(node1), emptyList()),
        Arguments.of(3, 5, List.of(node1, node2), emptyList()),
        Arguments.of(2, 5, List.of(node1, node2), List.of(node2)),
        Arguments.of(1, 6, List.of(node1, node2, node3), List.of(node2, node3)),
        Arguments.of(1, 5, List.of(node3), emptyList()),
        Arguments.of(1, 2, List.of(node2, node3), emptyList())
    );
  }

  // endregion
}
