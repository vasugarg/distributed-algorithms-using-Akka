package cs553.Utils

import scala.io.Source
import scala.util.matching.Regex

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
