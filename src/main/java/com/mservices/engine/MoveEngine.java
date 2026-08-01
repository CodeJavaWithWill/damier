package com.mservices.engine;

import com.mservices.board.Board;
import com.mservices.board.Tile;
import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;

import java.util.ArrayList;
import java.util.List;

public class MoveEngine {

    private static final int[][] DIAGONALS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private final Board board;

    public MoveEngine(Board board) {
        this.board = board;
    }

    public boolean tryExecuteMove(Tile startTile, Tile targetTile) {
        return executeMoveInternal(startTile, targetTile, true);
    }

    private boolean executeMoveInternal(Tile startTile, Tile targetTile, boolean applyPromotion) {
        if (!isValidMove(startTile, targetTile)) {
            return false;
        }

        boolean captureStep = isCaptureStep(startTile, targetTile);
        Piece movingPiece = startTile.getPiece();

        startTile.clear();
        targetTile.setPiece(movingPiece);

        if (captureStep) {
            removeSkippedPiece(startTile, targetTile);
        }

        if (applyPromotion) {
            checkAndPromote(targetTile);
        }
        return true;
    }

    private boolean executeMoveInternalDeferred(Tile startTile, Tile targetTile, java.util.List<Tile> deadPieces) {
        if (!isValidMove(startTile, targetTile)) {
            return false;
        }

        boolean captureStep = isCaptureStep(startTile, targetTile);
        Piece movingPiece = startTile.getPiece();

        startTile.clear();
        targetTile.setPiece(movingPiece);

        if (captureStep) {
            recordSkippedPieceForDeferral(startTile, targetTile, deadPieces);
        }

        return true;
    }

    private void recordSkippedPieceForDeferral(Tile source, Tile target, java.util.List<Tile> deadPieces) {
        Piece piece = target.getPiece();
        if (piece == null) return;

        int deltaRow = target.getRow() - source.getRow();
        int deltaCol = target.getCol() - source.getCol();

        if (piece.getType() == PieceType.MAN) {
            Tile skippedTile = getSkippedTileForMan(source, deltaRow, deltaCol);
            deadPieces.add(skippedTile);
        } else if (piece.getType() == PieceType.KING) {
            int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
            int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;

            int currentRow = source.getRow() + stepRow;
            int currentCol = source.getCol() + stepCol;

            while (currentRow != target.getRow() && currentCol != target.getCol()) {
                Tile pathTile = board.getTile(currentRow, currentCol);
                if (!pathTile.isEmpty()) {
                    deadPieces.add(pathTile);
                    break;
                }
                currentRow += stepRow;
                currentCol += stepCol;
            }
        }
    }

    /**
     * Clean Core Logic Evaluator
     */
    public boolean isValidMove(Tile source, Tile target) {
        Piece piece = source.getPiece();
        int deltaRow = target.getRow() - source.getRow();   //landing row
        int deltaCol = target.getCol() - source.getCol();   //landing column

        // ***** Validation
        // Guard 1: Source must have a piece, and target MUST be empty
        if (source.isEmpty() || !target.isEmpty()) {
            return false;
        }

        // Guard 2: Absolute diagonal validation (Row steps must exactly match Column steps)
        if (Math.abs(deltaRow) != Math.abs(deltaCol)) {
            return false;
        }

        // Branch by piece tier
        if (piece.getType() == PieceType.MAN) {
            return validateManMovement(source, target, piece, deltaRow, deltaCol);
        } else {
            return validateKingMovement(source, target);
        }
    }

