package cs553

import akka.actor.typed.ActorRef
import cs553.Algorithms.Election.Franklin.FranklinAlgorithm.Direction

object Nodes {

  sealed trait Command
  case class SetSuccessor(successorRef: ActorRef[Command]) extends Command
  case class SetLeftNeighbor(leftNeighborRef: ActorRef[Command]) extends Command
  case class SetRightNeighbor(rightNeighborRef: ActorRef[Command]) extends Command
  case class SetNeighbors(neighborRef: List[ActorRef[Command]]) extends Command
  case class Wave(tag: Int, replyTo: ActorRef[Command]) extends Command
  final case object WakeUpMessage extends Command
  case object StartAlgorithm extends Command
  case object StartWakeUp extends Command
  case class FranklinToken(senderId: Int, roundNumber: Int, direction: Direction) extends Command
  case class DKRToken(senderId: Int, electionId: Int, roundNumber: Int, n: Int) extends Command
  case class CRToken(senderId: Int) extends Command
  case class InformationMessage(id: Int) extends Command

}