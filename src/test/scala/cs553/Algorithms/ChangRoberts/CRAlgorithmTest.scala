package cs553.Algorithms.ChangRoberts

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import cs553.Algorithms.Election.ChangRoberts.CRAlgorithm
import cs553.Nodes._
import org.scalatest.wordspec.AnyWordSpecLike

class CRAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A CRAlgorithm actor" should {

    "become passive when it receives a CRToken with a higher ID" in {
      val probe = createTestProbe[Command]()
      val crAlgorithm = spawn(CRAlgorithm(1))

      crAlgorithm ! SetSuccessor(probe.ref)
      crAlgorithm ! StartAlgorithm
      probe.expectMessageType[CRToken]
      crAlgorithm ! CRToken(2)

      probe.expectMessage(CRToken(2))
    }

    "discard a CRToken with a lower ID" in {
      val probe = createTestProbe[Command]()
      val crAlgorithm = spawn(CRAlgorithm(3))

      crAlgorithm ! SetSuccessor(probe.ref)
      crAlgorithm ! StartAlgorithm
      probe.expectMessageType[CRToken]
      crAlgorithm ! CRToken(2)

      probe.expectNoMessage()
    }

    "acknowledge becoming the leader when it receives a CRToken with its own ID" in {
      val probe = createTestProbe[Command]()
      val crAlgorithm = spawn(CRAlgorithm(3))

      crAlgorithm ! SetSuccessor(probe.ref)
      crAlgorithm ! StartAlgorithm
      probe.expectMessageType[CRToken]
      crAlgorithm ! CRToken(3)

      probe.expectNoMessage() // No further message expected, actor should log "Becomes the leader"
    }
  }
}

