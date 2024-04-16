package cs553


import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import cs553.Algorithms.Election.Franklin.Direction
import cs553.Algorithms.Election.{ChangRoberts, DolevKlaweRodeh, EchoExtinction}
import akka.actor.typed.receptionist.ServiceKey

object Nodes {
  def applyCRAlgorithm(id: Int): Behavior[Command] =
    Behaviors.setup(context => new ChangRoberts(context, id))

  def applyFranklinAlgorithm(id: Int): Behavior[Command] =
    Behaviors.setup(context => new FranklinAlgorithm(context, id))

//  def applyDolevKlawRodeh(id: Int): Behavior[Command] =
//    Behaviors.setup(context => new DolevKlaweRodeh(context, id, true))

  def applyEchoExtinction(id: Int): Behavior[Command] =
    Behaviors.setup(context => new EchoExtinction(context, id))

  sealed trait Command

  case class ElectionMessage(id: Int) extends Command

  case class Coordinator(id: Int) extends Command

  case class SetSuccessor(successorRef: ActorRef[Command]) extends Command

  case class SetNeighbors(predecessorRef: ActorRef[Command], successorRef: ActorRef[Command]) extends Command

  case class SetLeftNeighbor(leftNeighborRef: ActorRef[Command]) extends Command

  case class SetRightNeighbor(rightNeighborRef: ActorRef[Command]) extends Command

  case class SetNeighbor(neighborRef: List[ActorRef[Command]]) extends Command

  case class StartElection(initiatorId: Int) extends Command

  case class ElectionMessage2(id: Int, parity: Boolean, direction: Direction) extends Command

  case class InitializeElection(parity: Boolean) extends Command

  case object BecomePassive extends Command

  case class DeclareLeader(id: Int) extends Command

  case class Wave(wave: Int) extends Command

  final case object Decide extends Command

  final case object WakeUpMessage extends Command

  case class TokMessage(r: Int, replyTo: ActorRef[Command]) extends Command

  case class LdrMessage(r: Int) extends Command

  case class InitiateElection(initiator: Boolean) extends Command

  case class InitiateNodes(id: Int) extends Command

  case class CompareIds() extends Command

  case class NeighbId(id: Int, n: Int, b: Boolean) extends Command

  case class One(cip: Int) extends Command

  case class Two(acnp: Int) extends Command

  case class Smal(acnp: Int) extends Command

  case class WinnerResponse(winnerId: Int)

  case class GetWinner(replyTo: ActorRef[WinnerResponse]) extends Command

  case object StartAlgorithm extends Command

  case class DolevKlaweRodehCommand(command: Any) extends Command

  case class ReceivedRightId(id: Int) extends Command

  case class ReceivedLeftId(id: Int) extends Command

  case object InitializationComplete extends Command

  case class NodeReady(nodeRef: ActorRef[Command]) extends Command

  case object StartElection extends Command

  case class Token(senderId: Int, roundNumber: Int, direction: Direction) extends Command

  case class DKRToken(senderId: Int, electionId: Int, roundNumber: Int, n: Int) extends Command

  val FranklinKey: ServiceKey[Command] = ServiceKey[Command]("FranklinKey")

}

//class ChangRobersAlgorithm(context: ActorContext[Nodes.Command], id: Int) extends AbstractBehavior[Nodes.Command](context) {
//
//  context.log.info(s"NodeActor $id created")
//
//  import Nodes._
//
//  private var successor: ActorRef[Nodes.Command] = context.self
//
//  override def onMessage(msg: Nodes.Command): Behavior[Nodes.Command] = msg match {
//
//    case SetSuccessor(successorRef) =>
//      successor = successorRef
//      Behaviors.same
//
//    case StartElection(_) =>
//      context.log.info(s"Node $id initiates the election.")
//      successor ! ElectionMessage(id)
//      Behaviors.same
//
//    case ElectionMessage(candidateId) =>
//      if (candidateId > id) {
//        context.log.info(s"Node $id forwards the election message from $candidateId to its successor.")
//        successor ! ElectionMessage(candidateId)
//      } else if (candidateId < id) {
//        context.log.info(s"Node $id discards the election message from $candidateId.")
//      } else {
//        context.log.info(s"Node $id is the leader")
//        successor ! Coordinator(id)
//      }
//      this
//
//    case Coordinator(leaderId) =>
//      context.log.info(s"Node $id acknowledges $leaderId as the leader.")
//      if (leaderId != id) {
//        successor ! Coordinator(leaderId) // Forward the coordinator message if this node is not the leader
//      }
//      Behaviors.same
//
//    case _ => Behaviors.unhandled
//  }
//
//  override def onSignal: PartialFunction[Signal, Behavior[Nodes.Command]] = {
//    case PostStop =>
//      context.log.info(s"NodeActor $id stopped")
//      this
//  }
//}


class FranklinAlgorithm(context: ActorContext[Nodes.Command], id: Int) extends AbstractBehavior[Nodes.Command](context) {
  import Nodes._

  private var predecessor: ActorRef[Nodes.Command] = context.self
  private var successor: ActorRef[Nodes.Command] = context.self
  private var receivedFromPredecessor: Option[Int] = None
  private var receivedFromSuccessor: Option[Int] = None


  def decideBasedOnNeighbors(): Behavior[Nodes.Command] = {
    (receivedFromPredecessor, receivedFromSuccessor) match {
      case (Some(predId), Some(succId)) =>
        val maxNeighborId = Math.max(predId, succId)
        if (maxNeighborId < id) {
          // max{q, r} < p: p enters another election round
          context.log.info(s"Node $id, with neighbors $predId and $succId, continues to the next round.")
          predecessor ! ElectionMessage(id)
          successor ! ElectionMessage(id)
          Behaviors.same
        } else if (maxNeighborId > id) {
          // max{q, r} > p: p becomes passive
          context.log.info(s"Node $id becomes passive; a neighbor has a larger ID.")
          Behaviors.same
        } else {
          // max{q, r} = p: p becomes the leader (this case might need adjustment based on algorithm specifics)
          context.log.info(s"Node $id is the leader.")
          predecessor ! Coordinator(id)
          successor ! Coordinator(id)
          Behaviors.same
        }
      case _ =>
        // Not all information from neighbors received yet; wait for more messages
        Behaviors.same
    }
  }

  override def onMessage(msg: Nodes.Command): Behavior[Nodes.Command] = msg match {
    case SetNeighbors(predecessorRef, successorRef) =>
      predecessor = predecessorRef
      successor = successorRef
      Behaviors.same

    case StartElection(_) =>
      context.log.info(s"Node $id starts an election round.")
      predecessor ! ElectionMessage(id)
      successor ! ElectionMessage(id)
      Behaviors.same

    case ElectionMessage(candidateId) =>
      if (context.self == predecessor) {
        receivedFromPredecessor = Some(candidateId)
      } else if (context.self == successor) {
        receivedFromSuccessor = Some(candidateId)
      }
      decideBasedOnNeighbors()

    case Coordinator(leaderId) =>
      context.log.info(s"Node $id acknowledges $leaderId as the leader.")
      Behaviors.same

    case _ => Behaviors.unhandled
  }
}