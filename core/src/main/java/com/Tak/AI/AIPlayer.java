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
    private ReinforcementLearningAgent rlAgent; // Optional, if using reinforcement learning
    private boolean useReinforcementLearning;   // Flag to switch between AI strategies

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
        this.useReinforcementLearning = false; // Default to Minimax
    }

    /**
     * Constructs an AIPlayer using reinforcement learning.
     *
     * @param color           The player's color.
     * @param flatStones      The number of flat stones.
     * @param standingStones  The number of standing stones.
     * @param capstones       The number of capstones.
     * @param rlAgent         The reinforcement learning agent.
     */
    public AIPlayer(Color color, int flatStones, int standingStones, int capstones, ReinforcementLearningAgent rlAgent) {
        super(color, flatStones, standingStones, capstones);
        this.rlAgent = rlAgent;
        this.useReinforcementLearning = true;
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
        if (useReinforcementLearning) {
            makeReinforcementLearningMove(game);
        } else {
            makeMinimaxMove(game);
        }
    }

    /**
     * Makes a move using the Minimax algorithm.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     */
    private void makeMinimaxMove(TakGame game) throws InvalidMoveException {
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

    /**
     * Makes a move using reinforcement learning.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     */
    private void makeReinforcementLearningMove(TakGame game) throws InvalidMoveException {
        Board currentState = game.getBoard();
        Move action = rlAgent.selectAction(currentState, this);

        if (action != null) {
            action.execute(currentState);

            // Get the new state
            Board nextState = currentState.copy(); // Make a copy if necessary

            // Calculate reward
            double reward = evaluateReward(currentState, action, nextState, game);

            // Update the Q-value
            rlAgent.updateQValue(currentState, action, reward, nextState);

            // Decay exploration rate if needed
            rlAgent.decayExplorationRate();

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

    /**
     * Evaluates the reward received after making a move.
     *
     * @param currentState The game state before the move.
     * @param action       The action taken.
     * @param nextState    The game state after the move.
     * @param game         The current TakGame instance.
     * @return The reward value.
     */
    private double evaluateReward(Board currentState, Move action, Board nextState, TakGame game) {
        // Placeholder method body
        // Implement logic to evaluate the reward
        return 0.0;
    }

    // Getters and setters for AI-specific attributes

    public int getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public void useReinforcementLearning(ReinforcementLearningAgent rlAgent) {
        this.rlAgent = rlAgent;
        this.useReinforcementLearning = true;
    }

    public void useMinimax() {
        this.rlAgent = null;
        this.useReinforcementLearning = false;
    }
}
