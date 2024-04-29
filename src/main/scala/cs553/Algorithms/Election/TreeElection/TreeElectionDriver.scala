package cs553.Algorithms.Election.TreeElection

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.config.{Config, ConfigFactory}
import cs553.Nodes._
import cs553.Utils.{CreateLogger, FileParser}
import org.slf4j.Logger

import scala.concurrent.duration._

object TreeElectionDriver {

  val config: Config = ConfigFactory.load("application.conf")
  val logger: Logger = CreateLogger(classOf[TreeElectionDriver.type])
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFile(config.getString("ElectionAlgorithms.inputFile.TreeAlgorithm"))
    logger.info(s"$nodeMappings")

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

