package com.mservices.board;

import com.mservices.engine.BoardInitialization;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Initialization Rules")
class BoardInitializationTest {

    @Test
    @DisplayName("STANDARD setup should place 20 white and 20 black men")
    void standardSetupShouldPlaceExpectedPieceCounts() {
        Board board = new Board(BoardInitialization.STANDARD);

        int whiteCount = 0;
        int blackCount = 0;

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Tile tile = board.getTile(row, col);
                if (tile.isEmpty()) {
                    continue;
                }
                assertEquals(PieceType.MAN, tile.getPiece().getType(), "Initial pieces must be men");
                if (tile.getPiece().getColor() == PieceColor.WHITE) {
                    whiteCount++;
                } else {
                    blackCount++;
                }
            }
        }

        assertEquals(20, whiteCount, "White should start with 20 pieces");
        assertEquals(20, blackCount, "Black should start with 20 pieces");
    }

    @Test
    @DisplayName("STANDARD setup should place pieces only on playable dark tiles and valid rows")
    void standardSetupShouldRespectPlayableTilesAndRows() {
        Board board = new Board(BoardInitialization.STANDARD);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Tile tile = board.getTile(row, col);
                if (tile.isEmpty()) {
                    continue;
                }

                assertTrue(tile.isPlayable(), "Pieces must be on playable tiles only");
                if (tile.getPiece().getColor() == PieceColor.WHITE) {
                    assertTrue(row >= 6 && row <= 9, "White must start on rows 6..9");
                } else {
                    assertTrue(row >= 0 && row <= 3, "Black must start on rows 0..3");
                }
            }
        }
    }

    @Test
    @DisplayName("EMPTY setup should contain no pieces")
    void emptySetupShouldContainNoPieces() {
        Board board = new Board(BoardInitialization.EMPTY);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                assertTrue(board.getTile(row, col).isEmpty(), "All tiles should be empty in EMPTY setup");
            }
        }
    }

    @Test
    @DisplayName("Bottom-left tile (9,0) should be dark and playable")
    void bottomLeftTileShouldBePlayable() {
        Board board = new Board(BoardInitialization.EMPTY);
        Tile bottomLeft = board.getTile(9, 0);

        assertTrue(bottomLeft.isPlayable(), "(9,0) must be a playable dark tile");
    }
}
