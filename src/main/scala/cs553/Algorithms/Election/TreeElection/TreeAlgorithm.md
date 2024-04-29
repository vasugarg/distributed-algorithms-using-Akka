# Tree Election Algorithm

This README describes the implementation of the Tree Election Algorithm in Scala using the Akka actor model. This algorithm is designed for a tree-structured network, where nodes determine a leader through a process of wave propagation and identification of the highest node ID.

## Implementation Details

- **Algorithm Description:**
  - Nodes in this algorithm are independent actors that communicate through messages to perform wake-ups and pass wave messages. The leader election is based on the propagation of the highest node ID through the tree structure.
  
- **Core Components:**
  - `TreeAlgorithm` object: Initializes the actor behaviors based on node IDs and their initiator status.
  - `TreeAlgorithm` class: Manages the election logic, handling the node states during wake-up, active, and passive phases.

- **Behavior Functions:**
  - **setup**: Sets up the initial state of a node, defining neighbors and handling wake-up signals.
  - **wakeUp**: Manages the state of a node as it receives wake-up messages, coordinating the start of the election process.
  - **active**: Processes incoming wave messages, compares node IDs, and forwards the highest ID up the tree until a leader is determined.

## Testing

- **TreeAlgorithmTest.scala**: Provides unit tests for the Tree election algorithm to ensure its correct operation and the proper election of a leader under various scenarios.

## References

[1] Wan Fokkink, “Elections,” in *Distributed Algorithms: An Intuitive Approach*, 2nd ed.