    /**
     * Logic for Standard Pieces (Man)
     */
    private boolean validateManMovement(Tile source, Tile target, Piece piece, int deltaRow, int deltaCol) {
        int absRow = Math.abs(deltaRow);

        // Scenario A: Regular Peaceful Move (1 Square away)
        if (absRow == 1) {
            // White can only decrease rows (-1); Black can only increase rows (+1)
            int validForwardDirection = (piece.getColor() == PieceColor.WHITE) ? -1 : 1;
            return deltaRow == validForwardDirection;
        }

        // Scenario B: Capture Jump Move (Exactly 2 Squares away)
        if (absRow == 2) {
            // Find the skipped middle square coordinates
            int skippedRow = source.getRow() + (deltaRow / 2);
            int skippedCol = source.getCol() + (deltaCol / 2);
            Tile skippedTile = board.getTile(skippedRow, skippedCol);

            // True if skipped tile holds an enemy piece
            return !skippedTile.isEmpty() && skippedTile.getPiece().getColor() != piece.getColor();
        }

        return false; // A standard Man can never step 3+ squares away
    }

    /**
     * Logic for Promoted Pieces (King)
     */
    private boolean validateKingMovement(Tile source, Tile target) {
        // Establish directional steps (-1 or +1)
        int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
        int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;

        int currentRow = source.getRow() + stepRow;
        int currentCol = source.getCol() + stepCol;

        int piecesEncountered = 0;
        boolean lastSquareWasOccupied = false;

        // Loop through the diagonal sliding path until reaching the target tile
        while (currentRow != target.getRow() && currentCol != target.getCol()) {
            Tile pathTile = board.getTile(currentRow, currentCol);

            if (!pathTile.isEmpty()) {
                piecesEncountered++;

                // RULE CHANGE ENFORCEMENT:
                // 1. Block if King hits its own color piece.
                // 2. Block if it hits two enemy pieces directly adjacent to each other.
                // 3. Block if it tries to jump more than one piece total *before landing*.
                if (pathTile.getPiece().getColor() == source.getPiece().getColor() ||
                        lastSquareWasOccupied ||
                        piecesEncountered > 1) {
                    return false;
                }

                lastSquareWasOccupied = true;
            } else {
                // The square is empty, so we reset the adjacency tracker
                lastSquareWasOccupied = false;
            }

            currentRow += stepRow;
            currentCol += stepCol;
        }

        return true;
    }

    /**
     * Mutation Helper: Call this immediately after a valid move executes
     */
    public void checkAndPromote(Tile tile) {
        if (tile.isEmpty()) return;
        Piece piece = tile.getPiece();

        // White promotes at row 0 (top); Black promotes at row 9 (bottom)
        if (piece.getType() == PieceType.MAN) {
            if ((piece.getColor() == PieceColor.WHITE && tile.getRow() == 0) ||
                    (piece.getColor() == PieceColor.BLACK && tile.getRow() == 9)) {
                piece.promoteToKing();
            }
        }
    }

    /**
     * Checks if a validated movement step represents an enemy capture jump.
     * @param source The starting tile of the step.
     * @param target The landing tile of the step.
     * @return true if an enemy piece was jumped over during the movement.
     */
    public boolean isCaptureStep(Tile source, Tile target) {
        if (source.isEmpty()) {
            return false;
        }

        Piece piece = source.getPiece();
        int deltaRow = target.getRow() - source.getRow();
        int deltaCol = target.getCol() - source.getCol();
        int absRow = Math.abs(deltaRow);

        // 1. Logic for a Standard Man piece
        if (piece.getType() == PieceType.MAN) {
            // A Man piece captures *if and only if* it jumps exactly 2 squares out
            if (absRow == 2) {
                Tile skippedTile = getSkippedTileForMan(source, deltaRow, deltaCol);
                // It's a capture step if the middle square holds an opponent piece
                return !skippedTile.isEmpty() && skippedTile.getPiece().getColor() != piece.getColor();
            }
            return false;
        }

        // 2. Logic for a Flying King piece
        if (piece.getType() == PieceType.KING) {
            // Establish the directional vectors (-1 or +1)
            int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
            int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;

            int currentRow = source.getRow() + stepRow;
            int currentCol = source.getCol() + stepCol;

            // Slide along the diagonal between source and target
            while (currentRow != target.getRow() && currentCol != target.getCol()) {
                Tile pathTile = board.getTile(currentRow, currentCol);

                // If we encounter an enemy piece on the diagonal line, it's a capture step
                if (!pathTile.isEmpty() && pathTile.getPiece().getColor() != piece.getColor()) {
                    return true;
                }
                currentRow += stepRow;
                currentCol += stepCol;
            }
        }

        return false;
    }

