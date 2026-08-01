package com.mservices.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class MoveChain {
    private final List<Move> moves;

    public MoveChain(List<Move> moves) {
        this.moves = Collections.unmodifiableList(new ArrayList<>(moves));
    }

    public List<Move> getMoves() {
        return moves;
    }

    public int getCaptureCount() {
        int captures = 0;
        for (Move move : moves) {
            if (move.isCapture()) {
                captures++;
            }
        }
        return captures;
    }
}

