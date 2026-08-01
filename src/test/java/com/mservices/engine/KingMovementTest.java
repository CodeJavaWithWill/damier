package com.mservices.engine;

import com.mservices.board.Board;
import com.mservices.board.Tile;
import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("King Movement Rules")
class KingMovementTest {

    private Board board;
    private MoveEngine moveEngine;

    @BeforeEach
    void setUp() {
        board = new Board(BoardInitialization.EMPTY);
        moveEngine = new MoveEngine(board);
    }

    @Test
    @DisplayName("King should move across multiple empty diagonal squares")
    void kingShouldMoveMultipleSquaresOnClearDiagonal() {
        Tile source = board.getTile(5, 4);
        Tile target = board.getTile(2, 1);
        source.setPiece(new Piece(PieceColor.WHITE, PieceType.KING));

        assertTrue(moveEngine.tryExecuteMove(source, target), "King long diagonal move should be valid");
        assertTrue(source.isEmpty(), "Source should be empty after move");
        assertEquals(PieceType.KING, target.getPiece().getType(), "King should remain a king after moving");
    }

    @Test
    @DisplayName("King should not move through a friendly piece")
    void kingShouldNotMoveThroughFriendlyPiece() {
        Tile source = board.getTile(5, 4);
        Tile blocker = board.getTile(4, 3);
        Tile target = board.getTile(2, 1);

        source.setPiece(new Piece(PieceColor.WHITE, PieceType.KING));
        blocker.setPiece(new Piece(PieceColor.WHITE, PieceType.MAN));

        assertFalse(moveEngine.isValidMove(source, target), "Friendly piece should block the king path");
    }

    @Test
    @DisplayName("King should detect a valid capture step across diagonal")
    void kingShouldDetectCaptureStep() {
        Tile source = board.getTile(5, 4);
        Tile enemy = board.getTile(3, 2);
        Tile target = board.getTile(2, 1);

        source.setPiece(new Piece(PieceColor.WHITE, PieceType.KING));
        enemy.setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

        assertAll(
                () -> assertTrue(moveEngine.isValidMove(source, target), "King capture move should be valid"),
                () -> assertTrue(moveEngine.isCaptureStep(source, target), "Move should be classified as capture")
        );
    }

    @Test
    @DisplayName("King should reject paths that cross more than one piece before landing")
    void kingShouldRejectMultiplePiecesOnPath() {
        Tile source = board.getTile(7, 6);
        Tile firstEnemy = board.getTile(6, 5);
        Tile secondEnemy = board.getTile(4, 3);
        Tile target = board.getTile(3, 2);

        source.setPiece(new Piece(PieceColor.WHITE, PieceType.KING));
        firstEnemy.setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));
        secondEnemy.setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

        assertFalse(moveEngine.isValidMove(source, target), "King should not jump over more than one piece in a single step");
    }
}