    /**
     * Helper Mutation Method: Deletes the skipped enemy piece from the board matrix.
     * Called by TurnManager immediately after a capture step is completed.
     */
    public void removeSkippedPiece(Tile source, Tile target) {
        // We look at the target tile because the piece has already finished its move step there
        Piece piece = target.getPiece();
        if (piece == null) return;

        int deltaRow = target.getRow() - source.getRow();
        int deltaCol = target.getCol() - source.getCol();

        if (piece.getType() == PieceType.MAN) {
            Tile skippedTile = getSkippedTileForMan(source, deltaRow, deltaCol);
            skippedTile.clear(); // Wipe the jumped man from the board
        } else if (piece.getType() == PieceType.KING) {
            int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
            int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;

            int currentRow = source.getRow() + stepRow;
            int currentCol = source.getCol() + stepCol;

            while (currentRow != target.getRow() && currentCol != target.getCol()) {
                Tile pathTile = board.getTile(currentRow, currentCol);
                if (!pathTile.isEmpty()) {
                    pathTile.clear(); // Wipe the jumped piece along the King's slide line
                    break; // A King can only jump one piece per single step line
                }
                currentRow += stepRow;
                currentCol += stepCol;
            }
        }
    }

    /**
     * Math helper to find the exact midpoint square for a standard piece jump.
     */
    private Tile getSkippedTileForMan(Tile source, int deltaRow, int deltaCol) {
        int skippedRow = source.getRow() + (deltaRow / 2);
        int skippedCol = source.getCol() + (deltaCol / 2);
        return board.getTile(skippedRow, skippedCol);
    }

    /**
     * Looks ahead from a single tile to see what jump destinations are legally available.
     * Answers: "From here, where can this piece jump right now?"
     */
    public List<Tile> getLegalCapturesForTile(Tile sourceTile) {
        if (sourceTile.isEmpty()) return List.of();

        List<Tile> captureDestinations = new ArrayList<>();
        Piece piece = sourceTile.getPiece();

        // Loop through all 4 diagonal lines (Both Men and Kings capture in all directions)
        for (int[] direction : DIAGONALS) {
            int stepRow = direction[0];
            int stepCol = direction[1];

            if (piece.getType() == PieceType.MAN) {
                // 1. Logic for a Standard Man (Jumps exactly 2 spaces out)
                int enemyRow = sourceTile.getRow() + stepRow;
                int enemyCol = sourceTile.getCol() + stepCol;
                int landingRow = sourceTile.getRow() + (stepRow * 2);
                int landingCol = sourceTile.getCol() + (stepCol * 2);

                if (isInsideBoundaries(landingRow, landingCol)) {
                    Tile enemyTile = board.getTile(enemyRow, enemyCol);
                    Tile landingTile = board.getTile(landingRow, landingCol);

                    // A Man can jump if there is an opponent piece followed by an empty space
                    if (!enemyTile.isEmpty() &&
                            enemyTile.getPiece().getColor() != piece.getColor() &&
                            landingTile.isEmpty()) {
                        captureDestinations.add(landingTile);
                    }
                }
            } else if (piece.getType() == PieceType.KING) {
                // 2. Logic for a Flying King (Can slide any distance to jump)
                int currentRow = sourceTile.getRow() + stepRow;
                int currentCol = sourceTile.getCol() + stepCol;

                boolean enemySpotted = false;
                boolean pathBlocked = false;

                while (isInsideBoundaries(currentRow, currentCol) && !pathBlocked) {
                    Tile currentTile = board.getTile(currentRow, currentCol);

                    if (!enemySpotted) {
                        if (!currentTile.isEmpty()) {
                            if (currentTile.getPiece().getColor() != piece.getColor()) {
                                enemySpotted = true; // Enemy found on the line
                            } else {
                                pathBlocked = true; // Friendly piece blocks the entire line
                            }
                        }
                    } else {
                        // Once an enemy is spotted, every empty trailing square is a valid landing spot
                        if (currentTile.isEmpty()) {
                            captureDestinations.add(currentTile);
                        } else {
                            // Hit a second piece right after the enemy (or an adjacent piece).
                            // This blocks the King instantly under International rules.
                            pathBlocked = true;
                        }
                    }
                    currentRow += stepRow;
                    currentCol += stepCol;
                }
            }
        }
        return captureDestinations;
    }

