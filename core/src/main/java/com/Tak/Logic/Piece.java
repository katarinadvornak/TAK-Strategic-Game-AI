package com.Tak.Logic;

public class Piece {
    private PieceType type;
    private boolean isWhite;  // For player color

    public Piece(PieceType type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public PieceType getType() {
        return type;
    }

    public boolean isWhite() {
        return isWhite;
    }
}

