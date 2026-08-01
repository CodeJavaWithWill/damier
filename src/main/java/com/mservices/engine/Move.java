package com.mservices.engine;

import com.mservices.board.Tile;

public class Move {
    private final Tile source;
    private final Tile destination;
    private final boolean capture;

    public Move(Tile source, Tile destination, boolean capture) {
        this.source = source;
        this.destination = destination;
        this.capture = capture;
    }

    public Tile getSource() {
        return source;
    }

    public Tile getDestination() {
        return destination;
    }

    public boolean isCapture() {
        return capture;
    }
}

