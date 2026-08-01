package com.mservices.board;

import com.mservices.engine.BoardInitialization;
import com.mservices.piece.Piece;

public class Board {

    private final Tile[][] matrix = new Tile[10][10]; // game fields

    /**
     * Constructs a 10x10 Damier board using a single-pass initialization sequence.
     * @param strategy The initialization pattern rule configuration profile.
     */
    public Board(BoardInitialization strategy) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {

                // 1. Math layout formula to alternate dark/light squares
                if ((row + col) % 2 != 0) {
                    // Instantiate structural Dark Tile
                    matrix[row][col] = new DarkTile(row, col);

                    // 2. Factory Step: Instantly query strategy for a piece placement
                    Piece startingPiece = strategy.getPieceForPosition(row, col);
                    if (startingPiece != null) {
                        matrix[row][col].setPiece(startingPiece);
                    }
                } else {
                    // Instantiate structural Light Tile (unplayable, always empty)
                    matrix[row][col] = new LightTile(row, col);
                }

            }
        }
    }

    /**
     * Safely retrieves a Tile object from the board grid using standard boundary checks.
     */
    public Tile getTile(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            throw new IndexOutOfBoundsException("Requested tile coordinates are out of 10x10 board boundaries.");
        }
        return matrix[row][col];
    }
}
