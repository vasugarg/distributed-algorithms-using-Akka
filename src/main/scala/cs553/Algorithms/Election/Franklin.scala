package cs553.Algorithms.Election

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable

object Franklin {

  sealed trait Direction
  case object Left extends Direction
  case object Right extends Direction

  def apply(id: Int): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new Franklin(context, id).setup(null, null)
  }
}

class Franklin(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"NodeActorFranklin $id created")

  import Nodes._
  import Franklin._

  private def checkAndActivate(leftN: ActorRef[Nodes.Command], rightN: ActorRef[Nodes.Command]): Behavior[Command] =
    if (leftN == null || rightN == null) {
      setup(leftN, rightN)
    }
    else {
      context.log.info(s"Node $id Neighbors Set")
      leftN ! Token(id, 0, Left)
      rightN ! Token(id, 0, Right)
      active(leftN, rightN,mutable.Set[(Int, Int)](), mutable.Set[(Int, Int)](), currentRound = 0)
    }

  private def setup(leftNode: ActorRef[Nodes.Command], rightNode: ActorRef[Nodes.Command]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetLeftNeighbor(leftNeighborRef) =>
        context.log.info(s"Node $id: Setting left neighbor to node with ActorRef: ${leftNeighborRef.path.name}")
        checkAndActivate(leftNeighborRef, rightNode)
      case SetRightNeighbor(rightNeighborRef) =>
        context.log.info(s"Node $id: Setting right neighbor to node with ActorRef: ${rightNeighborRef.path.name}")
        checkAndActivate(leftNode, rightNeighborRef)
    }
  }

  private def active (
                       leftNode: ActorRef[Nodes.Command],
                       rightNode: ActorRef[Nodes.Command],
                       leftTokens: mutable.Set[(Int, Int)],
                       rightTokens: mutable.Set[(Int, Int)],
                       currentRound: Int
                     ): Behavior[Command] =
    Behaviors.receiveMessage {

      case Token(senderId, roundNumber, direction) =>
        context.log.info(s"Node $id received Token from $direction with ID $senderId with round $roundNumber")
        direction match {
          case Left => leftTokens += (roundNumber -> senderId)
          case Right => rightTokens += (roundNumber -> senderId)
        }

        if (leftTokens.exists(_._1 == currentRound) && rightTokens.exists(_._1 == currentRound)) {
          val leftSenderId = leftTokens.find(_._1 == currentRound).map(_._2)
          val rightSenderId = rightTokens.find(_._1 == currentRound).map(_._2)
          leftSenderId.zip(rightSenderId).headOption.map { case (leftId, rightId) =>
            context.log.info(s"Round $currentRound --> Node $id evaluating between $leftId and $rightId at round $currentRound")
            val maxId = math.max(leftId, rightId)
            if (id > maxId) {
              leftTokens.filterInPlace(_._1 > currentRound)
              rightTokens.filterInPlace(_._1 > currentRound)
              val nextRound = currentRound + 1
              leftNode ! Token(id, nextRound, Left)
              rightNode ! Token(id, nextRound, Right)
              context.log.info(s"Round $currentRound --> Node $id proceeds to $nextRound and sends token to $leftNode and $rightNode")
              active(leftNode, rightNode, leftTokens, rightTokens, nextRound) // Ensuring this returns Behavior[Nodes.Command]
            } else if (id < maxId) {
              leftTokens.filterInPlace(_._1 > currentRound)
              rightTokens.filterInPlace(_._1 > currentRound)
              context.log.info(s"Round $currentRound --> Node $id LOST becoming PASSIVE with Right Tokens: $rightTokens and Left Tokens: $leftTokens")
              passive(leftNode, rightNode, leftTokens, rightTokens)
            } else {
              context.log.info(s"Round $currentRound --> Node $id is the leader.")
              Behaviors.same[Nodes.Command]
            }
          }.getOrElse(Behaviors.same[Nodes.Command])
        } else {
          context.log.info(s"Node $id Left Tokes: $leftTokens and Right Tokens: $rightTokens")
          Behaviors.same[Nodes.Command]
        }
    }

  private def passive(leftNode: ActorRef[Nodes.Command],
                      rightNode: ActorRef[Nodes.Command],
                      leftTokens: mutable.Set[(Int, Int)],
                      rightTokens:mutable.Set[(Int, Int)]): Behavior[Command] = {

    def forwardTokens(): Unit = {
      leftTokens.foreach { case (round, senderId) =>
        context.log.info(s"Node $id Forwarding Message with ID: $senderId to target ${rightNode.path}")
        leftNode ! Token(senderId, round, Left)
      }
      rightTokens.foreach { case (round, senderId) =>
        context.log.info(s"Node $id Forwarding Message with ID: $senderId to target ${leftNode.path}")
        rightNode ! Token(senderId, round, Right)
      }
      leftTokens.clear()
      rightTokens.clear()
    }

    forwardTokens()

    Behaviors.receiveMessage {
      case Token(senderId, roundNumber, direction) =>

        val target = if (direction == Left) leftNode else rightNode
        context.log.info(s"Node $id Relaying Message with ID: $senderId to target ${target.path}")
        target ! Token(senderId, roundNumber, direction)
        forwardTokens()
        Behaviors.same
    }
  }
}





