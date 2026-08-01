package com.mservices.board;

import com.mservices.engine.BoardInitialization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Board Matrix Boundaries")
class BoardTest {

    @Test
    @DisplayName("getTile should reject negative coordinates")
    void getTileShouldRejectNegativeCoordinates() {
        Board board = new Board(BoardInitialization.EMPTY);

        assertThrows(IndexOutOfBoundsException.class, () -> board.getTile(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getTile(0, -1));
    }

    @Test
    @DisplayName("getTile should reject coordinates outside 10x10 bounds")
    void getTileShouldRejectCoordinatesOutsideBounds() {
        Board board = new Board(BoardInitialization.EMPTY);

        assertThrows(IndexOutOfBoundsException.class, () -> board.getTile(10, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getTile(0, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getTile(10, 10));
    }
}

