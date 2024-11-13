// File: core/src/main/java/com/Tak/AI/AIPlayer.java
package com.Tak.AI;

import com.Tak.Logic.*;
import com.Tak.Logic.Piece.PieceType;
import com.Tak.utils.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

/**
 * The AIPlayer class represents an AI-controlled player utilizing Minimax with Alpha-Beta Pruning
 * and Reinforcement Learning (Q-Learning) strategies to make intelligent moves in the TAK game.
 */
public class AIPlayer extends Player {
    
    private EvaluationFunction evalFunction;
    private int searchDepth;
    private boolean useMinimax;
    private MinimaxAlgorithm minimax;
    
    // Q-Learning Components
    private QLearningAgent qAgent;
    private boolean useReinforcementLearning;
    
    private Random random;
    private double explorationRate; // Epsilon in ε-greedy
    private double explorationDecay;
    private double minExplorationRate;
    
    /**
     * Constructs an AIPlayer with specified parameters.
     *
     * @param color                   The player's color (BLACK or WHITE).
     * @param flatStones              Number of flat stones.
     * @param standingStones          Number of standing stones.
     * @param capstones               Number of capstones.
     * @param searchDepth             The depth limit for Minimax search.
     * @param useMinimax              Flag to use Minimax strategy.
     * @param useReinforcementLearning Flag to use Reinforcement Learning.
     */
    public AIPlayer(Color color, int flatStones, int standingStones, int capstones, int searchDepth, 
                    boolean useMinimax, boolean useReinforcementLearning) {
        super(color, flatStones, standingStones, capstones);
        this.searchDepth = searchDepth;
        this.useMinimax = useMinimax;
        this.useReinforcementLearning = useReinforcementLearning;
        this.evalFunction = new EvaluationFunction();
        this.minimax = new MinimaxAlgorithm(evalFunction, searchDepth, this);
        
        if (useReinforcementLearning) {
            this.qAgent = new QLearningAgent();
            this.qAgent.loadQTable(); // Load existing Q-table if available
        }
        
        this.random = new Random();
        this.explorationRate = 1.0; // Start with full exploration
        this.explorationDecay = 0.995;
        this.minExplorationRate = 0.01;
    }
    
    /**
     * Executes the AI player's move using the selected strategy.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    @Override
    public void makeMove(TakGame game) throws InvalidMoveException, GameOverException {
        if (useMinimax) {
            performMinimaxMove(game);
        } else if (useReinforcementLearning) {
            performQLearningMove(game);
        } else {
            throw new UnsupportedOperationException("No AI strategy selected.");
        }
    }
    
    /**
     * Performs a move using the Minimax algorithm with Alpha-Beta Pruning.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    private void performMinimaxMove(TakGame game) throws InvalidMoveException, GameOverException {
        Logger.log("AIPlayer", getColor() + " AI is making a move using Minimax.");
    
        // Log available pieces
        Logger.log("AIPlayer", "Available pieces for " + getColor() + ":");
        for (PieceType type : PieceType.values()) {
            Logger.log("AIPlayer", type + ": " + this.getRemainingPieces(type));
        }
    
        Action bestAction = minimax.findBestMove(game.getBoard(), this, game.getMoveCount());
    
        if (bestAction != null) {
            Logger.log("AIPlayer", getColor() + " AI selected action: " + bestAction.toString());
            try {
                bestAction.execute(game.getBoard());
    
                // Handle piece count updates based on action type
                if (bestAction instanceof Placement) {
                    Placement placement = (Placement) bestAction;
                    this.decrementPiece(placement.getPieceType());
                    Logger.log("AIPlayer", "Decremented " + placement.getPieceType() + ". Remaining: " + this.getRemainingPieces(placement.getPieceType()));
                }
                // For MoveAction, additional logic can be implemented if needed
    
                Logger.log("AIPlayer", getColor() + " AI executed action: " + bestAction.toString());
                game.incrementMoveCount();
                game.checkWinConditions();
                game.switchPlayer();
            } catch (InvalidMoveException e) {
                Logger.log("AIPlayer", "Move execution failed: " + e.getMessage());
                throw e; // Re-throw to handle in higher context
            }
        } else {
            throw new InvalidMoveException("AI could not determine a valid move using Minimax.");
        }
    }
    
    
    /**
     * Performs a move using the Q-Learning agent.
     *
     * @param game The current TakGame instance.
     * @throws InvalidMoveException If an invalid move is attempted.
     * @throws GameOverException    If the game has already ended.
     */
    private void performQLearningMove(TakGame game) throws InvalidMoveException, GameOverException {
        Logger.log("AIPlayer", getColor() + " AI is making a move using Q-Learning.");
        Board currentBoard = game.getBoard();
        String state = qAgent.generateStateHash(currentBoard, this);
        
        // Decide whether to explore or exploit
        boolean explore = random.nextDouble() < explorationRate;
        Action selectedAction;
        
        if (explore) {
            Logger.log("AIPlayer", "AI is exploring.");
            selectedAction = qAgent.selectRandomAction(currentBoard, this);
        } else {
            Logger.log("AIPlayer", "AI is exploiting.");
            selectedAction = qAgent.selectBestAction(state);
        }
        
        if (selectedAction != null) {
            Logger.log("AIPlayer", "AI selected action: " + selectedAction.toString());
            try {
                selectedAction.execute(currentBoard);
                Logger.log("AIPlayer", "AI executed action: " + selectedAction.toString());
                
                // Handle piece count updates based on action type
                if (selectedAction instanceof Placement) {
                    Placement placement = (Placement) selectedAction;
                    this.decrementPiece(placement.getPieceType());
                    Logger.log("AIPlayer", "Decremented " + placement.getPieceType() + ". Remaining: " + this.getRemainingPieces(placement.getPieceType()));
                }
                // For MoveAction, additional logic can be implemented if needed
                
                // Observe new state and reward
                String newState = qAgent.generateStateHash(currentBoard, this);
                double reward = evaluateReward(game);
                Logger.log("AIPlayer", "Received Reward: " + reward);
                
                // Update Q-Table
                qAgent.updateQTable(state, selectedAction, reward, newState);
                
                // Decay exploration rate
                if (explorationRate > minExplorationRate) {
                    explorationRate *= explorationDecay;
                    explorationRate = Math.max(explorationRate, minExplorationRate);
                }
                
                Logger.log("AIPlayer", "AI exploration rate updated to: " + explorationRate);
                
                game.incrementMoveCount();
                game.checkWinConditions();
                game.switchPlayer();
            } catch (InvalidMoveException e) {
                Logger.log("AIPlayer", "Move execution failed: " + e.getMessage());
                throw e; // Re-throw to handle in higher context
            }
        } else {
            throw new InvalidMoveException("AI could not determine a valid move using Q-Learning.");
        }
    }
    
