# Implementing Distributed Algorithms using Akka Actor Model

This Scala project simulates various distributed election algorithms, demonstrating node election processes in different network configurations. 

## Building and Running 

### Requirements

* [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Scala Build Tool](https://www.scala-sbt.org/), `sbt` (version >=1.9.9)
* [Scala](https://www.scala-lang.org/download/) (version >= 2.13.3)

### Building and Testing the Project

To compile and run the entire test suite, execute the following command in the terminal from the root folder of the project:

```
sbt clean compile test
```

### Running the Project

1) Clone this repository

```
git clone https://github.com/vasugarg/distributed-algorithms-using-Akka.git
```
2) cd to the Project
```
cd distributed-algorithms-using-Akka
```
3) Open the project in intelliJ
```
https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html#open-project
```
4) To run the algorithms, open `src/main/scala/cs553/Main.scala` file in intelliJ and execute the main function from IDE.

### Configuration Settings (application.conf)

The input graphs are defined as a configuration file under `src/main/resources/application.conf` and few sample graphs have been already generated and kept under `inputs` folder as DOT files. Graphs of different sizes can be generated from [NetGameSim](https://github.com/0x1DOCD00D/NetGameSim) 

## Algorithms Implemented

### [Chang-Roberts Algorithm](./src/main/scala/cs553/Algorithms/Election/ChangRoberts/ChangRoberts.md)
Click the above hyperlink for detailed documentation of the algorithm.
  - A classical ring election algorithm, using a token-passing mechanism to elect a leader.
  - **Files:**
    - `CRAlgorithm.scala`: Implements the Chang-Roberts algorithm.
    - `ChangRobertsDriver.scala`: Drives the simulation of the Chang-Roberts algorithm.
   
### [Franklin's Algorithm](./src/main/scala/cs553/Algorithms/Election/Franklin/Franklin.md)
Click the above hyperlink for detailed documentation of the algorithm.
  - Franklin's algorithm aimed to improve the message complexity of Chang-Roberts by comapring the neighboring IDs through undirected channels.
  - **Files:**
    - `FranklinAlgorithm.scala`: Implements Franklin's election algorithm.
    - `FranklinDriver.scala`: Driver for simulating Franklin's algorithm in a distributed setting.

### [Dolev-Klawe-Rodeh (DKR) Algorithm](./src/main/scala/cs553/Algorithms/Election/DolevKlaweRodeh/DolevKlaweRodeh.md)
Click the above hyperlink for detailed documentation of the algorithm.
  - The DKR Algorithm is designed for ring networks where each node communicates in a unidirectional manner.
  - **Files:**
    - `DKRAlgorithm.scala`: Contains the logic for the DKR election process.
    - `DKRDriver.scala`: Facilitates running the DKR algorithm simulation.

### [Echo Extinction Algorithm](./src/main/scala/cs553/Algorithms/Election/EchoExtinction/EchoExtinction.md)
Click the above hyperlink for detailed documentation of the algorithm.
  - This algorithm focuses on implementing a type of Wave algorithm called Echo to elect a leader.
  - **Files:**
    - `EchoAlgorithm.scala`: Implements the message propagation and extinction logic.
    - `EchoElectionDriver.scala`: Executes the Echo algorithm simulation.

### [Tree Election Algorithm](./src/main/scala/cs553/Algorithms/Election/TreeElection/TreeElection.md)
Click the above hyperlink for detailed documentation of the algorithm.
  - Ideal for any acyclic undirected networks, this algorithm elects a leader based on predefined criteria.
  - **Files:**
    - `TreeAlgorithm.scala`: Contains the core logic for tree-based elections.
    - `TreeElectionDriver.scala`: Used to simulate the Tree Election process.

## Utility Classes
- **Files:**
  - `CycleDetector.scala`: Helps detect cycles in network communication, crucial for some election algorithms like Tree Election.
  - `FileParser.scala`: Parses input files containing network configurations and node information.

## Main Class
- **File:**
  - `Main.scala`: The main entry point of the application. It initializes the environment and triggers the simulations based on user input or configuration.

## Testing
- Automated tests are implemented for each algorithm to ensure their correctness and stability.
- **Test Files:**
  - `DKRAlgorithmTest.scala`
  - `EchoAlgorithmTest.scala`
  - `FranklinAlgorithmTest.scala`
  - `TreeAlgorithmTest.scala`
  - `CRAlgorithmTest.scala`

