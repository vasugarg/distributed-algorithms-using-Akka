package cs553.Algorithms.TreeElection

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import cs553.Nodes.{WakeUpMessage, _}
import cs553.Algorithms.Election.TreeElection._
import scala.concurrent.duration._
import java.time.Duration

class TreeAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A TreeAlgorithm actor" should {
    "initiate wake up messages if it is the initiator" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(1, isInitiator = true))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))

      probe1.expectMessageType[WakeUpMessage.type]
      probe2.expectMessageType[WakeUpMessage.type]
    }

    "not initiate wake up messages if it is not the initiator" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(2, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))

      probe1.expectNoMessage()
      probe2.expectNoMessage()
    }

    "become active after receiving wake up messages from all neighbors" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(3, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))

      probe1.ref ! WakeUpMessage
      probe2.ref ! WakeUpMessage

      probe1.expectMessageType[WakeUpMessage.type]
      probe2.expectMessageType[WakeUpMessage.type]
    }

    "send wave messages to the parent and inform other neighbors upon receiving wave messages" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val probe3 = createTestProbe[Command]()
      val treeNode1 = spawn(TreeAlgorithm(4, isInitiator = true))
      val treeNode2 = spawn(TreeAlgorithm(6, isInitiator = false))

      treeNode1 ! SetNeighbors(List(treeNode2))
      treeNode2 ! SetNeighbors(List(probe1.ref, probe2.ref, probe3.ref))

      // Make the node active by simulating it received all wake up calls
      List(probe1, probe2, probe3).foreach { probe =>
        treeNode2 ! WakeUpMessage
      }

      // Simulate receiving a Wave message from one of the neighbors
      //treeNode ! Wave(5, probe1.ref)

      // Expect a Wave message sent to one and Information messages to others
      probe1.expectMessage(WakeUpMessage)
      probe2.expectMessage(WakeUpMessage)
      probe3.expectMessage(WakeUpMessage)


//      List(probe1, probe2, probe3).foreach(_.ref ! WakeUpMessage)

      // Simulate receiving a Wave message from one of the neighbors
      treeNode2 ! Wave(4, treeNode1.ref)
//      testKit.scheduler.scheduleOnce(500.milliseconds, new Runnable {
//        def run(): Unit = {
//          treeNode1 ! Wave(5, probe1.ref)
//        }
//      })(system.executionContext)

      // Expect a Wave message sent to one and Information messages to others
      val expectedWave = probe2.expectMessageType[Wave]
      assert(expectedWave.tag == 5)

      val expectedInfo = probe3.expectMessageType[InformationMessage]
      assert(expectedInfo.id == 5)
    }

    "correctly identify the maximum ID from wave messages and propagate it" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val treeNode = spawn(TreeAlgorithm(6, isInitiator = false))

      treeNode ! SetNeighbors(List(probe1.ref, probe2.ref))

      // Make the node active and simulate wave messages
      treeNode ! Wave(10, probe1.ref)
      treeNode ! Wave(15, probe2.ref)

      // Expect the node to propagate the maximum ID
      probe1.expectMessage(InformationMessage(15))
      probe2.expectMessage(InformationMessage(15))
    }
  }
}

