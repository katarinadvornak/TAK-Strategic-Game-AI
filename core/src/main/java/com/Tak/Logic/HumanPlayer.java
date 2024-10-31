package com.Tak.Logic;

/**
 * The HumanPlayer class represents a human player in the Tak game.
 * It extends the Player class.
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
     * Executes the human player's move.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    @Override
    public void makeMove(TakGame game) throws InvalidMoveException, GameOverException {
        // Implementation depends on how you handle human input.
        // For a GUI-based game, this might be empty or involve UI interactions.
        // For now, we'll leave it empty.
    }
}
