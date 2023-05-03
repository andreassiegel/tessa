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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for handling nodes in the parsed source code, and for retrieving information from them.
 */
class NodeUtil {

  private NodeUtil() {}

  /**
   * Filters the provided list of nodes based on the position of begin and end nodes, i.e., it keeps
   * all nodes between the begin and end nodes: Children in the region have to start after the end
   * of the begin node, and they must end before the begin of the end node.
   *
   * <p>If either begin or end do not have a range to evaluate, an empty list is returned.
   * Similarly, child nodes without a range are omitted in the result.
   *
   * <p>The result is ordered by the begin of the child range.
   *
   * @param nodes the list of nodes
   * @param beginNode the begin node
   * @param endNode the end node
   * @return the filtered list of children
   */
  static List<Node> childrenInRegion(List<Node> nodes, Node beginNode, Node endNode) {
    if (!(beginNode.hasRange() && endNode.hasRange())) {
      return Collections.emptyList();
    }

    var begin = beginNode.getRange().get().end;
    var end = endNode.getRange().get().begin;

    return nodes.stream()
        .filter(NodeWithRange::hasRange)
        .sorted(Comparator.comparing(n -> n.getRange().get().begin))
        .filter(
            n -> {
              var range = n.getRange().get();
              return range.isAfter(begin) && range.isBefore(end);
            })
        .collect(Collectors.toList());
  }
}
