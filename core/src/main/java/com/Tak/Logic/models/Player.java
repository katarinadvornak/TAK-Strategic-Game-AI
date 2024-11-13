// File: core/src/main/java/com/Tak/Logic/models/Player.java
package com.Tak.Logic.models;

import java.util.Objects;
import java.io.Serializable;

import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;

/**
 * The Player class represents a player in the Tak game.
 * It serves as an abstract base class for different player types.
 */
public abstract class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Color { BLACK, WHITE }

    private Color color;
    private Player opponent;
    private int flatStones;
    private int standingStones;
    private int capstones;
    protected int score; // Changed to protected to allow subclasses to access

    /**
     * Constructor to initialize a player.
     *
     * @param color           The color of the player (BLACK or WHITE).
     * @param flatStones      Number of flat stones.
     * @param standingStones  Number of standing stones.
     * @param capstones       Number of capstones.
     */
    public Player(Color color, int flatStones, int standingStones, int capstones) {
        this.color = color;
        this.flatStones = flatStones;
        this.standingStones = standingStones;
        this.capstones = capstones;
        this.score = 0;
    }

    /**
     * Sets the opponent player.
     *
     * @param opponent The opponent player.
     */
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    /**
     * Gets the opponent player.
     *
     * @return The opponent player.
     */
    public Player getOpponent() {
        return this.opponent;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    /**
     * Decrements the count of a specific piece type.
     *
     * @param pieceType The type of piece to decrement.
     */
    public void decrementPiece(Piece.PieceType pieceType) {
        switch (pieceType) {
            case FLAT_STONE:
                if (flatStones > 0) {
                    flatStones--;
                }
                break;
            case STANDING_STONE:
                if (standingStones > 0) {
                    standingStones--;
                }
                break;
            case CAPSTONE:
                if (capstones > 0) {
                    capstones--;
                }
                break;
        }
    }

    /**
     * Increments the count of a specific piece type.
     *
     * @param pieceType The type of piece to increment.
     */
    public void incrementPiece(Piece.PieceType pieceType) {
        switch (pieceType) {
            case FLAT_STONE:
                flatStones++;
                break;
            case STANDING_STONE:
                standingStones++;
                break;
            case CAPSTONE:
                capstones++;
                break;
        }
    }

    public boolean hasPieces(Piece.PieceType pieceType) {
        return hasPiecesLeft(pieceType);
    }
    
    public boolean hasPiecesLeft(Piece.PieceType pieceType) {
        switch (pieceType) {
            case FLAT_STONE:
                return flatStones > 0;
            case STANDING_STONE:
                return standingStones > 0;
            case CAPSTONE:
                return capstones > 0;
            default:
                return false;
        }
    }
    

    /**
     * Gets the number of remaining pieces of a specific type.
     *
     * @param pieceType The type of piece.
     * @return The number of remaining pieces.
     */
    public int getRemainingPieces(Piece.PieceType pieceType) {
        switch (pieceType) {
            case FLAT_STONE:
                return flatStones;
            case STANDING_STONE:
                return standingStones;
            case CAPSTONE:
                return capstones;
            default:
                return 0;
        }
    }

    /**
     * Resets the player's pieces to the specified counts.
     *
     * @param flat     Number of flat stones.
     * @param standing Number of standing stones.
     * @param capstone Number of capstones.
     */
    public void resetPieces(int flat, int standing, int capstone) {
        this.flatStones = flat;
        this.standingStones = standing;
        this.capstones = capstone;
        System.out.println(this.color + " pieces have been reset.");
    }

    /**
     * Resets the player's score.
     */
    public void resetScore() {
        this.score = 0;
        System.out.println(this.color + " score has been reset.");
    }

    /**
     * Increments the player's score.
     *
     * @param points The number of points to add.
     */
    public void incrementScore(int points) {
        this.score += points;
        System.out.println(this.color + " score increased by " + points + " to " + this.score + ".");
    }

    /**
     * Gets the player's color.
     *
     * @return The player's color.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Gets the player's current score.
     *
     * @return The player's score.
     */
    public int getScore() {
        return this.score;
    }

    /**
     * Abstract method to create a copy of the player.
     *
     * @return A new Player instance with the same properties.
     */
    public abstract Player copy();

    /**
     * Abstract method to execute the player's move.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    public abstract void makeMove(TakGame game) throws InvalidMoveException, GameOverException;

    /**
     * Overrides equals method to compare Players based on unique attributes.
     *
     * @param obj The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Player)) return false;
        Player other = (Player) obj;
        return this.color == other.color;
    }

    /**
     * Overrides hashCode method consistent with equals.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(color);
    }

    public abstract int getTotalPiecesLeft();
}
