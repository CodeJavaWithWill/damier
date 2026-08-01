package com.mservices.board;

public class DarkTile extends Tile {

    DarkTile(int row, int col) {
        super(row, col);
    }
    @Override
    public boolean isPlayable() {
        return true;
    }


}
