# Technical Functional Specification: Digital Damier (International 10x10 Draughts)

## 1. Project Overview & Scope
This document defines the functional and technical requirements for developing a digital, two-player implementation of **International Damier (10x10 Draughts)**. The application must support standard regulatory movement, forced-capture rules, and structural logic defined by the World Draughts Federation (FMJD).

---

## 2. Core Game Logic & Structural Board State

### 2.1 Coordinate System & Board Matrix
* **Data Structure:** The board must be represented as a 10x10 matrix ($100$ total tiles) or a performance-optimized 50-element array representing active gameplay squares.
* **Coordinate Mapping:** The board uses internal coordinates ranging from `(0,0)` to `(9,9)`.
* **Active Tiles:** Gameplay is restricted strictly to dark squares.
    * A tile at `(row, col)` is playable if and only if `(row + col) % 2 != 0` (assuming `(0,0)` is a light square).
* **Orientation:** The bottom-left corner relative to the current player must always be a **dark playable square** (`(9,0)` for White).

### 2.2 Initial State Configuration
* **White Pieces:** Occupy dark squares on rows `6, 7, 8, 9` (Total: 20 Man pieces).
* **Black Pieces:** Occupy dark squares on rows `0, 1, 2, 3` (Total: 20 Man pieces).
* **Starting Turn:** Player White always executes the first move.

BLACK SIDE (Rows 0-3)
[B][ ][B][ ][B][ ][B][ ][B][ ]  <- Row 0
[ ][B][ ][B][ ][B][ ][B][ ][B]  <- Row 1
[B][ ][B][ ][B][ ][B][ ][B][ ]  <- Row 2
[ ][B][ ][B][ ][B][ ][B][ ][B]  <- Row 3
[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]  <- Row 4
[ ][ ][ ][ ][ ][ ][ ][ ][ ][ ]  <- Row 5
[W][ ][W][ ][W][ ][W][ ][W][ ]  <- Row 6
[ ][W][ ][W][ ][W][ ][W][ ][W]  <- Row 7
[W][ ][W][ ][W][ ][W][ ][W][ ]  <- Row 8
[ ][W][ ][W][ ][W][ ][W][ ][W]  <- Row 9
WHITE SIDE (Rows 6-9)


---

## 3. Movement Rule Engine (FSM Rules)

The rule engine must compute valid moves at the start of every turn using a two-phase check: **Capture Verification** and **Regular Movement Validation**.

### 3.1 Piece Definitions
* **Man (Standard Piece):** Can move 1 step diagonally forward to an empty square.
* **King (Promoted Piece):** A Man promotes to a King only if it *finishes* its turn on the opponent's baseline (Row 0 for White, Row 9 for Black).
    * Flying King abilities: Can move across any number of empty squares along a diagonal line.

### 3.2 Forcing Priority: The Majority Capture Rule
* **Mandatory Captures:** If a capture sequence is available, the player *cannot* make a regular diagonal move.
* **Maximum Quantity Rule:** If multiple distinct capture paths exist, the system must force the player to select a path that results in the **maximum possible number of captured pieces**.
* **Type Disregard:** A King piece holds no higher priority weight than a Man during capture count calculation; 3 Men must be captured over 2 Kings.
* **UI Enforcement:** Lock all non-capturing pieces. Highlight only the pieces capable of executing the maximum capture chain.

### 3.3 Capture & Jump Mechanics
* **Direction:** Both Men and Kings can capture backwards and forwards.
* **Man Jumps:** Must jump over an adjacent enemy piece to an immediately succeeding empty square along the diagonal line.
* **King Jumps:** Can jump over an enemy piece located at a distance on a diagonal path, provided all preceding squares are empty and at least one trailing square is empty.
* **The "Turkish" Capture Protocol:**
    1. Captured pieces are *not* removed from the board immediately when jumped.
    2. Captured pieces are flagged as "dead" and remain physically on the board until the *entire multi-jump sequence ends*.
    3. A piece cannot cross or jump over the same enemy piece twice during a single turn chain.

---

## 4. Game Over Conditions & Win State Validation

The state machine must validate game termination after every completed turn.

### 4.1 Victory Criteria
A player wins when the opponent:
* Has zero remaining pieces on the board.
* Is completely blocked and has no legal moves available on their turn.

### 4.2 Draw Criteria (Standard International Rules)
The engine must declare a draw automatically if:
* The same exact board position occurs for a **third time** (with the same player to move).
* Both players agree to a draw.
* **The 25-Move Rule:** 25 successive turns are played where only Kings have moved, and no pieces have been captured.
* **Endgame Specific Profiles:**
    * 3 Kings vs 1 King: Automatically a draw after 16 moves.
    * 2 Kings vs 1 King, or 1 King vs 1 King: Automatically a draw after 5 moves.

---

## 5. UI/UX & Technical Implementation Requirements

### 5.1 Front-End Interface Guidelines
* **Aspect Ratio:** The board wrapper must maintain a strict 1:1 responsive layout.
* **Visual Interaction States:**
    * `Idle`: Non-turn pieces have default pointers.
    * `Selectable`: Valid pieces to move pulse or show an outline pointer.
    * `Active/Selected`: Shows a distinct border around the chosen piece.
    * `Target Destinations`: Highlights valid drop-zones/target tiles in green or translucent markers.
* **Multi-Jump Assist:** For complex multi-capture paths, the UI should display temporary path-arrows or sequential steps to prevent player confusion.

### 5.2 State Management & API Schema (Draft JSON)
The backend or core state module must track the game state dynamically. Below is the minimum required JSON state structure:

```json
{
  "gameId": "uuidv4-string",
  "turn": "WHITE",
  "status": "ACTIVE", 
  "moveCount": 42,
  "kingRuleCounter": 12,
  "board": [
    {"index": 1, "row": 0, "col": 1, "piece": "BLACK_MAN"},
    {"index": 12, "row": 2, "col": 3, "piece": "WHITE_KING"},
    {"index": 50, "row": 9, "col": 8, "piece": null}
  ],
  "activePlayerCanCapture": true,
  "forcedCapturePaths": [
    {
      "from": {"row": 6, "col": 1},
      "sequence": [{"row": 4, "col": 3}, {"row": 2, "col": 1}],
      "capturedIndices": [14, 23]
    }
  ]
}
```