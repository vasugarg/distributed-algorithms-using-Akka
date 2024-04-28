/*
 * Franklin Election Algorithm Implementation
 * ==========================================
 *
 * This Scala file contains the implementation of the Franklin Election Algorithm using the Akka actor model.
 * The algorithm operates in a distributed system where each node may become a leader based on the exchange of tokens.
 * The actors communicate with their left and right neighbors, sending and receiving tokens to determine the leader.
 *
 * - `FranklinAlgorithm` object: Encapsulates the behavior definitions for the nodes participating in the election.
 * - `FranklinAlgorithm` class: Represents the behavior of a single node participating in the election process.
 *
 * Note: This implementation leverages mutable state for tokens collection, which is encapsulated within actor state to
 * maintain the principles of the actor model.
 *
 * Author: Vasu Garg
 * Version: 1.0
 * Last Updated: April 28, 2024
 */

package cs553.Algorithms.Election.Franklin

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import cs553.Nodes

import scala.collection.mutable

object FranklinAlgorithm {

  sealed trait Direction
  case object Left extends Direction
  case object Right extends Direction

  def apply(id: Int): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new FranklinAlgorithm(context, id).setup(null, null, mutable.Set[(Int, Int)](), mutable.Set[(Int, Int)]())
  }
}

class FranklinAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"Franklin Algorithm actor id: $id created")

  import FranklinAlgorithm._
  import Nodes._

  // Method to check if the neighbors are set and activate the node or keep it in setup state.
  private def checkAndActivate(leftN: ActorRef[Nodes.Command],
                               rightN: ActorRef[Nodes.Command],
                               leftTokens: mutable.Set[(Int, Int)],
                               rightTokens: mutable.Set[(Int, Int)]
                              ): Behavior[Command] =
    if (leftN == null || rightN == null) {
      setup(leftN, rightN, leftTokens, rightTokens)
    }
    else {
      context.log.info(s"Node $id: Neighbors Set")
      leftN ! FranklinToken(id, 0, Left)
      rightN ! FranklinToken(id, 0, Right)
      active(leftN, rightN, leftTokens, rightTokens, currentRound = 0)
    }

  // Handles the initial setup state of the actor, waiting for neighbors to be assigned.
  private def setup(leftNode: ActorRef[Nodes.Command],
                    rightNode: ActorRef[Nodes.Command],
                    leftTokens: mutable.Set[(Int, Int)],
                    rightTokens: mutable.Set[(Int, Int)]
                   ): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetLeftNeighbor(leftNeighborRef) =>
        context.log.info(s"Node $id: Setting left neighbor to node with ActorRef: ${leftNeighborRef.path.name}")
        checkAndActivate(leftNeighborRef, rightNode, leftTokens, rightTokens)
      case SetRightNeighbor(rightNeighborRef) =>
        context.log.info(s"Node $id: Setting right neighbor to node with ActorRef: ${rightNeighborRef.path.name}")
        checkAndActivate(leftNode, rightNeighborRef, leftTokens, rightTokens)

      case FranklinToken(senderId, roundNumber, direction) =>
        context.log.debug(s"Node $id: Received Unknown Message")
        direction match {
          case Left => leftTokens += (roundNumber -> senderId)
          case Right => rightTokens += (roundNumber -> senderId)
        }
        setup(leftNode, rightNode, leftTokens, rightTokens)
    }
  }

  // Handles the active state of the actor, processing tokens and determining leadership.
  private def active (
                       leftNode: ActorRef[Nodes.Command],
                       rightNode: ActorRef[Nodes.Command],
                       leftTokens: mutable.Set[(Int, Int)],
                       rightTokens: mutable.Set[(Int, Int)],
                       currentRound: Int
                     ): Behavior[Command] =
    Behaviors.receiveMessage {

      case FranklinToken(senderId, roundNumber, direction) =>
        context.log.info(s"Round $currentRound: Node $id received token from $direction with ID $senderId with round $roundNumber")
        direction match {
          case Left => leftTokens += (roundNumber -> senderId)
          case Right => rightTokens += (roundNumber -> senderId)
        }

        if (leftTokens.exists(_._1 == currentRound) && rightTokens.exists(_._1 == currentRound)) {
          val leftSenderId = leftTokens.find(_._1 == currentRound).map(_._2)
          val rightSenderId = rightTokens.find(_._1 == currentRound).map(_._2)
          leftSenderId.zip(rightSenderId).map { case (leftId, rightId) =>
            context.log.info(s"Round $currentRound: Node $id evaluating between $leftId and $rightId at round $currentRound")
            val maxId = math.max(leftId, rightId)
            if (id > maxId) {
              leftTokens.filterInPlace(_._1 > currentRound)
              rightTokens.filterInPlace(_._1 > currentRound)
              val nextRound = currentRound + 1
              leftNode ! FranklinToken(id, nextRound, Left)
              rightNode ! FranklinToken(id, nextRound, Right)
              context.log.info(s"Round $currentRound: Node $id proceeds to $nextRound and sends token to $leftNode and $rightNode")
              active(leftNode, rightNode, leftTokens, rightTokens, nextRound) // Ensuring this returns Behavior[Nodes.Command]
            } else if (id < maxId) {
              leftTokens.filterInPlace(_._1 > currentRound)
              rightTokens.filterInPlace(_._1 > currentRound)
              context.log.info(s"Round $currentRound: Node $id LOST becoming PASSIVE with Right Tokens: $rightTokens and Left Tokens: $leftTokens")
              passive(leftNode, rightNode, leftTokens, rightTokens)
            } else {
              context.log.info(s"Round $currentRound: Node $id is the leader.")
              Behaviors.same[Nodes.Command]
            }
          }.getOrElse(Behaviors.same[Nodes.Command])
        } else {
          Behaviors.same[Nodes.Command]
        }
    }
  // Handles the passive state of the actor, which simply forwards the received token to its next neighbor.
  private def passive(leftNode: ActorRef[Nodes.Command],
                      rightNode: ActorRef[Nodes.Command],
                      leftTokens: mutable.Set[(Int, Int)],
                      rightTokens:mutable.Set[(Int, Int)]): Behavior[Command] = {

    // Clear the remaining unprocessed tokens
    def forwardTokens(): Unit = {
      leftTokens.foreach { case (round, senderId) =>
        context.log.info(s"Passive Node $id: Forwarding Message with ID: $senderId to target ${rightNode.path.name}")
        leftNode ! FranklinToken(senderId, round, Left)
      }
      rightTokens.foreach { case (round, senderId) =>
        context.log.info(s"Passive Node $id: Forwarding Message with ID: $senderId to target ${leftNode.path.name}")
        rightNode ! FranklinToken(senderId, round, Right)
      }
      leftTokens.clear()
      rightTokens.clear()
    }

    forwardTokens()

    Behaviors.receiveMessage {
      case FranklinToken(senderId, roundNumber, direction) =>

        val target = if (direction == Left) leftNode else rightNode
        context.log.info(s"Passive Node $id: Relaying Message with ID: $senderId to target ${target.path.name}")
        target ! FranklinToken(senderId, roundNumber, direction)
        forwardTokens()
        Behaviors.same
    }
  }
}





