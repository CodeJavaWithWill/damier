package com.mservices.board;

import com.mservices.piece.Piece;

public abstract class Tile {

    private final int row;
    private final int col;
    private Piece piece;

    Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public abstract boolean isPlayable();

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isEmpty() {
        return piece == null;
    }

    public boolean hasPiece() {
        return !isEmpty();
    }

    public Piece getPiece() {
        return piece;
    }

    public void clear() {
        setPiece(null);
    }
}
