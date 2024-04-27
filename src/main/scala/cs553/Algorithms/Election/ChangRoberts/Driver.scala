package cs553.Algorithms.Election.ChangRoberts

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Nodes.{Command, SetSuccessor}
import cs553.Utils.FileParser

object Driver {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFile("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/DirectedRings/random50DirectedRing.dot")
    println(s"$nodeMappings")

    runCRAlgorithm(nodeMappings)
  }

  private def runCRAlgorithm(nodeMappings:  Map[Int, List[Int]]): Unit = {
    val systemName = "CRSystem"
    ActorSystem(createRing(nodeMappings), systemName)
  }

  def createRing(nodeMappings: Map[Int, List[Int]]): Behavior[Command] = {
    Behaviors.setup { context =>
      // Create CRAlgorithm actors without setting their neighbors
      val nodeActors = nodeMappings.keys.map { nodeId =>
        val behavior = CRAlgorithm(nodeId)
        nodeId -> context.spawn(behavior, s"node$nodeId")
      }.toMap

      // After all the actors are created, then set their neighbors
      nodeMappings.foreach { case (nodeId, successorId) =>
        val node = nodeActors(nodeId)
        val successor = nodeActors.getOrElse(successorId.head, context.system.ignoreRef)
        node ! SetSuccessor(successor)
      }
      Behaviors.same
    }
  }
}

