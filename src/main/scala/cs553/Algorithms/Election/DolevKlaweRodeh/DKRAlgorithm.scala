/*
 * Dolev-Klawe-Rodeh Election Algorithm Implementation
 * ===================================================
 *
 * This Scala file contains the implementation of the Dolev-Klawe-Rodeh Election Algorithm using the Akka actor model.
 * The algorithm operates in a distributed system where each node may become a leader based on the exchange of tokens.
 * The actors communicate with their left and right neighbors, sending and receiving tokens to determine the leader.
 *
 * - `DKRAlgorithm` object: Encapsulates the behavior definitions for the nodes participating in the election.
 * - `DKRAlgorithm` class: Represents the behavior of a single node participating in the election process.
 *
 * Note: This implementation leverages mutable state for tokens collection, which is encapsulated within actor state to
 * maintain the principles of the actor model.
 *
 * Author: Vasu Garg
 * Version: 1.0
 * Last Updated: April 28, 2024
 */

package cs553.Algorithms.Election.DolevKlaweRodeh

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable

object DKRAlgorithm {

  def apply(id: Int): Behavior[Nodes.Command] = Behaviors.setup { context =>
    new DKRAlgorithm(context, id).setup(null, mutable.Set[(Int, Int)](), mutable.Set[(Int, Int)]())
  }
}

class DKRAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {

  context.log.info(s"Dolev-Klawe-Rodeh Algorithm actor id: $id created")

  import Nodes._

  private def setup(successor: ActorRef[Nodes.Command],
                    storedTokensN0: mutable.Set[(Int, Int)],
                    storedTokensN1: mutable.Set[(Int, Int)]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetSuccessor(successorRef) =>
        context.log.info(s"Node $id: Setting successor to node with ActorRef: ${successorRef.path.name}")
        setup(successorRef, storedTokensN0, storedTokensN1)


      case StartAlgorithm =>
        successor ! DKRToken(id, id, 0, 0)
        active(id, successor, 0, storedTokensN0, storedTokensN1)


      case DKRToken(senderId, electionId, roundNumber, n) =>
        context.log.info(s"Node $id: Received DKR Token in setup phase, storing the token for active state to process")
        if (n==0) {
          storedTokensN0 += (roundNumber -> electionId)
          setup(successor, storedTokensN0, storedTokensN1)
        }
        else if (n == 1) {
          storedTokensN1 += (roundNumber -> electionId)
          setup(successor, storedTokensN0, storedTokensN1)
        }
        else {
          Behaviors.same
        }
    }
  }

  private def active(
                      myID: Int,
                      successor: ActorRef[Nodes.Command],
                      currentRound: Int,
                      storedTokensN0: mutable.Set[(Int, Int)],
                      storedTokensN1: mutable.Set[(Int, Int)]
                    ): Behavior[Command] = {
    Behaviors.receiveMessage {
      case DKRToken(senderId, electionId, roundNumber, n) =>
        if (n == 0) {
          storedTokensN0 += (roundNumber -> electionId)
          successor ! DKRToken(myID, electionId, roundNumber, 1)
          active(myID,successor, currentRound, storedTokensN0, storedTokensN1)
        }
        else if (n == 1) {
          storedTokensN1 += (roundNumber -> electionId)
          (storedTokensN0.find(_._1 == currentRound).map(_._2), storedTokensN1.find(_._1 == currentRound).map(_._2)) match {
            case (Some(p), Some(q)) =>
              val maxID = math.max(myID, q)
              context.log.info(s"Round $currentRound: Node $myID Comparing p = $p and q = $q")
              if (p > maxID) {
                context.log.info(s"Round $currentRound: Node $id Proceeds to the next round with ID $p")
                storedTokensN0.clear()
                storedTokensN1.clear()
                successor ! DKRToken(p, p, currentRound + 1, 0)
                active(p, successor, currentRound + 1, storedTokensN0, storedTokensN1)
              }
              else if (p < maxID) {
                context.log.info(s"Round $currentRound: Node $myID Transitioning to passive state")
                storedTokensN0.filterInPlace { case (roundNumber, _) => roundNumber != currentRound }
                storedTokensN1.filterInPlace { case (roundNumber, _) => roundNumber != currentRound }
                passive(myID, successor, storedTokensN0, storedTokensN1)
              }
              else {
                context.log.info(s"Round $currentRound: Node $myID Is elected as the leader.")
                Behaviors.same
              }
            case _ =>
              context.log.info(s"Round $currentRound: Node $myID Waiting for more tokens to make a decision.")
              Behaviors.same
          }
        }
        else Behaviors.same
    }
  }

  private def passive(
                       myID: Int,
                       successor: ActorRef[Nodes.Command],
                       storedTokensN0: mutable.Set[(Int, Int)],
                       storedTokensN1: mutable.Set[(Int, Int)]
                     ): Behavior[Command] = {

        // Clear the remaining unprocessed tokens
        def forwardTokens(): Unit = {
          storedTokensN0.foreach { case (roundNumber, electionId) =>
            context.log.info(s"Passive Node $id: forwarding remaining n0 tokens to successor: ${successor.path.name}")
            successor ! DKRToken(myID, electionId, roundNumber, 0)
          }
          storedTokensN1.foreach { case (roundNumber, electionId) =>
            context.log.info(s"Passive Node $id: forwarding remaining n1 tokens to successor: ${successor.path.name}")
            successor ! DKRToken(myID, electionId, roundNumber, 1)
          }
          storedTokensN0.clear()
          storedTokensN1.clear()
        }

        forwardTokens()

    Behaviors.receiveMessage {
      case DKRToken(senderId, electionId, roundNumber, n) =>
        context.log.info(s"Passive Node $id: Relaying Message to its successor: ${successor.path.name}")
        successor ! DKRToken(id, electionId, roundNumber, n)
        forwardTokens()
        passive(myID, successor, storedTokensN0, storedTokensN1)
    }
  }
}