    /**
     * Evaluates the reward based on the current game state.
     *
     * @param game The current TakGame instance.
     * @return The calculated reward.
     */
    private double evaluateReward(TakGame game) {
        if (game.isGameEnded()) {
            if (game.getWinner() == this) {
                return 100.0; // Win
            } else if (game.getWinner() == null) {
                return 0.0; // Tie
            } else {
                return -100.0; // Loss
            }
        }
        
        // Intermediate rewards can be based on evaluation function
        double evalScore = evalFunction.evaluate(game.getBoard(), this);
        return evalScore;
    }
    
    /**
     * Saves the AI's current state, including the Q-Table.
     */
    public void saveState() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.saveQTable();
            Logger.log("AIPlayer", "AI's state has been saved.");
        }
    }
    
    /**
     * Resets the AI's learning parameters and Q-Table.
     */
    public void resetAI() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.resetQTable();
            explorationRate = 1.0;
            Logger.log("AIPlayer", "AI's learning has been reset.");
        }
    }
    
    /**
     * Saves the Q-Table to persistent storage.
     */
    public void saveQTable() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.saveQTable();
            Logger.log("AIPlayer", "AI's Q-Table has been saved.");
        }
    }
    
    /**
     * Loads the Q-Table from persistent storage.
     */
    public void loadQTable() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.loadQTable();
            Logger.log("AIPlayer", "AI's Q-Table has been loaded.");
        }
    }
    
    /**
     * Resets the AI's learning parameters and Q-Table.
     */
    public void resetLearning() {
        resetAI();
    }
    
    /**
     * Creates a copy of this AIPlayer.
     *
     * @return A new AIPlayer instance with the same properties.
     */
    @Override
    public Player copy() {
        AIPlayer copy = new AIPlayer(this.getColor(), 
                                     this.getRemainingPieces(PieceType.FLAT_STONE),
                                     this.getRemainingPieces(PieceType.STANDING_STONE),
                                     this.getRemainingPieces(PieceType.CAPSTONE),
                                     this.searchDepth,
                                     this.useMinimax,
                                     this.useReinforcementLearning);
        copy.setScore(this.getScore());
        if (this.useReinforcementLearning && this.qAgent != null) {
            copy.qAgent = this.qAgent.copy();
        }
        copy.evalFunction = this.evalFunction.copy(); // Ensure EvaluationFunction has a copy method
        copy.minimax = new MinimaxAlgorithm(copy.evalFunction, this.searchDepth, copy); // Assign new MinimaxAlgorithm without copying AIPlayer
        copy.explorationRate = this.explorationRate;
        copy.explorationDecay = this.explorationDecay;
        copy.minExplorationRate = this.minExplorationRate;
        return copy;
    }
    
    /**
     * Overrides equals method to compare AIPlayers based on Player properties.
     *
     * @param obj The object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof AIPlayer;
    }
    
    /**
     * Overrides hashCode method consistent with equals.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), "AIPlayer");
    }
}
