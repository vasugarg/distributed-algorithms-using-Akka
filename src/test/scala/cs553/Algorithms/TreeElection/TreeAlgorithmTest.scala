package cs553.Algorithms.TreeElection

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import cs553.Nodes.{WakeUpMessage, _}
import cs553.Algorithms.Election.TreeElection._

class TreeAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A TreeAlgorithm actor" should {
    "initiate wake up messages if it is the initiator" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(1, isInitiator = true))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))
      treeNode ! StartWakeUp

      probe1.expectMessageType[WakeUpMessage.type]
      probe2.expectMessageType[WakeUpMessage.type]
    }

    "not initiate wake up messages if it is not the initiator" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(2, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))
      treeNode ! StartWakeUp

      probe1.expectNoMessage()
      probe2.expectNoMessage()
    }

    "become active after receiving wake up messages from all neighbors" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(3, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))
      treeNode ! StartWakeUp

      probe1.ref ! WakeUpMessage
      probe2.ref ! WakeUpMessage

      probe1.expectMessageType[WakeUpMessage.type]
      probe2.expectMessageType[WakeUpMessage.type]
    }

    "correctly identify the maximum ID from wave messages and propagate it" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(6, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))
      treeNode ! StartWakeUp

      treeNode ! WakeUpMessage
      treeNode ! WakeUpMessage
      probe1.expectMessage(WakeUpMessage)
      probe2.expectMessage(WakeUpMessage)

      treeNode ! StartAlgorithm

      // Make the node active and simulate wave messages
      treeNode ! Wave(10, probe1.ref)
      treeNode ! Wave(15, probe2.ref)
      probe2.expectMessageType[Wave]

      // Expect the node to propagate the maximum ID
      probe1.expectMessage(InformationMessage(15))
    }
  }
}

