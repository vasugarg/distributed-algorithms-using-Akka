package cs553.Algorithms.Election.DolevKlaweRodeh

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Nodes.{Command, DKRToken, SetSuccessor, StartAlgorithm}
import cs553.Utils.FileParser

object DKRDriver {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFile("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/DirectedRings/50.dot")

    runDolevKlaweRodehAlgorithm(nodeMappings)
  }

  private def runDolevKlaweRodehAlgorithm(nodeMappings: Map[Int, List[Int]]): Unit = {
    val systemName = "DolevKlaweRodehSystem"
    ActorSystem(createActors(nodeMappings), systemName)
  }

  def createActors(nodeMappings: Map[Int, List[Int]]): Behavior[Command] = Behaviors.setup { context =>
    val nodeActors = nodeMappings.keys.map { nodeId =>
      val behavior = DKRAlgorithm.apply(nodeId)
      nodeId -> context.spawn(behavior, s"node$nodeId")
    }.toMap

    nodeMappings.foreach { case (nodeId, successorId) =>
      val node = nodeActors(nodeId)
      val successor = nodeActors.getOrElse(successorId.head, context.system.ignoreRef)
      node ! SetSuccessor(successor)
      //successor ! DKRToken(nodeId, nodeId, 0, 0)
    }
    nodeActors.values.foreach(_ ! StartAlgorithm)

    Behaviors.empty
  }
}
