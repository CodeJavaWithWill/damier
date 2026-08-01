package com.mservices.engine;

import com.mservices.piece.Piece;
import com.mservices.piece.PieceColor;
import com.mservices.piece.PieceType;

public enum BoardInitialization {
    STANDARD {
        @Override
        public Piece getPieceForPosition(int row, int col) {
            // Rows 0 to 3 belong to Black setup
            if (row >= 0 && row <= 3) {
                return new Piece(PieceColor.BLACK, PieceType.MAN);
            }
            // Rows 6 to 9 belong to White setup
            if (row >= 6 && row <= 9) {
                return new Piece(PieceColor.WHITE, PieceType.MAN);
            }
            // Rows 4 and 5 are the empty middle playable squares
            return null;
        }
    },
    EMPTY {
        @Override
        public Piece getPieceForPosition(int row, int col) {
            // Returns null for every position to create a clear board for tests
            return null;
        }
    };

    /**
     * Factory method to resolve what piece belongs on a given tile position.
     * @param row The matrix row coordinate (0-9)
     * @param col The matrix column coordinate (0-9)
     * @return A newly instantiated Piece object, or null if the tile should be empty.
     */
    public abstract Piece getPieceForPosition(int row, int col);
}