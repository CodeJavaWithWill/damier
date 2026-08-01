package com.mservices.board;

import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tile Hierarchy Unit Tests")
class TileTest {

    @Nested
    @DisplayName("Abstract & General Tile Behavior Tests")
    class GeneralTileTests {

        @ParameterizedTest(name = "Tile at row {0}, col {1} should retain its coordinates")
        @CsvSource({
                "0, 1",
                "5, 5",
                "9, 8"
        })
        void shouldRetainCoordinatesWhenInitialized(int row, int col) {
            // Act: Instantiate both types to ensure abstract constructor assigns values properly
            Tile darkTile = new DarkTile(row, col);
            Tile lightTile = new LightTile(row, col);

            // Assert
            assertAll(
                    () -> assertEquals(row, darkTile.getRow(), "Dark tile row mismatch"),
                    () -> assertEquals(col, darkTile.getCol(), "Dark tile column mismatch"),
                    () -> assertEquals(row, lightTile.getRow(), "Light tile row mismatch"),
                    () -> assertEquals(col, lightTile.getCol(), "Light tile column mismatch")
            );
        }

        @Test
        @DisplayName("Newly initialized tiles must be empty by default")
        void shouldBeEmptyOnInitialization() {
            // Arrange & Act
            Tile darkTile = new DarkTile(0, 1);
            Tile lightTile = new LightTile(0, 0);

            // Assert
            assertAll(
                    () -> assertTrue(darkTile.isEmpty(), "Dark tile should initially be empty"),
                    () -> assertFalse(darkTile.hasPiece(), "Dark tile should report false for having a piece"),
                    () -> assertTrue(lightTile.isEmpty(), "Light tile should initially be empty")
            );
        }
    }

    @Nested
    @DisplayName("DarkTile Specific Constraints")
    class DarkTileTests {

        @Test
        @DisplayName("Dark tiles must always report as playable")
        void shouldAlwaysBePlayable() {
            // Arrange & Act
            Tile darkTile = new DarkTile(4, 3);

            // Assert
            assertTrue(darkTile.isPlayable(), "Dark tiles must return true for isPlayable()");
        }

        @Test
        @DisplayName("Dark tiles must successfully hold and clear game pieces")
        void shouldAllowPieceMutations() {
            // Arrange
            Tile darkTile = new DarkTile(6, 1);
            Piece mockPiece = new Piece(PieceColor.WHITE, PieceType.MAN);

            // Act: Place piece
            darkTile.setPiece(mockPiece);

            // Assert placement
            assertAll(
                    () -> assertFalse(darkTile.isEmpty(), "Tile should no longer be empty"),
                    () -> assertTrue(darkTile.hasPiece(), "Tile should report it has a piece"),
                    () -> assertEquals(mockPiece, darkTile.getPiece(), "Returned piece should match placed piece")
            );

            // Act: Clear piece
            darkTile.clear();

            // Assert removal
            assertAll(
                    () -> assertTrue(darkTile.isEmpty(), "Tile should be empty after clear()"),
                    () -> assertNull(darkTile.getPiece(), "Piece reference should be null after clear()")
            );
        }
    }

    @Nested
    @DisplayName("LightTile Specific Constraints")
    class LightTileTests {

        @Test
        @DisplayName("Light tiles must never report as playable")
        void shouldNeverBePlayable() {
            // Arrange & Act
            Tile lightTile = new LightTile(0, 0);

            // Assert Light Tiles must always return false for isPlayable()
            assertFalse(lightTile.isPlayable(), "Light tiles must return false for isPlayable()");
        }

        @Test
        @DisplayName("Light tiles must throw an exception if a piece is placed on them")
        void shouldThrowExceptionWhenPlacingPieceOnLightTile() {
            // Arrange
            Tile lightTile = new LightTile(2, 2);
            Piece mockPiece = new Piece(PieceColor.BLACK, PieceType.MAN);

            // Act & Assert
            UnsupportedOperationException exception = assertThrows(
                    UnsupportedOperationException.class,
                    () -> lightTile.setPiece(mockPiece),
                    "Expected setPiece on LightTile to throw UnsupportedOperationException"
            );

            assertEquals("Cannot place a piece on an unplayable Light Tile.", exception.getMessage());
        }
    }
}
