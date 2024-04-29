# Chang-Roberts Election Algorithm

The Chang-Roberts algorithm is a ring-based leader election algorithm that operates under the assumption that each node in the network is connected in a unidirectional ring. Each node sends its identifier around the ring, and each node passes on the largest identifier it has seen. The node with the highest identifier becomes the leader.

## Implementation Details

- **Algorithm Description:**
  - Each participating node sends its ID in a message around the ring.
  - Upon receiving an ID, a node compares it with its own. If the received ID is greater, it forwards the received ID; otherwise, it does nothing unless its own ID returns to it, indicating it is the highest and thus the leader.
  - The algorithm ensures that each node will eventually receive the highest ID and recognize the leader.

- **Files:**
  - `CRAlgorithm.scala`: Contains the implementation of the Chang-Roberts leader election algorithm.
  - `ChangRobertsDriver.scala`: A driver program that sets up the network, initiates the algorithm, and manages the simulation.
 
## Testing

- **CRAlgorithmTest.scala**: Provides unit tests for the Chang-Roberts algorithm to ensure its correct operation and the proper election of a leader under various scenarios.

## References

[1] Wan Fokkink, “Elections,” in *Distributed Algorithms: An Intuitive Approach*, 2nd ed. This reference provides the theoretical foundation for the Chang-Roberts algorithm and discusses its properties and variations in the context of distributed systems.
