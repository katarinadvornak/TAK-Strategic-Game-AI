package com.Tak.AI;

import com.Tak.Logic.*;
import com.Tak.Logic.Player.Color;

/**
 * The AIPlayer class represents an AI-controlled player in the Tak game.
 * It extends the Player class and uses AI algorithms to decide moves.
 */
public class AIPlayer extends Player {

    private int searchDepth; // Depth for the Minimax algorithm
    private MinimaxAlgorithm minimaxAlgorithm;

    /**
     * Constructs an AIPlayer with the specified color and search depth.
     *
     * @param color           The player's color.
     * @param flatStones      The number of flat stones.
     * @param standingStones  The number of standing stones.
     * @param capstones       The number of capstones.
     * @param searchDepth     The depth to which the Minimax algorithm will search.
     */
    public AIPlayer(Color color, int flatStones, int standingStones, int capstones, int searchDepth) {
        super(color, flatStones, standingStones, capstones);
        this.searchDepth = searchDepth;
        this.minimaxAlgorithm = new MinimaxAlgorithm();
    }

    /**
     * Determines and executes the AI's move based on the current game state.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If the AI attempts an invalid move.
     * @throws GameOverException    If the game has already ended.
     */
    @Override
    public void makeMove(TakGame game) throws InvalidMoveException, GameOverException {
        Board board = game.getBoard();
        Move bestMove = minimaxAlgorithm.findBestMove(board, this, searchDepth);
        if (bestMove != null) {
            bestMove.execute(board);

            // Update the game state
            game.incrementMoveCount();
            game.checkWinConditions();
            if (!game.isGameEnded()) {
                game.switchPlayer();
            }
        } else {
            throw new InvalidMoveException("AI could not find a valid move.");
        }
    }

    // Getters and setters for AI-specific attributes

    public int getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }
}
