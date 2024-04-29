# Echo Extinction Election Algorithm

This README details the implementation of the Echo Extinction Election Algorithm in Scala using the Akka actor model. The algorithm facilitates leader election in distributed systems where nodes communicate through token passing, allowing the system to dynamically determine the leader by propagating "wave" messages among nodes.

## Implementation Details

- **Algorithm Description:**
  - Each node acts as an independent actor that can receive and send wave tokens to its neighbors to participate in the election process.
  - The nodes keep track of waves and join the highest wave they encounter, which determines the election's progression.

- **Core Components:**
  - `EchoAlgorithm` object: Sets up the actor behaviors and initiates the Echo algorithm.
  - `EchoAlgorithm` class: Manages the logic of processing waves, setting neighbors, and determining the active state of the node based on the waves received.

- **Behavior Functions:**
  - **setup**: Initializes the actor with its neighbors and prepares it to receive wave messages.
  - **active**: Handles the active participation of the node in the election process, including joining waves, ignoring lower-tagged waves, and potentially declaring itself as the leader if it finds itself the originator of the highest wave.

## Testing

- **EchoAlgorithmTest.scala**: Provides unit tests for the Echo algorithm to ensure its correct operation and the proper election of a leader under various scenarios.

## References

[1] Wan Fokkink, “Elections,” in *Distributed Algorithms: An Intuitive Approach*, 2nd ed.
