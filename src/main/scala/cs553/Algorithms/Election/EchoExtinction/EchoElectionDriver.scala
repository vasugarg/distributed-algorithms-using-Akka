package cs553.Algorithms.Election.EchoExtinction

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Nodes.{Command, SetNeighbors, StartAlgorithm}
import cs553.Utils.FileParser

object EchoElectionDriver {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFile("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/UndirectedGraphs/3.dot")
    println(s"$nodeMappings")

    runEchoAlgorithm(nodeMappings)
  }

  private def runEchoAlgorithm(nodeMappings:  Map[Int, List[Int]]): Unit = {
    val systemName = "EchoExtinction"
    ActorSystem(createActors(nodeMappings), systemName)
  }

  def createActors(nodeMappings: Map[Int, List[Int]]): Behavior[Command] = {
    Behaviors.setup { context =>
      // Create Echo actors without setting their neighbors
      val nodeActors = nodeMappings.keys.map { nodeId =>
        val behavior = EchoAlgorithm(nodeId)
        nodeId -> context.spawn(behavior, s"node$nodeId")
      }.toMap

      // After all Echo actors are created, then set their neighbors
      nodeMappings.foreach { case (nodeId, neighborIds) =>
        val node = nodeActors(nodeId)
        val neighbors = neighborIds.map(neighborId => nodeActors.getOrElse(neighborId, context.system.ignoreRef))
        node ! SetNeighbors(neighbors)
      }
      nodeActors.values.foreach(_ ! StartAlgorithm)
      Behaviors.same
    }
  }
}
