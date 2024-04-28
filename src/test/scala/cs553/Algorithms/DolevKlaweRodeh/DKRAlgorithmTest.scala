package cs553.Algorithms.DolevKlaweRodeh

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import cs553.Nodes._
import cs553.Algorithms.Election.DolevKlaweRodeh._

class DKRAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A DKRAlgorithm actor" should {
    "become passive when it receives a lower election ID than its own and check the behaviour of Passive state" in {
      val probe = createTestProbe[Command]()
      val dkrNode = spawn(DKRAlgorithm(2))

      dkrNode ! SetSuccessor(probe.ref)
      dkrNode ! StartAlgorithm
      probe.expectMessageType[DKRToken] // Initial setup token

      // Node receives a higher ID than p (1) and becomes passive
      dkrNode ! DKRToken(3, 1, 0, 0)
      probe.expectMessageType[DKRToken]
      dkrNode ! DKRToken(3, 5, 0, 1)
      probe.expectNoMessage()


      // Send another token and verify forwarding
      dkrNode ! DKRToken(4, 6, 1, 0)
      val forwardedToken = probe.expectMessageType[DKRToken]
      assert(forwardedToken.electionId == 6 && forwardedToken.roundNumber == 1 && forwardedToken.n == 0)
    }
  }
  "elect the correct leader when higher IDs are propagated" in {
    val probe = createTestProbe[Command]()
    val dkrNode = spawn(DKRAlgorithm(5))


    dkrNode ! SetSuccessor(probe.ref)
    dkrNode ! StartAlgorithm
    probe.expectMessageType[DKRToken] // Initial setup token

    // Simulate rounds where node p:12 should eventually become the leader
    dkrNode ! DKRToken(10, 12, 0, 0)
    probe.expectMessageType[DKRToken]
    dkrNode ! DKRToken(10, 10, 0, 1)

    // Assume round completion, node p has the highest ID seen and proceeds to the next round
    val nextRoundToken = probe.expectMessageType[DKRToken]
    assert(nextRoundToken.electionId == 12 && nextRoundToken.roundNumber == 1)
  }
}

