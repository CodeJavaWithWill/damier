package com.mservices.engine;

import com.mservices.board.Board;
import com.mservices.board.Tile;
import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;

import java.util.List;

/**
 * the TurnManager acts as the referee or conductor of the game loop.
 * It tells the system whose turn it is,
 * what they are allowed to do right now,
 * and when their turn is officially over.
 */
public class TurnManager {
    private final Board board;
    private final MoveEngine moveEngine;
    private PieceColor activePlayerColor = PieceColor.WHITE;

    // Tracks a piece mid-turn if it must continue capturing
    private Tile activeJumpingTile = null;

    public TurnManager(Board board, MoveEngine moveEngine) {
        this.board = board;
        this.moveEngine = moveEngine;
    }

    /**
     * Executes a move action triggered by the player or AI.
     */
    public void handlePlayerMoveAction(Tile source, Tile target) {
        // Rule Guard: If mid-sequence, player MUST use the same piece
        if (activeJumpingTile != null && source != activeJumpingTile) {
            throw new IllegalStateException("Forced Capture Active: You must continue jumping with the same piece.");
        }

        // Validate the individual step using our MoveEngine rules
        if (!moveEngine.isValidMove(source, target)) {
            throw new IllegalArgumentException("Invalid diagonal movement execution path.");
        }

        boolean isCapture = moveEngine.isCaptureStep(source, target);

        // 1. Physically move the piece across the grid matrix
        Piece movingPiece = source.getPiece();
        source.clear();
        target.setPiece(movingPiece);

        // 2. If it was a capture, remove the skipped enemy piece from the board
        if (isCapture) {
            moveEngine.removeSkippedPiece(source, target);

            // 3. CORE RULE: Check if THIS specific piece has *further* captures available
            List<Tile> nextAvailableJumps = moveEngine.getLegalCapturesForTile(target);

            if (!nextAvailableJumps.isEmpty()) {
                // The sequence MUST continue. Lock turn state to this piece.
                activeJumpingTile = target;
                System.out.println("Continuous capture required! Next targets available: " + nextAvailableJumps.size());
                return; // Exit early. Do NOT switch turns, do NOT check promotions yet.
            }
        }

        // 4. Turn Finalization Phase (Triggers only when NO MORE captures remain)
        finalizeTurnAndCheckPromotion(target);
        cleanTurnStateAndSwitchPlayers();
    }

    private void finalizeTurnAndCheckPromotion(Tile finalTile) {
        Piece piece = finalTile.getPiece();
        if (piece != null && piece.getType() == PieceType.MAN) {
            // Promotion check occurs strictly on the final resting tile
            if ((piece.getColor() == PieceColor.WHITE && finalTile.getRow() == 0) ||
                    (piece.getColor() == PieceColor.BLACK && finalTile.getRow() == 9)) {
                piece.promoteToKing();
                System.out.println("Turn complete. Piece safely promoted to King.");
            }
        }
    }

    private void cleanTurnStateAndSwitchPlayers() {
        activeJumpingTile = null; // Clear multi-jump lock
        activePlayerColor = (activePlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
    }
}
