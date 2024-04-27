package cs553.Algorithms.Election.ChangRoberts

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors}
import cs553.Nodes

object CRAlgorithm {

  def apply(id: Int): Behavior[Nodes.Command] = Behaviors.setup { context =>
    new CRAlgorithm(context, id).setup()
  }
}

class CRAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"NodeActor $id created")

  import Nodes._

  private def setup(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetSuccessor(successorRef) =>
        context.log.info(s"Node $id: Setting successor to node with ActorRef: ${successorRef.path.name}")
        successorRef ! CRToken(id)
        active(successorRef)

      case _ =>
        Behaviors.unhandled
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
    }
  }

  private def passive(successor: ActorRef[Nodes.Command]):  Behavior[Command] = {
    Behaviors.receiveMessage{
      case CRToken(candidateId) =>
        context.log.info(s"Passive Node $id: Forwards the received $candidateId to successor")
        successor ! CRToken(candidateId)
        Behaviors.same

      case _ =>
        Behaviors.unhandled
    }
  }
}