    /**
     * Helper to verify if a coordinate exists on a 10x10 matrix.
     */
    private boolean isInsideBoundaries(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    public List<Move> calculateLegalMoves(PieceColor playerColor) {
        List<Move> captureMoves = new ArrayList<>();
        List<Move> nonCaptureMoves = new ArrayList<>();

        for (int sourceRow = 0; sourceRow < 10; sourceRow++) {
            for (int sourceCol = 0; sourceCol < 10; sourceCol++) {
                Tile source = board.getTile(sourceRow, sourceCol);
                if (source.isEmpty() || source.getPiece().getColor() != playerColor) {
                    continue;
                }

                for (int targetRow = 0; targetRow < 10; targetRow++) {
                    for (int targetCol = 0; targetCol < 10; targetCol++) {
                        Tile target = board.getTile(targetRow, targetCol);
                        if (!isValidMove(source, target)) {
                            continue;
                        }

                        boolean capture = isCaptureStep(source, target);
                        Move move = new Move(source, target, capture);
                        if (capture) {
                            captureMoves.add(move);
                        } else {
                            nonCaptureMoves.add(move);
                        }
                    }
                }
            }
        }

        // Keep forced-capture behavior for callers that rely on this method.
        return captureMoves.isEmpty() ? nonCaptureMoves : captureMoves;
    }

    public List<MoveChain> calculateOptimalCaptureChains(PieceColor playerColor) {
        List<MoveChain> allChains = new ArrayList<>();

        for (int sourceRow = 0; sourceRow < 10; sourceRow++) {
            for (int sourceCol = 0; sourceCol < 10; sourceCol++) {
                Tile source = board.getTile(sourceRow, sourceCol);
                if (source.isEmpty() || source.getPiece().getColor() != playerColor) {
                    continue;
                }

                List<Tile> availableCaptures = getLegalCapturesForTile(source);
                for (Tile firstCapture : availableCaptures) {
                    Piece movedPiece = source.getPiece();
                    Piece capturedPiece = getCapturedPiece(source, firstCapture);

                    // Simulate the move on the board
                    source.clear();
                    firstCapture.setPiece(movedPiece);
                    if (capturedPiece != null) {
                        Tile capturedTile = findCapturedTile(source, firstCapture);
                        if (capturedTile != null) {
                            capturedTile.clear();
                        }
                    }

                    buildCaptureChains(source, firstCapture, new ArrayList<>(List.of(new Move(source, firstCapture, true))), playerColor, allChains);

                    // Undo the move
                    firstCapture.clear();
                    source.setPiece(movedPiece);
                    if (capturedPiece != null) {
                        Tile capturedTile = findCapturedTile(source, firstCapture);
                        if (capturedTile != null) {
                            capturedTile.setPiece(capturedPiece);
                        }
                    }
                }
            }
        }

        if (allChains.isEmpty()) {
            return List.of();
        }

        int maxCaptureCount = 0;
        for (MoveChain chain : allChains) {
            maxCaptureCount = Math.max(maxCaptureCount, chain.getCaptureCount());
        }

        List<MoveChain> optimalChains = new ArrayList<>();
        for (MoveChain chain : allChains) {
            if (chain.getCaptureCount() == maxCaptureCount) {
                optimalChains.add(chain);
            }
        }

        return optimalChains;
    }

    private Piece getCapturedPiece(Tile source, Tile target) {
        int deltaRow = target.getRow() - source.getRow();
        int deltaCol = target.getCol() - source.getCol();

        if (Math.abs(deltaRow) == 2) {
            Tile skippedTile = getSkippedTileForMan(source, deltaRow, deltaCol);
            return skippedTile.getPiece();
        } else {
            int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
            int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;
            int midRow = source.getRow() + stepRow;
            int midCol = source.getCol() + stepCol;
            return board.getTile(midRow, midCol).getPiece();
        }
    }

    private Tile findCapturedTile(Tile source, Tile target) {
        int deltaRow = target.getRow() - source.getRow();
        int deltaCol = target.getCol() - source.getCol();

        if (Math.abs(deltaRow) == 2) {
            return getSkippedTileForMan(source, deltaRow, deltaCol);
        } else {
            int stepRow = (target.getRow() > source.getRow()) ? 1 : -1;
            int stepCol = (target.getCol() > source.getCol()) ? 1 : -1;
            int midRow = source.getRow() + stepRow;
            int midCol = source.getCol() + stepCol;
            return board.getTile(midRow, midCol);
        }
    }

    private void buildCaptureChains(Tile lastSourceTile, Tile currentTile, List<Move> currentChain, PieceColor playerColor, List<MoveChain> result) {
        List<Tile> nextCaptures = getLegalCapturesForTile(currentTile);

        if (nextCaptures.isEmpty()) {
            result.add(new MoveChain(new ArrayList<>(currentChain)));
            return;
        }

        for (Tile nextCapture : nextCaptures) {
            Piece movedPiece = currentTile.getPiece();
            Piece capturedPiece = getCapturedPiece(currentTile, nextCapture);

            currentTile.clear();
            nextCapture.setPiece(movedPiece);
            if (capturedPiece != null) {
                Tile capturedTile = findCapturedTile(currentTile, nextCapture);
                if (capturedTile != null) {
                    capturedTile.clear();
                }
            }

            currentChain.add(new Move(currentTile, nextCapture, true));
            buildCaptureChains(currentTile, nextCapture, currentChain, playerColor, result);
            currentChain.remove(currentChain.size() - 1);

            // Undo the move
            nextCapture.clear();
            currentTile.setPiece(movedPiece);
            if (capturedPiece != null) {
                Tile capturedTile = findCapturedTile(currentTile, nextCapture);
                if (capturedTile != null) {
                    capturedTile.setPiece(capturedPiece);
                }
            }
        }
    }

    public void executeMultiJumpSequence(Tile startTile, List<Tile> landingSequence) {
        if (landingSequence.isEmpty()) {
            throw new IllegalArgumentException("Landing sequence cannot be empty.");
        }

        Tile currentTile = startTile;
        java.util.List<Tile> deadPieces = new ArrayList<>();

        for (int i = 0; i < landingSequence.size(); i++) {
            Tile nextLanding = landingSequence.get(i);
            if (!executeMoveInternalDeferred(currentTile, nextLanding, deadPieces)) {
                throw new IllegalArgumentException("Invalid jump sequence step provided.");
            }
            currentTile = nextLanding;
        }

        // Only promote at the final landing
        checkAndPromote(currentTile);

        // Clear all dead pieces after sequence completes (Turkish protocol)
        for (Tile deadTile : deadPieces) {
            deadTile.clear();
        }
    }

    public boolean isValidCapturePath(Tile tile, Tile forwardTarget) {
        return isValidMove(tile, forwardTarget) && isCaptureStep(tile, forwardTarget);
    }
}
