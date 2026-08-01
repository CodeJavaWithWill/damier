# Damier - International 10x10 Draughts Engine

A Java implementation of International 10x10 Draughts (Turkish Draughts) game engine with move validation, capture mechanics, and piece promotion.

## Current Status

✅ **Fully Functional Core Gameplay**
- Move validation for Men and Kings
- Capture detection and execution (Turkish protocol)
- Multi-jump sequences with maximum capture rule enforcement
- Piece promotion to King on baseline
- Complete test coverage (26 passing tests)

⚠️ **Not Yet Implemented**
- Game termination conditions (win/loss detection)
- Draw rules (25-move king-only, 3-fold repetition, endgame draws)
- Move history and undo functionality
- UI/display layer

This README documents the **current working implementation**. As features are added, this documentation will evolve.

## Project Structure

```
damier/
├── pom.xml                          # Maven configuration
├── README.md                         # This file
├── DEVELOPER_GUIDE.md               # Architecture and development guide
├── src/
│   ├── specs.md                     # Functional specifications (FMJD rules)
│   ├── main/
│   │   ├── java/com/mservices/
│   │   │   ├── board/               # Board representation and tile management
│   │   │   │   ├── Board.java
│   │   │   │   ├── Tile.java
│   │   │   │   ├── DarkTile.java    # Playable tiles
│   │   │   │   └── LightTile.java   # Non-playable tiles
│   │   │   ├── piece/               # Piece representation
│   │   │   │   ├── Piece.java
│   │   │   │   ├── PieceColor.java  # Enum: WHITE, BLACK
│   │   │   │   └── PieceType.java   # Enum: MAN, KING
│   │   │   └── engine/              # Game logic and rules engine
│   │   │       ├── MoveEngine.java          # Movement and capture validation
│   │   │       ├── TurnManager.java         # Turn state and flow control
│   │   │       ├── Move.java                # Single move representation
│   │   │       ├── MoveChain.java           # Multi-jump sequence
│   │   │       └── BoardInitialization.java # Board setup strategies
│   │   └── resources/               # Configuration files
│   └── test/
│       ├── java/com/mservices/
│       │   ├── board/               # Board and tile tests
│       │   └── engine/              # Game logic tests
│       └── resources/               # Test configuration
└── target/                          # Build output (Maven)
```

## Key Features ✅ Currently Implemented

### Board Representation
- **10x10 matrix** with only dark squares playable (50 active squares)
- **Tile abstraction** with `DarkTile` (playable) and `LightTile` (non-playable)
- Safe boundary checking for all coordinate access
- Standard initialization with 20 pieces per side

### Piece System
- **Men**: Standard pieces that move 1 square diagonally forward and capture diagonally (any direction)
- **Kings**: Promoted pieces that move any distance along diagonals
- **Colors**: WHITE and BLACK pieces with correct movement directions
- **Promotion**: Automatic when Man reaches opponent's baseline, deferred during multi-jump sequences

### Movement Validation Engine
- **Diagonal-only movement**: Enforces correct diagonal paths for all pieces
- **Direction enforcement**: Men restricted to forward; Kings unrestricted
- **Capture detection**: Automatic identification of capture vs. regular moves
- **Path clarity**: Kings cannot move through multiple enemy pieces
- **Out-of-bounds prevention**: No moves off the board

### Turkish Capture Protocol
- **Deferred removal**: Captured pieces stay on board during entire multi-jump sequence
- **Collision prevention**: Same piece cannot be captured twice in sequence
- **Atomic cleanup**: All captured pieces removed together when sequence ends
- **Continuation detection**: Allows discovery of follow-up captures from new positions

### Maximum Capture Rule
- **Recursive chain search**: Explores all possible capture paths to find maximum
- **Board simulation**: Moves simulated on actual board during search for accurate detection
- **Optimal selection**: Returns only chains with global maximum capture count
- **Multi-piece alternatives**: Correctly compares chains involving different pieces



## Quick Start

### Building the Project

```bash
# Navigate to the damier directory
cd /path/to/damier

# Build with Maven
mvn clean install

# Run tests
mvn test
```

### Basic Usage

```java
import com.mservices.board.Board;
import com.mservices.board.Tile;
import com.mservices.engine.*;
import com.mservices.piece.PieceColor;

// Initialize the board with standard starting position
Board board = new Board(BoardInitialization.STANDARD);

// Create the game engine
MoveEngine moveEngine = new MoveEngine(board);

// Validate a move (WHITE piece at row 2)
Tile source = board.getTile(2, 1);
Tile target = board.getTile(1, 0);
if (moveEngine.isValidMove(source, target)) {
    // Execute the move
    moveEngine.tryExecuteMove(source, target);
}

// For multi-jump sequences, use executeMultiJumpSequence()
List<Tile> jumpSequence = Arrays.asList(
    board.getTile(5, 2),
    board.getTile(3, 4)
);
moveEngine.executeMultiJumpSequence(source, jumpSequence);
```

