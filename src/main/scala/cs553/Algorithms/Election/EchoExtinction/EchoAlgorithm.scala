/*
 * Echo Algorithm with Extinction Implementation
 * =============================================
 *
 * This Scala file contains the implementation of the Echo Election Algorithm using the Akka actor model.
 * The algorithm operates in a distributed system where each node may become a leader based on the exchange of tokens.
 * The actors communicate with their left and right neighbors, sending and receiving tokens to determine the leader.
 *
 * - `EchoAlgortihm` object: Encapsulates the behavior definitions for the nodes participating in the election.
 * - `EchoAlgortihm` class: Represents the behavior of a single node participating in the election process.
 *
 * Note: This implementation leverages mutable state for tokens collection, which is encapsulated within actor state to
 * maintain the principles of the actor model.
 *
 * Author: Vasu Garg
 * Version: 1.0
 * Last Updated: April 28, 2024
 */

package cs553.Algorithms.Election.EchoExtinction

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cs553.Nodes

import scala.collection.mutable


object EchoAlgorithm {
  def apply(id: Int): Behavior[Nodes.Command] =
    Behaviors.setup { context =>
      new EchoAlgorithm(context, id).setup(null, mutable.Set[(Int, ActorRef[Nodes.Command])]())
    }
}

class EchoAlgorithm(context: ActorContext[Nodes.Command], id: Int)  {
  context.log.info(s"Echo Algorithm actor id: $id created")

  import Nodes._

  private def setup(neighbors: List[ActorRef[Nodes.Command]],
                    storedWaves: mutable.Set[(Int, ActorRef[Nodes.Command])]) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case SetNeighbors(neighborsRef) =>
        context.log.info(s"Node $id: Setting neighbors")
        setup(neighborsRef, storedWaves)

      case StartAlgorithm =>
        neighbors.foreach { neighbor =>
          neighbor ! Wave(id, context.self)
        }
        active(neighbors, null, id, storedWaves)

      case Wave(tag, replyTo) =>
        context.log.info(s"Node $id: received a wave in setup phase, storing for further processing in Active state")
        storedWaves += (tag -> replyTo)
        setup(neighbors, storedWaves)
    }
  }

  private def active(
                      neighbors: List[ActorRef[Nodes.Command]],
                      parent: ActorRef[Nodes.Command],
                      caw: Int, // Current Active Wave
                      storedWaves: mutable.Set[(Int, ActorRef[Nodes.Command])]) : Behavior[Command] = {
    Behaviors.receiveMessage {
      case Wave(tag, replyTo) =>
        if (caw == -1) {
          context.log.info(s"Node $id: joins the first wave encountered: $tag")
          storedWaves += (tag -> replyTo)
          active(neighbors, replyTo, tag, storedWaves)
        }
        else if (tag > caw) {
          storedWaves.clear()
          context.log.info(s"Node $id: joins the higher wave $tag")
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
          context.log.info(s"Node $id: received wave with tag equal to the current participating wave")
          storedWaves += (tag -> replyTo)
          if (storedWaves.size == neighbors.size) {
            context.log.info(s"Node $id: received messages from all neighbors")
            if (id == caw) {
              context.log.info(s"Node $id: All neighbors have acknowledged, declaring self as leader.")
              Behaviors.same
//              neighbors.foreach(_ ! Terminate)
//              Behaviors.stopped(() => context.log.info(s"Node $id: terminating as leader."))
            }
            else {
              parent ! Wave(caw, context.self)
              Behaviors.same
            }
          }
          else {
            Behaviors.same
          }
        }
        else {
          Behaviors.same
        }
    }
  }
}