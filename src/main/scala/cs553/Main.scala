package cs553

import cs553.Algorithms.Election.ChangRoberts.ChangRobertsDriver
import cs553.Algorithms.Election.DolevKlaweRodeh.DKRDriver
import cs553.Algorithms.Election.EchoExtinction.EchoElectionDriver
import cs553.Algorithms.Election.Franklin.FranklinDriver
import cs553.Algorithms.Election.TreeElection.TreeElectionDriver

object Main{
  def main(args: Array[String]): Unit = {
    println("Select the algorithm to run:")
    val algorithms = Seq(
      "Chang-Roberts Algorithm" -> ChangRobertsDriver.main _,
      "Franklin Algorithm" -> FranklinDriver.main _,
      "DolevKlaweRodeh Algorithm" -> DKRDriver.main _,
      "Tree Election Algorithm" -> TreeElectionDriver.main _,
      "Echo Algorithm with Extinction" -> EchoElectionDriver.main _
    )

    algorithms.zipWithIndex.foreach { case (algo, index) =>
      println(s"${index + 1} - ${algo._1}")
    }

    val choice = scala.io.StdIn.readInt()

    if (choice > 0 && choice <= algorithms.length) {
      algorithms(choice - 1)._2(Array.empty[String]) // Call the main method of the chosen algorithm
    } else {
      println("Invalid choice, please select a valid option.")
    }
  }
}

