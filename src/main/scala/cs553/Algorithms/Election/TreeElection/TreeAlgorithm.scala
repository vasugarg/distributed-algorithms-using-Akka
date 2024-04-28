/*
 * Tree Election Algorithm Implementation
 * =======================================
 *
 * This Scala file contains the implementation of the Tree Election Algorithm using the Akka actor model.
 * The algorithm operates in a distributed system where each node may become a leader based on the exchange of tokens.
 * The actors communicate with their left and right neighbors, sending and receiving tokens to determine the leader.
 *
 * - `TreeAlgorithm` object: Encapsulates the behavior definitions for the nodes participating in the election.
 * - `TreeAlgorithm` class: Represents the behavior of a single node participating in the election process.
 *
 * Note: This implementation leverages mutable state for tokens collection, which is encapsulated within actor state to
 * maintain the principles of the actor model.
 *
 * Author: Vasu Garg
 * Version: 1.0
 * Last Updated: April 28, 2024
 */

package cs553.Algorithms.Election.TreeElection

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable
object TreeAlgorithm {

  def apply(
             id: Int,
             isInitiator: Boolean
           ): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new TreeAlgorithm(context, id).setup(null,isInitiator)
    }
}


class TreeAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {

  context.log.info(s"Tree Election Algorithm actor id: $id created")

  import Nodes._
  import TreeAlgorithm._

  private def setup(neighbors: List[ActorRef[Nodes.Command]], isInitiator: Boolean): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetNeighbors(neighborsRef) =>
        context.log.info(s"Node $id: Setting neighbors as $neighborsRef")
        setup(neighborsRef, isInitiator)

      case StartWakeUp =>
        context.log.info(s"Node $id: Entering the wake up phase")
        if (isInitiator) {
          neighbors.foreach(_ ! WakeUpMessage)
          wakeUp(neighbors, 0, mutable.Set[(Int, ActorRef[Nodes.Command])]())
        }
        else {
          wakeUp(neighbors, 0, mutable.Set[(Int, ActorRef[Nodes.Command])]())
        }
    }
  }

  private def wakeUp(
                      neighbors: List[ActorRef[Nodes.Command]],
                      storedWakeUps: Int,
                      storedWaves: mutable.Set[(Int, ActorRef[Nodes.Command])]) : Behavior[Command] = {

    Behaviors.receiveMessage{
      case WakeUpMessage =>
        context.log.info(s"Node $id: Received wake up message")
        val count = storedWakeUps + 1
        if (count == neighbors.size) {
          context.log.info(s"Node $id: Woken Up post getting wake up messages from all the neighbors")
          Behaviors.same
        } else {
          neighbors.foreach(_ ! WakeUpMessage)
          wakeUp(neighbors, count, storedWaves)
        }

      case StartAlgorithm =>
        if (neighbors.size == 1) {
          context.log.info(s"Node $id: Leaf node sending wave messages to neighbor")
          neighbors.head ! Wave(id, context.self)
          active(neighbors, neighbors.head, storedWaves, id)
        }
        else {
          active(neighbors, null, storedWaves, -1)
        }

      case Wave(q, replyTo) =>
        context.log.info(s"Node $id: Received a Wave in WakeUp phase, storing it for further processing in Active state")
        storedWaves += (q -> replyTo)
        wakeUp(neighbors, storedWakeUps, storedWaves)
    }
  }

  private def active(
                      neighbors: List[ActorRef[Nodes.Command]],
                      parent: ActorRef[Nodes.Command],
                      storedWaves: mutable.Set[(Int, ActorRef[Nodes.Command])],
                      storedID: Int) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case Wave(q, replyTo) =>
        context.log.info(s"Node $id: Received a wave message from: $q")
        storedWaves += (q -> replyTo)
        if (storedWaves.size == neighbors.size - 1 && parent == null) {
          val maxID = math.max(storedWaves.maxBy(_._1)._1, id)
          val respondedNeighbors = storedWaves.map(_._2).toSet
          val newParent = neighbors.find(!respondedNeighbors.contains(_)).get
          context.log.info(s"Node $id: Setting the node ${newParent.path.name} as its parent")
          newParent ! Wave(maxID, context.self)
          active(neighbors, newParent, storedWaves, maxID)
        }
        else if (parent == replyTo) {
          val maxId_ = math.max(q, storedID)
          context.log.info(s"Node $id and Maximum: $maxId_")
          neighbors.filterNot(_ == parent).foreach(_ ! InformationMessage(maxId_))
          Behaviors.same
        }
        else {
          Behaviors.same
        }

      case InformationMessage(maxId) =>
        context.log.info(s"Node $id: Received information message with leader ID: $maxId")
        neighbors.filterNot(_ == parent).foreach(_ ! InformationMessage(id))
        Behaviors.same

      case _ =>
        context.log.info(s"Node $id: Received an unknown message")
        Behaviors.same
    }
  }
}