## Game Rules Summary

### Starting Position
- **White**: Rows 1-3 (20 Men at the top)
- **Black**: Rows 7-9 (20 Men at the bottom)
- Only dark squares playable (where `row + col` is odd)

### Movement ✅ Implemented
- **Men**: Move 1 diagonal square forward; capture by jumping 2 squares diagonally (any direction)
- **Kings**: Move diagonally any number of squares; capture along diagonal paths
- **Promotion**: Man → King when reaching opponent's baseline (Row 0 for White, Row 9 for Black)

### Capturing ✅ Implemented
- **Maximum captures**: If multiple capture paths exist, player must choose path with most pieces
- **Capture sequence**: After capture, same piece continues jumping if more captures available
- **Turkish protocol**: Captured pieces remain on board during multi-jump sequence, removed when sequence completes
- **Collision prevention**: Pieces cannot be captured twice in same sequence

### Win Conditions ⚠️ Not Yet Implemented
- Detection of no pieces remaining
- Detection of no legal moves (stalemate)

### Draw Conditions ⚠️ Not Yet Implemented
- Position repetition (3-fold)
- 25 consecutive king-only moves without captures
- Endgame automatic draws

## Testing

The project includes comprehensive test coverage (26 passing tests):

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=GamePlayTest

# Run with detailed output
mvn clean test
```

### Test Coverage

| Suite | Tests | Focus |
|-------|-------|-------|
| **GamePlayTest** | 5 | Standard moves, captures, promotion timing |
| **KingMovementTest** | 4 | Flying moves, blocking, multi-square captures |
| **CaptureProtocolTest** | 1 | Turkish protocol verification (deferred removal) |
| **BoardInitializationTest** | 2 | Board setup validation |
| **BoardTest** | 2 | Boundary checking |
| **TileTest** | 12 | Individual tile behavior |

**Result: 26/26 tests passing ✅**

## Architecture Highlights

### Module Responsibilities

**Board Module** (`com.mservices.board`)
- Board state representation (10x10 grid)
- Tile abstraction and concrete implementations
- Coordinate validation and translation

**Piece Module** (`com.mservices.piece`)
- Piece model (Men and Kings)
- Color tracking (WHITE and BLACK)
- Type tracking and promotion capability

**Engine Module** (`com.mservices.engine`)
- `MoveEngine`: Move validation and single-move execution
- `TurnManager`: Turn flow orchestration and forced-capture handling
- `Move`: Immutable single move value object
- `MoveChain`: Immutable multi-jump sequence with capture counting
- `BoardInitialization`: Board setup strategies (STANDARD or EMPTY)

### Key Design Decisions

1. **Tile-based Movement**: All moves reference `Tile` objects (identity-based) rather than coordinates for safety
2. **Separation of Concerns**: 
   - `MoveEngine` owns move validation and single-move execution
   - `TurnManager` owns turn sequencing and multi-move orchestration
3. **No Board Mutation During Validation**: Move validation never modifies board state
4. **Deferred Removal Pattern**: Captured pieces marked "dead" during sequence but not removed until completion
5. **Board Simulation During Search**: Chain search simulates moves on actual board to accurately detect continuations

### Design Patterns

- **Strategy Pattern**: `BoardInitialization` enum for flexible board setup
- **Template Method**: `Tile` abstract class with implementations
- **Immutable Value Objects**: `Move` and `MoveChain` for thread-safe representation
- **Simulation Pattern**: Recursive search with move simulation and undo for chain discovery

## Development

### Building the Project

```bash
cd /path/to/damier
mvn clean compile  # Build
mvn clean test     # Build and run tests
```

### Dependencies

- **Runtime**: Java 9+
- **Build**: Maven 3.6+
- **Testing**: JUnit 4.13.2

### Adding New Features

When extending functionality:

1. Check the current test suite passes: `mvn test`
2. Add tests first for new behavior
3. Ensure separation of concerns:
   - Move validation/execution → `MoveEngine`
   - Turn sequencing → `TurnManager`
   - Board state → `Board` and `Tile`
   - Piece model → `Piece`
4. Update this README to reflect new capabilities
5. Document in [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) if architecture changes

### Known Limitations

- No game termination detection yet (win/loss)
- No draw rule enforcement yet
- No move history or undo
- No UI layer

## References

- [World Draughts Federation (FMJD)](https://www.fmjd.org/) - Official rules
- `src/specs.md` - Technical specifications for this project

## Related Documentation

- [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) - Architecture, module details, and extension patterns

