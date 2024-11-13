/**
 * The Move class represents a movement action in the game of Tak, providing methods to execute and
 * undo the move on a game board.
 */
package com.Tak.Logic.models;

import java.io.Serializable;

import java.util.Objects;

/**
 * The Piece class represents a game piece in Tak.
 * It holds information about the piece type and its owner.
 */
public class Piece implements Serializable {
    private static final long serialVersionUID = 1L; 
    /**
     * Enum to define the types of pieces in Tak.
     */
    public enum PieceType {
        FLAT_STONE,     // A flat stone that can be stacked and is part of a road.
        STANDING_STONE, // A standing stone (wall) that blocks roads.
        CAPSTONE        // A capstone that can flatten standing stones and count toward roads.
    }

    private PieceType pieceType; // The type of the piece.
    private Player owner;        // The player who owns the piece.

    /**
     * Constructor to initialize the piece with its type and owner.
     *
     * @param pieceType The type of the piece.
     * @param owner     The owner of the piece.
     */
    public Piece(PieceType pieceType, Player owner) {
        this.pieceType = pieceType;
        this.owner = owner;
    }

    /**
     * Gets the type of the piece.
     *
     * @return The piece type.
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Sets the type of the piece.
     * This may be needed when a capstone flattens a standing stone.
     *
     * @param pieceType The new piece type.
     */
    public void setPieceType(PieceType pieceType) {
        this.pieceType = pieceType;
    }

    /**
     * Gets the owner of the piece.
     *
     * @return The player who owns the piece.
     */
    public Player getOwner() {
        return this.owner;
    }

    /**
     * Checks if the piece is a capstone.
     *
     * @return true if the piece is a capstone, false otherwise.
     */
    public boolean isCapstone() {
        return this.pieceType == PieceType.CAPSTONE;
    }

    /**
     * Checks if the piece can be part of a road.
     *
     * @return true if the piece can be part of a road, false otherwise.
     */
    public boolean canBePartOfRoad() {
        return this.pieceType == PieceType.FLAT_STONE || this.pieceType == PieceType.CAPSTONE;
    }

    /**
     * Overrides equals method to compare Pieces based on type and owner.
     *
     * @param obj The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Piece)) return false;
        Piece other = (Piece) obj;
        return this.pieceType == other.pieceType &&
               Objects.equals(this.owner, other.owner);
    }

    /**
     * Overrides hashCode method consistent with equals.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(pieceType, owner);
    }

    /**
     * Creates a copy of this Piece.
     *
     * @return A new Piece instance with the same type and owner.
     */
    public Piece copy(Player ownerCopy) {
        return new Piece(this.pieceType, ownerCopy);
    }   
}
