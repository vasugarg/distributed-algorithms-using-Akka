package cs553.Algorithms.Election.Franklin

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Nodes.{Command, SetLeftNeighbor, SetRightNeighbor}
import cs553.Utils.FileParser

object FranklinDriver {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFileWithOrientation("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/UndirectedRings/50.dot")
    println(s"$nodeMappings")

    runFranklinAlgorithm(nodeMappings)
  }

  def runFranklinAlgorithm(nodeMappings:  Map[Int, Map[String, Option[Int]]]): Unit = {
    val systemName = "FranklinSystem"
    ActorSystem(createActors(nodeMappings), systemName)
  }

  def createActors(nodeMappings: Map[Int, Map[String, Option[Int]]]): Behavior[Command] = {
    Behaviors.setup { context =>
      // Create FranklinAlgorithm actors without setting their neighbors
      val nodeActors = nodeMappings.keys.map { nodeId =>
        val behavior = FranklinAlgorithm(nodeId)
        nodeId -> context.spawn(behavior, s"node$nodeId")
      }.toMap

      // After all FranklinAlgorithm actors are created, then set their neighbors
      nodeMappings.foreach { case (nodeId, neighbors) =>
        val node = nodeActors(nodeId)
        val leftNeighbor = nodeActors(neighbors("Left").getOrElse(-1))
        val rightNeighbor = nodeActors(neighbors("Right").getOrElse(-1))
        node ! SetLeftNeighbor(leftNeighbor)
        node ! SetRightNeighbor(rightNeighbor)
      }
      Behaviors.same
    }
  }
}
