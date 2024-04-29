# Franklin Election Algorithm

This README details the implementation of the Franklin Election Algorithm in Scala using the Akka actor model. The algorithm is designed for distributed systems with a bidirectional ring topology, where nodes attempt to elect a leader by comparing token values sent in both directions.

## Implementation Details

- **Algorithm Description:**
  - Nodes operate as independent actors that send and receive tokens with their IDs to both their left and right neighbors. The election process involves comparing these IDs to determine the highest, potentially electing a leader if a node finds itself to be the highest in the comparison.

- **Core Components:**
  - `FranklinAlgorithm` object: Configures and initiates the actor behaviors for the Franklin election process.
  - `FranklinAlgorithm` class: Handles the election logic, managing token exchange, neighbor setup, and leader determination.

- **Behavior Functions:**
  - **setup**: Initializes the actor with its left and right neighbors, setting up for token exchange.
  - **active**: Engages in token processing, evaluates potential leadership based on token comparisons, and advances the election rounds.
  - **passive**: When a node no longer contends for leadership, it forwards tokens and acts as a relay in the election process.

## Testing

- **FranklinAlgorithmTest.scala**: Provides unit tests for the Franklin algorithm to ensure its correct operation and the proper election of a leader under various scenarios.

## References

[1] Wan Fokkink, “Elections,” in *Distributed Algorithms: An Intuitive Approach*, 2nd ed.
