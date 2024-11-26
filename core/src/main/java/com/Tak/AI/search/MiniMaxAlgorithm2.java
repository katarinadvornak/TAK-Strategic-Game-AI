// File: com/Tak/AI/search/MiniMaxAlgorithm2.java
package com.Tak.AI.search;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.AI.utils.ActionGenerator;
import com.Tak.AI.utils.MoveOrderingHeuristics;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the Minimax algorithm with Alpha-Beta Pruning for decision-making in the TAK game.
 * Now includes complexity analysis tracking.
 */
public class MiniMaxAlgorithm2 implements Serializable {
    private static final long serialVersionUID = 1L;

    private EvaluationFunction evalFunction;
    private final int maxDepth;
    private final Player aiPlayer;
    private Map<String, Double> transpositionTable;
    private final long timeLimitMillis = 20000;
    private long startTime;

    // Complexity metrics
    private int nodesEvaluated;
    private long timeTakenMillis;
    private int pruneCount; // Counts how many times pruning occurred

    public boolean useMoveOrdering; // Flag to enable/disable move ordering

    /**
     * Constructs a MinimaxAlgorithm with the specified evaluation function, maximum depth, AI player, and move ordering option.
     *
     * @param evalFunction     The evaluation function to assess board states.
     * @param maxDepth         The maximum search depth.
     * @param aiPlayer         The AIPlayer instance.
     * @param useMoveOrdering  Flag to enable or disable move ordering.
     */
    public MiniMaxAlgorithm2(EvaluationFunction evalFunction, int maxDepth, Player aiPlayer, boolean useMoveOrdering) {
        this.evalFunction = evalFunction;
        this.maxDepth = maxDepth;
        this.aiPlayer = aiPlayer;
        this.transpositionTable = new HashMap<>();
        this.nodesEvaluated = 0;
        this.timeTakenMillis = 0;
        this.pruneCount = 0;
        this.useMoveOrdering = useMoveOrdering;
    }

    /**
     * Finds the best move for the AIPlayer using the Minimax algorithm with Alpha-Beta Pruning.
     * Also tracks complexity metrics.
     *
     * @param board            The current game board.
     * @param player           The AIPlayer making the move.
     * @param currentMoveCount The current move count in the game.
     * @return The best Action to take, or null if no valid actions are available.
     */
    public Action findBestMove(Board board, Player player, int currentMoveCount) {
        Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        startTime = System.currentTimeMillis();
        nodesEvaluated = 0; // Reset node count
        pruneCount = 0; // Reset prune count

        // Generate all possible moves from the current state at depth 0
        List<String> possibleActionStrings = ActionGenerator.generatePossibleActions(board, player, currentMoveCount);
        List<Action> possibleActions = parseActionStrings(possibleActionStrings, player);

        // Order moves if enabled
        if (useMoveOrdering) {
            MoveOrderingHeuristics.orderMoves(possibleActions, board, player, evalFunction, true); // Root node is maximizing
        }

        // Initialize variables to track time limit and best move
        boolean timeLimitExceeded = false;

        for (Action action : possibleActions) {
            if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                timeLimitExceeded = true;
                break;
            }

            Board boardAfterAction = board.copy();
            try {
                action.execute(boardAfterAction);
                double moveValue = minimax(boardAfterAction, maxDepth - 1, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, player, currentMoveCount + 1);

                if (moveValue > bestValue || bestAction == null) {
                    bestValue = moveValue;
                    bestAction = action;
                }
            } catch (InvalidMoveException e) {
                continue;
            } catch (InterruptedException e) {
                timeLimitExceeded = true;
                break;
            }
        }

        if (bestAction == null) {
            // Fallback move selection
            if (!possibleActions.isEmpty()) {
                bestAction = possibleActions.get(0); // Choose the first available move
            } else {
                throw new IllegalStateException("No valid moves available.");
            }
        }

        timeTakenMillis = System.currentTimeMillis() - startTime;
        return bestAction;
    }

    /**
     * The core Minimax recursive function with Alpha-Beta Pruning.
     *
     * @param board        The current game board.
     * @param depth        The remaining search depth.
     * @param isMaximizing Indicates whether the current layer is maximizing or minimizing.
     * @param alpha        The best already explored option for the maximizer.
     * @param beta         The best already explored option for the minimizer.
     * @param player       The AIPlayer instance.
     * @param moveCount    The current move count in the game.
     * @return The evaluation score of the board state.
     */
    private double minimax(Board board, int depth, boolean isMaximizing, double alpha, double beta, Player player, int moveCount) throws InterruptedException {
        if (System.currentTimeMillis() - startTime > timeLimitMillis) {
            throw new InterruptedException("Time limit exceeded during minimax.");
        }

        nodesEvaluated++; // Increment node count

        // Terminal condition
        double currentEval = evalFunction.evaluate(board, aiPlayer);
        if (depth == 0 || board.isFull() || currentEval == Double.POSITIVE_INFINITY || currentEval == Double.NEGATIVE_INFINITY) {
            return currentEval;
        }

        Player currentPlayer = isMaximizing ? player : player.getOpponent();
        List<String> possibleActionStrings = ActionGenerator.generatePossibleActions(board, currentPlayer, moveCount);
        List<Action> possibleActions = parseActionStrings(possibleActionStrings, currentPlayer);

        // Order moves if enabled
        if (useMoveOrdering) {
            MoveOrderingHeuristics.orderMoves(possibleActions, board, currentPlayer, evalFunction, isMaximizing);
        }

        if (isMaximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Action action : possibleActions) {
                if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                    throw new InterruptedException("Time limit exceeded during minimax.");
                }

                Board boardAfterAction = board.copy();
                try {
                    action.execute(boardAfterAction);
                    double eval = minimax(boardAfterAction, depth - 1, false, alpha, beta, player, moveCount + 1);

                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);

                    if (beta <= alpha) {
                        pruneCount++;
                        break;
                    }
                } catch (InvalidMoveException e) {
                    continue;
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Action action : possibleActions) {
                if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                    throw new InterruptedException("Time limit exceeded during minimax.");
                }

                Board boardAfterAction = board.copy();
                try {
                    action.execute(boardAfterAction);
                    double eval = minimax(boardAfterAction, depth - 1, true, alpha, beta, player, moveCount + 1);

                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);

                    if (beta <= alpha) {
                        pruneCount++;
                        break;
                    }
                } catch (InvalidMoveException e) {
                    continue;
                }
            }
            return minEval;
        }
    }

    /**
     * Parses action strings into Action objects.
     *
     * @param actionStrings The list of action strings.
     * @param player        The player performing the actions.
     * @return A list of valid Action objects.
     */
    private List<Action> parseActionStrings(List<String> actionStrings, Player player) {
        List<Action> actions = new ArrayList<>();
        for (String actionStr : actionStrings) {
            try {
                Action action = Action.fromString(actionStr, player.getColor());
                actions.add(action);
            } catch (InvalidMoveException e) {
                continue;
            }
        }
        return actions;
    }

    /**
     * Clears the transposition table, useful when resetting the AI or starting a new game.
     */
    public void clearTranspositionTable() {
        transpositionTable.clear();
    }

    /**
     * Retrieves the total number of nodes evaluated during the last search.
     *
     * @return The number of nodes evaluated.
     */
    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

    /**
     * Retrieves the time taken (in milliseconds) during the last search.
     *
     * @return The time taken in milliseconds.
     */
    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }

    /**
     * Retrieves the number of times alpha-beta pruning occurred during the last search.
     *
     * @return The number of pruning events.
     */
    public int getPruneCount() {
        return pruneCount;
    }
}
