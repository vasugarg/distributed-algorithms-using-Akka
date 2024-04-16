package cs553

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import cs553.Algorithm.ChangRoberts
import cs553.Algorithms.Election.{DolevKlaweRodeh, Franklin}
import cs553.Nodes.{Command, InitiateNodes, SetLeftNeighbor, SetRightNeighbor, SetSuccessor, StartAlgorithm, StartElection}
import cs553.Utils.FileParser
import scala.concurrent.duration._

object Driver3 {
  def main(args: Array[String]): Unit = {
    val nodeMappings = FileParser.parseDotFileWithOrientation("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/random50UndirectedRing.dot")
    println(s"$nodeMappings")

    runFranklinAlgorithm(nodeMappings)
  }

  def runFranklinAlgorithm(nodeMappings:  Map[Int, Map[String, Option[Int]]]): Unit = {
    val systemName = "FranklinSystem"
    val system = ActorSystem(createRing(nodeMappings), systemName)
  }

  def createRing(nodeMappings: Map[Int, Map[String, Option[Int]]]): Behavior[Command] = {
    Behaviors.setup { context =>
      // Create Franklin actors without setting their neighbors
      val nodeActors = nodeMappings.keys.map { nodeId =>
        val behavior = Franklin(nodeId)
        nodeId -> context.spawn(behavior, s"node$nodeId")
      }.toMap

      // After all Franklin actors are created, then set their neighbors
      nodeMappings.foreach { case (nodeId, neighbors) =>
        val node = nodeActors(nodeId)
        val leftNeighbor = nodeActors(neighbors("Left").getOrElse(-1))
        val rightNeighbor = nodeActors(neighbors("Right").getOrElse(-1))
        node ! SetLeftNeighbor(leftNeighbor)
        node ! SetRightNeighbor(rightNeighbor)
      }
      Behaviors.same

      // Nested Behaviors.withTimers inside Behaviors.setup
//      Behaviors.withTimers { timers =>
//        timers.startSingleTimer(StartAlgorithm, StartAlgorithm, 3.seconds)
//        Behaviors.receiveMessage {
//          case StartAlgorithm =>
//            nodeActors.values.foreach(_ ! StartAlgorithm)
//            Behaviors.same
//          case _ => Behaviors.unhandled
//        }
//      }
    }
  }
}


//package cs553
//
//import akka.actor.typed.ActorRef
//import akka.actor.typed.scaladsl.Behaviors
//import akka.actor.typed.{ActorSystem, Behavior}
//import cs553.Algorithms.Election.Franklin
//import cs553.Nodes.{Command, SetLeftNeighbor, SetRightNeighbor, StartAlgorithm, StartElection}
//import cs553.Utils.FileParser
//
//object Driver3 {
//  def main(args: Array[String]): Unit = {
//    // Assuming parseDotFileWithOrientation returns a Map[Int, (Int, Int)] for undirected ring
//    val nodeMappings = FileParser.parseDotFileWithOrientation("/Users/vasugarg/Documents/Github/CS553/DistributedAlgorithms/DistributedAlgorithms/inputs/random5UndirectedGraph.dot")
//
//    runFranklinAlgorithm(nodeMappings)
//  }
//
//  def runFranklinAlgorithm(nodeMappings: Map[Int, (Option[Int], Option[Int])]): Unit = {
//    val systemName = "FranklinSystem"
//    val system: ActorSystem[Command] = ActorSystem(createRing(nodeMappings), systemName)
//
//    // Assuming we have an actor that can initiate the algorithm, typically the first node.
//    val firstNodeId = nodeMappings.keys.min
//    system ! StartElection(firstNodeId)
//  }
//
//  def createRing(nodeMappings: Map[Int, (Option[Int], Option[Int])]): Behavior[Command] = Behaviors.setup { context =>
//    val nodeActors: Map[Int, ActorRef[Command]] = nodeMappings.keys.map { nodeId =>
//      val behavior = Franklin(nodeId)
//      nodeId -> context.spawn(behavior, s"node$nodeId")
//    }.toMap
//
//    nodeMappings.foreach { case (nodeId, (leftNeighborID, rightNeighborID)) =>
//      val node = nodeActors(nodeId)
//      val leftNeighbor = nodeActors(leftNeighborID.get)
//      val rightNeighbor = nodeActors(rightNeighborID.get)
//
//      node ! SetLeftNeighbor(leftNeighbor)
//      node ! SetRightNeighbor(rightNeighbor)
//      // Do not start the election here; it will be initiated by the system
//    }
//
//    Behaviors.receiveMessage {
//      case StartElection(nodeId) =>
//        nodeActors.get(nodeId).foreach(_ ! StartAlgorithm)
//        Behaviors.same
//      case _ => Behaviors.unhandled
//    }
//  }
//}
