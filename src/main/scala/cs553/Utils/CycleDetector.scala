package cs553.Utils

import scala.collection.mutable

object CycleDetector {

  def isCyclicUtil(node: Int, visited: mutable.Set[Int], parent: Option[Int], graph: Map[Int, List[Int]]): Boolean = {
    visited.add(node)

    // Check all adjacent nodes
    graph.getOrElse(node, List()).exists { neighbor =>
      if (!visited.contains(neighbor)) {
        if (isCyclicUtil(neighbor, visited, Some(node), graph)) return true
      } else if (parent.isEmpty || neighbor != parent.get) {
        return true
      }
      false
    }
  }

  def isCyclic(graph: Map[Int, List[Int]]): Boolean = {
    val visited = mutable.Set[Int]()

    // Check each node that hasn't been visited yet
    graph.keys.exists { node =>
      if (!visited.contains(node)) {
        if (isCyclicUtil(node, visited, None, graph)) return true
      }
      false
    }
  }
//
//  def main(args: Array[String]): Unit = {
//    val graph = FileParser.parseDotFile("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/DirectedRings/50.dot")
//    println(graph)
//    if (isCyclic(graph)) {
//      println("The graph contains a cycle.")
//    } else {
//      println("The graph does not contain a cycle.")
//    }
//  }
}

