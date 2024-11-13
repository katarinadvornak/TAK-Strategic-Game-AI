package com.Tak.Logic;

import java.util.Objects;

/**
 * The Placement class represents a placement action in the Tak game.
 */
public class Placement extends Action {
    private int x, y;
    private Piece.PieceType pieceType;
    private Player player;

    public Placement(int x, int y, Piece.PieceType pieceType, Player player) {
        this.x = x;
        this.y = y;
        this.pieceType = pieceType;
        this.player = player;
    }

    public Piece.PieceType getPieceType() {
        return this.pieceType;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void execute(Board board) throws InvalidMoveException {
        board.placePiece(x, y, new Piece(pieceType, player), player);
    }

    @Override
    public Player getActionPlayer() {
        return this.player;
    }


    @Override
    public String toString() {
        return player.getColor() + " placed " + pieceType + " at (" + x + ", " + y + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Placement)) return false;
        Placement other = (Placement) obj;
        return this.x == other.x && this.y == other.y && this.pieceType == other.pieceType && this.player.equals(other.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, pieceType, player);
    }

    @Override
    public void undo(Board board) throws InvalidMoveException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'undo'");
    }
}
