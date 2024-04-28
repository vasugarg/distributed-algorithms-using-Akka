package cs553.Algorithms.Election.TreeElection

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Nodes._
import cs553.Utils.FileParser

import scala.concurrent.duration._

object TreeElectionDriver {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFile("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/UndirectedGraphs/20.dot")
    println(s"$nodeMappings")

    runTreeElection(nodeMappings)
  }

  private def runTreeElection(nodeMappings:  Map[Int, List[Int]]): Unit = {
    val systemName = "TreeElection"
    ActorSystem(createActors(nodeMappings), systemName)
  }

  private def createActors(nodeMappings: Map[Int, List[Int]]): Behavior[Command] = {
    Behaviors.setup { context =>
      // Create Tree actors without setting their neighbors
      val nodeActors = nodeMappings.keys.map { nodeId =>
        val isInitiator = nodeId == 1
        val behavior = TreeAlgorithm(nodeId, isInitiator)
        nodeId -> context.spawn(behavior, s"node$nodeId")
      }.toMap


      nodeMappings.foreach { case (nodeId, neighborIds) =>
        val node = nodeActors(nodeId)
        val neighbors = neighborIds.map(neighborId => nodeActors.getOrElse(neighborId, context.system.ignoreRef))
        node ! SetNeighbors(neighbors)
      }
      nodeActors.values.foreach(_ ! StartWakeUp)
      nodeActors.values.foreach { node =>
        context.scheduleOnce(3.seconds, node, StartAlgorithm) // Wait for the nodes to complete WakeUp phase
      }
      Behaviors.same
    }
  }
}

