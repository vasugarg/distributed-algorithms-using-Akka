package cs553.Utils

import scala.io.Source
import scala.util.Using
import scala.util.matching.Regex

//object FileParser {
//  // Regular expression to extract node IDs and weights from the .dot file lines
//  private val edgePattern: Regex = """"(\d+)" -> "(\d+)" \["weight"="(\d+\.\d+)"\]""".r
//
//  def parseDotFile(filePath: String): Map[Int, Int] = {
//    Using(Source.fromFile(filePath)) { source =>
//      source.getLines().foldLeft(Map.empty[Int, Int]) {
//        case (acc, edgePattern(from, to, _)) =>
//          acc + (from.toInt -> to.toInt)
//        case (acc, _) => acc
//      }
//    }.getOrElse(Map.empty)
//  }
//}

object FileParser {
  def parseDotFile(filePath: String): Map[Int, List[Int]] = {
    val edgePattern: Regex = """"(\d+)"\s*(->|--)\s*"(\d+)"\s*\[\s*"weight"\s*=\s*"(\d+\.\d+)"\s*\]""".r
    val source = Source.fromFile(filePath)

    val edges = source.getLines().collect {
      case edgePattern(from, edgeType, to, _) if edgeType == "->" =>
        // Directed edge: from -> to
        Seq((from.toInt, to.toInt))
      case edgePattern(from, edgeType, to, _) if edgeType == "--" =>
        // Undirected edge: treat as bidirectional
        Seq((from.toInt, to.toInt), (to.toInt, from.toInt))
    }.flatten.toList

    source.close()

    // Accumulate edges in a map, where each node maps to a list of connected nodes
    edges.foldLeft(Map.empty[Int, List[Int]]) { (acc, edge) =>
      val (from, to) = edge

      // Update the map for 'from' node to include 'to' node, avoiding duplication
      val updatedFrom = acc.getOrElse(from, List.empty) :+ to

      acc + (from -> updatedFrom)
    }
  }

//  def parseDotFileWithOrientation(filePath: String): Map[Int, (Option[Int], Option[Int])] = {
//    val edgePattern: Regex = """"(\d+)"\s*(--)\s*"(\d+)"\s*\[\s*"weight"\s*=\s*"(\d+\.\d+)"\s*\]""".r
//    val source = Source.fromFile(filePath)
//    val edges = source.getLines().collect {
//      case edgePattern(from, _, to, _) => (from.toInt, to.toInt)
//    }.toList
//    source.close()
//
//    // This map tracks the first and last connection for each node
//    var connectionsMap = Map.empty[Int, (Option[Int], Option[Int])]
//
//    edges.foreach { case (from, to) =>
//      // For 'from' node, update connections
//      val fromConnections = connectionsMap.getOrElse(from, (None, None))
//      connectionsMap = connectionsMap.updated(from, (fromConnections._1.orElse(Some(to)), Some(to)))
//
//      // For 'to' node, update connections
//      val toConnections = connectionsMap.getOrElse(to, (None, None))
//      connectionsMap = connectionsMap.updated(to, (toConnections._1.orElse(Some(from)), Some(from)))
//    }
//
//    // Adjusting the map to ensure the correct order of connections based on the requirement
//    val finalMap = connectionsMap.map { case (node, (first, last)) =>
//      if (first == last) {
//        (node, (first, None)) // If only one connection, there's no right neighbor
//      } else {
//        (node, (last, first)) // Swap to ensure the last connection is the left neighbor
//      }
//    }
//
//    finalMap
//  }

  def parseDotFileWithOrientation(filePath: String): Map[Int, Map[String, Option[Int]]] = {
    val edgePattern: Regex = """"(\d+)"\s*--\s*"(\d+)"\s*\[\s*"weight"\s*=\s*"(\d+\.\d+)"\s*\]""".r
    val source = Source.fromFile(filePath)

    // Use a mutable map for convenience in updating entries
    val neighborsMap = scala.collection.mutable.Map[Int, Map[String, Option[Int]]]()

    source.getLines().foreach {
      case edgePattern(left, right, _) =>
        val leftInt = left.toInt
        val rightInt = right.toInt

        // For the left node, update or set its right neighbor
        val leftEntry = neighborsMap.getOrElse(leftInt, Map("Left" -> None, "Right" -> None))
        neighborsMap(leftInt) = leftEntry + ("Right" -> Some(rightInt))

        // For the right node, update or set its left neighbor
        val rightEntry = neighborsMap.getOrElse(rightInt, Map("Left" -> None, "Right" -> None))
        neighborsMap(rightInt) = rightEntry + ("Left" -> Some(leftInt))

      case _ => // Ignore lines that do not match the edge pattern
    }

    source.close()

    neighborsMap.toMap
  }
}
