package cs553.Algorithms.Election.EchoExtinction

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable


object EchoAlgortihm {
  def apply(id: Int): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new EchoAlgortihm(context, id).setup()
    }
}

class EchoAlgortihm(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"NodeActorEcho $id created")

  import Nodes._

  private def setup() : Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetNeighbors(neighborsRef) =>
        context.log.info(s"Node $id: Setting neighbors")
        neighborsRef.foreach {neighbor =>
          neighbor ! Wave(id, context.self)
        }
        active(neighborsRef, null, id, mutable.Set[(Int, ActorRef[Nodes.Command])]())
    }
  }

  private def active(
                      neighbors: List[ActorRef[Nodes.Command]],
                      parent: ActorRef[Nodes.Command],
                      caw: Int,
                      storedWaves: mutable.Set[(Int, ActorRef[Nodes.Command])]) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case Wave(tag, replyTo) =>
        if (caw == -1) {
          context.log.info(s"Node $id: joins wave $tag")
          storedWaves += (tag -> replyTo)
          active(neighbors, replyTo, tag, storedWaves)
        }
        else if (tag > caw) {
          storedWaves.clear()
          context.log.info(s"Node $id: joins higher wave $tag")
          storedWaves += (tag -> replyTo)
          val filteredNeighbors = neighbors.filterNot(_ == replyTo)
          filteredNeighbors.foreach(_ ! Wave(tag, context.self))
          active(neighbors, replyTo, tag, storedWaves)
        }
        else if (caw > tag) {
          context.log.info(s"Node $id: ignores wave: $tag")
          Behaviors.same
        }
        else if (caw == tag) {
          context.log.info(s"Node $id: received tag equal to caw")
          storedWaves += (tag -> replyTo)
          if (storedWaves.size == neighbors.size) {
            context.log.info(s"Node $id: received messages from all neighbors")
            if (id == caw) {
              context.log.info(s"Node $id: All neighbors have acknowledged, declaring self as leader.")
            }
            else {
              parent ! Wave(caw, context.self)
            }
          }
        }
        Behaviors.same
    }
  }
}