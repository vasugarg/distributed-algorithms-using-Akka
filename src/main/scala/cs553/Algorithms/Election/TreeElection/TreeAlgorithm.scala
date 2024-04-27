package cs553.Algorithms.Election.TreeElection

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable
import scala.concurrent.duration._

object TreeAlgorithm {

  def apply(
             id: Int,
             isInitiator: Boolean
           ): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new TreeAlgorithm(context, id).setup(isInitiator)
    }
}


class TreeAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {

  context.log.info(s"Node $id created for Tree Election Algorithm")

  import Nodes._
  import TreeAlgorithm._

  private def setup(isInitiator: Boolean): Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetNeighbors(neighborsRef) =>
        context.log.info(s"Node $id: Setting neighbors as $neighborsRef")
        context.log.info(s"Node $id: Entering the wake up phase")
        if (isInitiator) {
          neighborsRef.foreach(_ ! WakeUpMessage)
          wakeUp(neighborsRef, 0)
        }
        else {
          wakeUp(neighborsRef, 0)
        }
    }
  }

  private def wakeUp(
                      neighbors: List[ActorRef[Nodes.Command]],
                      storedWakeUps: Int) : Behavior[Command] = {

    Behaviors.receiveMessage{
      case WakeUpMessage =>
        context.log.info(s"Node $id: Received wake up message")
        val count = storedWakeUps + 1
        if (count == neighbors.size) {
          context.log.info(s"Node $id: Woken Up post getting wake up messages from all the neighbors")
          Behaviors.same
        } else {
          neighbors.foreach(_ ! WakeUpMessage)
          wakeUp(neighbors, count)
        }

      case StartAlgorithm =>
        if (neighbors.size == 1) {
          context.log.info(s"Node $id: Leaf node sending wave messages to neighbor")
          context.scheduleOnce(3.seconds, neighbors.head, Wave(id, context.self))
          //neighbors.head ! Wave(id, context.self)
          active(neighbors, neighbors.head, mutable.Set[(Int, ActorRef[Nodes.Command])](), id)
        }
        else {
          active(neighbors, null, mutable.Set[(Int, ActorRef[Nodes.Command])](), -1)
        }

      case _ =>
        context.log.info(s"Node $id: Received an unknown message")
        Behaviors.unhandled
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
        context.log.info(s"Node $id: Received Information message with largest ID: $maxId")
        neighbors.filterNot(_ == parent).foreach(_ ! InformationMessage(id))
        Behaviors.same

      case _ =>
        context.log.info(s"Node $id: Received an unknown message")
        Behaviors.unhandled
    }
  }
}