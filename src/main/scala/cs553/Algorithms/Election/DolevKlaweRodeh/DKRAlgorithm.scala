package cs553.Algorithms.Election.DolevKlaweRodeh

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable

object DKRAlgorithm {

  def apply(id: Int): Behavior[Nodes.Command] = Behaviors.setup { context =>
    new DKRAlgorithm(context, id).setup()
  }
}

class DKRAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {

  context.log.info(s"Actor Node created for id: $id")

  import Nodes._

  private def setup(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetSuccessor(successorRef) =>
        context.log.info(s"Node $id: Setting successor to node with ActorRef: ${successorRef.path.name}")
        successorRef ! DKRToken(id, id, 0, 0)
        active(id, successorRef, 0, mutable.Set[(Int, Int)](), mutable.Set[(Int, Int)]())
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
          Behaviors.same
        }
        else if (n == 1) {
          storedTokensN1 += (roundNumber -> electionId)
          (storedTokensN0.find(_._1 == currentRound).map(_._2), storedTokensN1.find(_._1 == currentRound).map(_._2)) match {
            case (Some(p), Some(q)) =>
              val maxID = math.max(myID, q)
              context.log.info(s"Active Node $myID: Comparing p = $p and q = $q")
              if (p > maxID) {
                context.log.info(s"Active Node $id: Proceeds to the next round with ID $p")
                storedTokensN0.clear()
                storedTokensN1.clear()
                successor ! DKRToken(p, p, currentRound + 1, 0)
                active(p, successor, currentRound + 1, storedTokensN0, storedTokensN1)
              }
              else if (p < maxID) {
                context.log.info(s"Active Node $myID: Transitioning to passive state")
                passive(myID, successor, storedTokensN0, storedTokensN1)
              }
              else {
                context.log.info(s"Active Node $myID: Is elected as the leader.")
                Behaviors.same
              }
            case _ =>
              context.log.info(s"Active Node $myID:  Waiting for more tokens to make a decision.")
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
        Behaviors.same
    }
  }
}