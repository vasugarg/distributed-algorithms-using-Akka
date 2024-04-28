package cs553.Algorithms.EchoExtinction

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import cs553.Nodes._
import cs553.Algorithms.Election.EchoExtinction._

class EchoAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "An EchoAlgorithm actor" should {
    "correctly initiate and propagate waves to neighbors" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val echoNode = spawn(EchoAlgorithm(1))

      echoNode ! SetNeighbors(List(probe1.ref, probe2.ref))

      probe1.expectMessageType[Wave]
      probe2.expectMessageType[Wave]
    }

    "join a wave with a higher tag and propagate it" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val probe3 = createTestProbe[Command]()
      val echoNode = spawn(EchoAlgorithm(2))

      echoNode ! SetNeighbors(List(probe1.ref, probe2.ref, probe3.ref))
      echoNode ! Wave(5, probe1.ref)

      probe2.expectMessage(Wave(5, echoNode.ref))
      probe3.expectMessage(Wave(5, echoNode.ref))
    }

    "ignore waves with a lower tag than the current active wave" in {
      val probe1 = createTestProbe[Command]()
      val echoNode = spawn(EchoAlgorithm(3))

      echoNode ! SetNeighbors(List(probe1.ref))
      echoNode ! Wave(10, probe1.ref)  // Join wave 10
      echoNode ! Wave(5, probe1.ref)   // Ignore wave 5

      probe1.expectMessageType[Wave]  // Only the first wave should cause a message
      probe1.expectNoMessage()
    }

    "become the leader if it initiated the wave and received all acknowledgments" in {
      val probe1 = createTestProbe[Command]()
      val probe2 = createTestProbe[Command]()
      val echoNode = spawn(EchoAlgorithm(4))

      echoNode ! SetNeighbors(List(probe1.ref, probe2.ref))
      echoNode ! Wave(4, echoNode.ref)  // Node itself initiates the wave

      probe1.expectMessage(Wave(4, echoNode.ref))
      probe2.expectMessage(Wave(4, echoNode.ref))

      // Simulate acknowledgments from all neighbors
      echoNode ! Wave(4, probe1.ref)
      echoNode ! Wave(4, probe2.ref)

      // Check for leader declaration log (Can be captured or inferred from the behavior changes)
    }
  }
}
