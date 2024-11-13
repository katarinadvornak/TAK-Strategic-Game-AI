package com.Tak.Logic;

import java.util.Objects;
import com.Tak.Logic.Piece.PieceType;

/**
 * The HumanPlayer class represents a human player in the game.
 */
public class HumanPlayer extends Player {

    /**
     * Constructs a HumanPlayer with the specified color and piece counts.
     *
     * @param color           The player's color.
     * @param flatStones      The number of flat stones.
     * @param standingStones  The number of standing stones.
     * @param capstones       The number of capstones.
     */
    public HumanPlayer(Color color, int flatStones, int standingStones, int capstones) {
        super(color, flatStones, standingStones, capstones);
    }

    /**
     * Creates a copy of this HumanPlayer.
     *
     * @return A new HumanPlayer instance with the same properties.
     */
    @Override
    public Player copy() {
        HumanPlayer copy = new HumanPlayer(this.getColor(), 
                                          this.getRemainingPieces(Piece.PieceType.FLAT_STONE),
                                          this.getRemainingPieces(Piece.PieceType.STANDING_STONE),
                                          this.getRemainingPieces(Piece.PieceType.CAPSTONE));
        copy.setScore(this.getScore());
        return copy;
    }

    /**
     * Executes a human player's move.
     * Implementation depends on your input handling mechanism.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    @Override
    public void makeMove(TakGame game) throws InvalidMoveException, GameOverException {
        // TODO: Implement human move logic here (e.g., input from console or UI)
        // This method may be handled via UI interactions instead of programmatically.
    }

    /**
     * Overrides equals method to compare HumanPlayers based on Player properties.
     *
     * @param obj The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof HumanPlayer;
    }

    /**
     * Overrides hashCode method consistent with equals.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), "HumanPlayer");
    }
}
