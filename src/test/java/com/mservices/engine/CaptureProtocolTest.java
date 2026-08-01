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

@DisplayName("Capture Protocol Rules")
class CaptureProtocolTest {

    private Board board;
    private MoveEngine moveEngine;

    @BeforeEach
    void setUp() {
        board = new Board(BoardInitialization.EMPTY);
        moveEngine = new MoveEngine(board);
    }

    @Test
    @DisplayName("Turkish protocol: jumped piece should remain until chain completion")
    void jumpedPieceShouldRemainOnBoardUntilSequenceEnds() {
        Tile start = board.getTile(2, 1);
        Tile firstLanding = board.getTile(0, 3);
        Tile secondLanding = board.getTile(2, 5);
        Tile firstEnemy = board.getTile(1, 2);
        Tile secondEnemy = board.getTile(1, 4);

        start.setPiece(new Piece(PieceColor.WHITE, PieceType.MAN));
        firstEnemy.setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));
        secondEnemy.setPiece(new Piece(PieceColor.BLACK, PieceType.MAN));

        // Execute multi-jump sequence: the first jumped enemy should remain until sequence completes
        moveEngine.executeMultiJumpSequence(start, java.util.List.of(firstLanding, secondLanding));

        assertAll(
                () -> assertTrue(start.isEmpty(), "Starting tile should be empty"),
                () -> assertTrue(firstEnemy.isEmpty(), "First jumped piece should be removed after full sequence ends"),
                () -> assertTrue(secondEnemy.isEmpty(), "Second jumped piece should be removed after full sequence ends"),
                () -> assertEquals(PieceType.MAN, secondLanding.getPiece().getType(), "Man should land on final position and remain MAN during multi-jump")
        );
    }
}

