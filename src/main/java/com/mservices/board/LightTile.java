package com.mservices.board;

import com.mservices.piece.Piece;

public class LightTile extends Tile {

    LightTile(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isPlayable() {
        return false;
    }

    @Override
    public void setPiece(Piece piece) {
        throw new UnsupportedOperationException("Cannot place a piece on an unplayable Light Tile.");
    }
}
