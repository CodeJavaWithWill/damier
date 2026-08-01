package com.mservices.piece;

public class Piece {
    private PieceColor color;
    private PieceType type;

    public Piece(PieceColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }


    public void promoteToKing() {
        this.type = PieceType.KING;
    }
}
