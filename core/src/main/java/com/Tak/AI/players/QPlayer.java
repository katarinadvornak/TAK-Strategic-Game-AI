package com.Tak.AI.players;

import com.Tak.AI.actions.Action;
import com.Tak.AI.actions.Move;
import com.Tak.AI.actions.Placement;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.AI.learning.QLearningAgent;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.Piece.PieceType;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.models.Player.Color;

import java.util.Objects;
import java.util.Scanner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * QPlayer represents an AI player that uses Q-learning for decision-making.
 */
public class QPlayer extends Player implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient Scanner scanner;
    private QLearningAgent qAgent;
    private EvaluationFunction evalFunction;
    private boolean useReinforcementLearning;
    private String lastActionDescription;

    private double explorationRate;
    private double explorationDecay;
    private double minExplorationRate;

    private int roadBuildsThisGame;
    private int blockingsThisGame;

    /**
     * Constructs a QPlayer with specified parameters.
     *
     * @param color                    The color of the player.
     * @param flatStones               Number of flat stones.
     * @param standingStones           Number of standing stones.
     * @param capstones                Number of capstones.
     * @param useReinforcementLearning Flag to use Q-Learning.
     */
    public QPlayer(Color color, int flatStones, int standingStones,
                  int capstones, boolean useReinforcementLearning) {
        super(color, flatStones, standingStones, capstones);
        this.useReinforcementLearning = useReinforcementLearning;

        if (useReinforcementLearning) {
            this.qAgent = QLearningAgent.getInstance();
            this.evalFunction = new EvaluationFunction(qAgent);
        }

        this.explorationRate = 1.0;
        this.explorationDecay = 0.995;
        this.minExplorationRate = 0.01;

        this.roadBuildsThisGame = 0;
        this.blockingsThisGame = 0;
    }

    /**
     * Executes the AI player's move using Q-Learning.
     *
     * @param game The current game instance.
     * @throws InvalidMoveException If the move is invalid.
     * @throws GameOverException    If the game is over.
     */
    @Override
    public void makeMove(TakGame game) throws InvalidMoveException, GameOverException {
        if (useReinforcementLearning) {
            boolean moveMade = false;
            int retryCount = 0;
            int maxRetries = 3;

            while (!moveMade && retryCount < maxRetries) {
                try {
                    Action chosenAction = qAgent.chooseAction(game.getBoard(), this, explorationRate);
                    if (chosenAction != null) {
                        String currentState = qAgent.generateStateHashSymmetrically(game.getBoard(), this);
                        qAgent.recordExperience(currentState, chosenAction);

                        if (chosenAction instanceof Move) {
                            Move moveAction = (Move) chosenAction;
                            game.moveStack(
                                moveAction.getStartX(),
                                moveAction.getStartY(),
                                moveAction.getDirection(),
                                moveAction.getDropCounts().stream().mapToInt(Integer::intValue).toArray()
                            );
                        } else if (chosenAction instanceof Placement) {
                            Placement placementAction = (Placement) chosenAction;
                            game.placePiece(
                                placementAction.getX(),
                                placementAction.getY(),
                                placementAction.getPieceType(),
                                this
                            );
                        } else {
                            throw new InvalidMoveException("Unknown action type.");
                        }

                        double reward = evalFunction.evaluate(game.getBoard(), this);
                        qAgent.updateQTable(game.getBoard(), this, reward, explorationRate);
                        decayExplorationRate();
                        moveMade = true;
                    } else {
                        game.endGameAsTie();
                        moveMade = true;
                    }
                } catch (InvalidMoveException e) {
                    qAgent.updateQTableAfterGame(-100.0);
                    retryCount++;
                }
            }

            if (!moveMade) {
                game.endGameAsTie();
            }
        } else {
            throw new UnsupportedOperationException("Non-reinforcement learning AI is not implemented.");
        }
    }

    /**
     * Re-initializes transient fields after deserialization.
     *
     * @param ois The ObjectInputStream from which the object is being deserialized.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     * @throws IOException            If an I/O error occurs.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Evaluates the board state and assigns a reward.
     *
     * @param board  The current game board.
     * @param player The player for whom the evaluation is being done.
     * @return A numerical score representing the desirability of the board state.
     */
    private double evaluateBoardState(Board board, Player player) {
        return evalFunction.evaluate(board, player);
    }

    /**
     * Gets the description of the last action taken.
     *
     * @return The last action description.
     */
    public String getLastActionDescription() {
        return lastActionDescription;
    }

    /**
     * Decays the exploration rate after each move.
     */
    public void decayExplorationRate() {
        this.explorationRate = Math.max(minExplorationRate, this.explorationRate * explorationDecay);
    }

    /**
     * Saves the AI's current state, including the Q-Table.
     */
    public void saveState() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.saveQTableAsync();
        }
    }

    /**
     * Resets the AI's learning parameters and Q-Table.
     */
    public void resetAI() {
        if (useReinforcementLearning && qAgent != null) {
            qAgent.resetQTable();
        }
    }

    /**
     * Creates a copy of this QPlayer.
     *
     * @return A copy of this QPlayer.
     */
    @Override
    public Player copy() {
        QPlayer copy = new QPlayer(this.getColor(),
                                   this.getRemainingPieces(PieceType.FLAT_STONE),
                                   this.getRemainingPieces(PieceType.STANDING_STONE),
                                   this.getRemainingPieces(PieceType.CAPSTONE),
                                   this.useReinforcementLearning);
        copy.setScore(this.getScore());
        copy.explorationRate = this.explorationRate;
        copy.explorationDecay = this.explorationDecay;
        copy.minExplorationRate = this.minExplorationRate;
        return copy;
    }

    /**
     * Compares QPlayers based on Player properties.
     *
     * @param obj The object to compare.
     * @return true if the players are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof QPlayer;
    }

    /**
     * Computes the hash code for this QPlayer.
     *
     * @return The hash code of this QPlayer.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), "QPlayer");
    }

    /**
     * Retrieves the QLearningAgent instance.
     *
     * @return The QLearningAgent instance.
     */
    public QLearningAgent getQLearningAgent() {
        return this.qAgent;
    }

    /**
     * Returns the total number of pieces left for the player.
     *
     * @return The total number of pieces left.
     */
    @Override
    public int getTotalPiecesLeft() {
        int totalPieces = 0;
        for (PieceType pieceType : PieceType.values()) {
            totalPieces += getRemainingPieces(pieceType);
        }
        return totalPieces;
    }

    /**
     * Shuts down the QLearningAgent's executor service.
     */
    public void shutdownAgent() {
        if (qAgent != null) {
            qAgent.shutdown();
        }
    }

    /**
     * Increments the road builds counter.
     */
    public void incrementRoadBuilds() {
        roadBuildsThisGame++;
    }

    /**
     * Increments the blockings counter.
     */
    public void incrementBlockings() {
        blockingsThisGame++;
    }

    /**
     * Retrieves the number of road builds in the current game.
     *
     * @return The road builds count.
     */
    public int getRoadBuildsThisGame() {
        return roadBuildsThisGame;
    }

    /**
     * Retrieves the number of blockings in the current game.
     *
     * @return The blockings count.
     */
    public int getBlockingsThisGame() {
        return blockingsThisGame;
    }

    /**
     * Resets the per-game metrics.
     */
    public void resetGameMetrics() {
        roadBuildsThisGame = 0;
        blockingsThisGame = 0;
    }
}
