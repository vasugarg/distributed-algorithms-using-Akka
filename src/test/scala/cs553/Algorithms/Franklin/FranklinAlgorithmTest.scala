package cs553.Algorithms.Franklin

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import cs553.Nodes._
import cs553.Algorithms.Election.Franklin._

class FranklinAlgorithmTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "A FranklinAlgorithm actor" should {
    "send initial tokens to both neighbors upon activation" in {
      val leftProbe = createTestProbe[Command]()
      val rightProbe = createTestProbe[Command]()
      val franklinNode = spawn(FranklinAlgorithm(1))

      franklinNode ! SetLeftNeighbor(leftProbe.ref)
      franklinNode ! SetRightNeighbor(rightProbe.ref)

      leftProbe.expectMessageType[FranklinToken]
      rightProbe.expectMessageType[FranklinToken]
    }
  }

  "moves to the next round if it has the highest ID" in {
    val leftProbe = createTestProbe[Command]()
    val rightProbe = createTestProbe[Command]()
    val franklinNode = spawn(FranklinAlgorithm(10))

    franklinNode ! SetLeftNeighbor(leftProbe.ref)
    franklinNode ! SetRightNeighbor(rightProbe.ref)
    leftProbe.expectMessageType[FranklinToken]
    rightProbe.expectMessageType[FranklinToken]

    // Node receives lower ID tokens from both sides
    franklinNode ! FranklinToken(2, 0, FranklinAlgorithm.Left)
    franklinNode ! FranklinToken(3, 0, FranklinAlgorithm.Right)

    // The node should move to the next round and send its ID to the neighbors
    val sentTokenLeft = leftProbe.expectMessageType[FranklinToken]
    val sentTokenRight = rightProbe.expectMessageType[FranklinToken]
    assert(sentTokenLeft.senderId == 10 && sentTokenLeft.roundNumber == 1)
    assert(sentTokenRight.senderId == 10 && sentTokenRight.roundNumber == 1)
  }

  "transition to passive if a higher ID is received from any neighbor" in {
    val leftProbe = createTestProbe[Command]()
    val rightProbe = createTestProbe[Command]()
    val franklinNode = spawn(FranklinAlgorithm(5))

    franklinNode ! SetLeftNeighbor(leftProbe.ref)
    franklinNode ! SetRightNeighbor(rightProbe.ref)
    leftProbe.expectMessageType[FranklinToken]
    rightProbe.expectMessageType[FranklinToken]

    // Node receives a higher ID token from the right side
    franklinNode ! FranklinToken(4, 0, FranklinAlgorithm.Left)
    franklinNode ! FranklinToken(6, 0, FranklinAlgorithm.Right)

    // Expect that the node forwards future tokens without initiating new rounds
    franklinNode ! FranklinToken(7, 1, FranklinAlgorithm.Left)
    val forwardedToken = leftProbe.expectMessageType[FranklinToken]
    assert(forwardedToken.senderId == 7 && forwardedToken.roundNumber == 1)
  }

  "continue to next rounds if no leader is determined immediately" in {
    val leftProbe = createTestProbe[Command]()
    val rightProbe = createTestProbe[Command]()
    val franklinNode = spawn(FranklinAlgorithm(10))

    franklinNode ! SetLeftNeighbor(leftProbe.ref)
    franklinNode ! SetRightNeighbor(rightProbe.ref)

    // Simulate a scenario where the node continues to receive tokens with varying IDs
    franklinNode ! FranklinToken(9, 0, FranklinAlgorithm.Left)
    franklinNode ! FranklinToken(9, 0, FranklinAlgorithm.Right)

    // Expect the node to initiate the next round
    leftProbe.expectMessageType[FranklinToken]
    rightProbe.expectMessageType[FranklinToken]
  }
}

