# Dolev-Klawe-Rodeh (DKR) Election Algorithm

The Dolev-Klawe-Rodeh algorithm adapts Franklin's algorithm for use in a directed ring where messages only travel one way, making direct ID comparisons by an active process with its neighbors challenging. To handle this, ID comparisons are carried out not at the initiating node but at the next active neighbor in the ring. If the initiating node's ID is the highest, that neighbor progresses in the election; if not, it becomes passive.

## Implementation Details

- **Algorithm Description:**
  - The algorithm is implemented with each actor (node) having the ability to send tokens to its successor via a directed channel. The election decision is based on the comparison of token values representing node IDs.
  - Mutable state for tokens is managed within each actor to adhere to the principles of the actor model, ensuring that state changes are localized to individual actors.

- **Core Components:**
  - `DKRAlgorithm` object: Manages behavior definitions and actor setup.
  - `DKRAlgorithm` class: Handles the election logic for each actor, including token sending, receiving, and leader election.

- **Behavior Functions:**
  - **setup**: Prepares the actor with initial states and successor references.
  - **active**: Engages in the token exchange and leader election process.
  - **passive**: Takes a back seat in the election process, forwarding tokens without participating in leader decisions.
    
## Testing

- **DKRAlgorithmTest.scala**: Provides unit tests for the DKR algorithm to ensure its correct operation and the proper election of a leader under various scenarios.

## References

[1] Wan Fokkink, “Elections,” in *Distributed Algorithms: An Intuitive Approach*, 2nd ed.

