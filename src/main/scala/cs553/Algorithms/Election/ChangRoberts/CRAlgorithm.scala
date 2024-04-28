/*
 * Chang-Roberts Algorithm Implementation
 * ==========================================
 *
 * This Scala file contains the implementation of the Chang Roberts Algorithm using the Akka actor model.
 * The algorithm operates in a distributed system where each node may become a leader based on the exchange of tokens.
 * The actors communicate with their left and right neighbors, sending and receiving tokens to determine the leader.
 *
 * - `CRAlgorithm` object: Encapsulates the behavior definitions for the nodes participating in the election.
 * - `CRAlgorithm` class: Represents the behavior of a single node participating in the election process.
 *
 * Note: This implementation leverages mutable state for tokens collection, which is encapsulated within actor state to
 * maintain the principles of the actor model.
 *
 * Author: Vasu Garg
 * Version: 1.0
 * Last Updated: April 28, 2024
 */

package cs553.Algorithms.Election.ChangRoberts

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors}
import cs553.Nodes

object CRAlgorithm {

  def apply(id: Int): Behavior[Nodes.Command] = Behaviors.setup { context =>
    new CRAlgorithm(context, id).setup(null)
  }
}

class CRAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"Chang-Roberts Algorithm actor id: $id created")

  import Nodes._

  private def setup(successor: ActorRef[Nodes.Command]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetSuccessor(successorRef) =>
        context.log.info(s"Node $id: Setting successor to node with ActorRef: ${successorRef.path.name}")
        setup(successorRef)

      case StartAlgorithm =>
        successor ! CRToken(id)
        active(successor)

      case _ =>
        Behaviors.same
    }
  }

  private def active(successor: ActorRef[Nodes.Command]): Behavior[Command] = {
    Behaviors.receiveMessage{
      case CRToken(candidateId) =>
        if (candidateId > id) {
          context.log.info(s"Active Node $id: Forwards the received $candidateId to successor and becomes Passive")
          successor ! CRToken(candidateId)
          passive(successor)
        }
        else if (candidateId < id) {
          context.log.info(s"Active Node $id: Discards the received $candidateId")
          Behaviors.same
        }
        else {
          context.log.info(s"Active Node $id: Becomes the leader")
          Behaviors.same
        }

      case _ =>
        context.log.info(s"Active Node $id: Received Unknown Token")
        Behaviors.same
    }
  }

  private def passive(successor: ActorRef[Nodes.Command]):  Behavior[Command] = {
    Behaviors.receiveMessage{
      case CRToken(candidateId) =>
        context.log.info(s"Passive Node $id: Forwards the received $candidateId to successor")
        successor ! CRToken(candidateId)
        Behaviors.same

      case _ =>
        Behaviors.same
    }
  }
}