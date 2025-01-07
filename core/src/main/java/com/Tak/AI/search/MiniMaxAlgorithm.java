package com.Tak.AI.search;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.IEvaluationFunction;
import com.Tak.AI.utils.ActionGenerator;
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
 * Now referencing IEvaluationFunction instead of a concrete class.
 */
public class MiniMaxAlgorithm implements Serializable {
    private static final long serialVersionUID = 1L;

    private IEvaluationFunction evalFunction; 
    private final int maxDepth;
    private final Player aiPlayer;
    private Map<String, Double> transpositionTable;
    private final long timeLimitMillis = 20000;
    private long startTime;

    // Complexity metrics
    private int nodesEvaluated;
    private long timeTakenMillis;
    private int pruneCount; 

    public boolean useMoveOrdering; // Flag to enable/disable move ordering

    /**
     * Constructs a MiniMaxAlgorithm with the specified evaluation function,
     * maximum depth, AI player, and move ordering option.
     */
    public MiniMaxAlgorithm(IEvaluationFunction evalFunction,
                             int maxDepth,
                             Player aiPlayer,
                             boolean useMoveOrdering) {
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
     * Public getter to allow direct access to the evaluation function.
     * Needed for "findWorstMove" logic in MinimaxAgent.
     */
    public IEvaluationFunction getEvalFunction() {
        return this.evalFunction;
    }

    /**
     * Finds the best move for the AI player using Minimax with Alpha-Beta Pruning.
     */
    public Action findBestMove(Board board, Player player, int currentMoveCount) {
        Action bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        startTime = System.currentTimeMillis();
        nodesEvaluated = 0;
        pruneCount = 0;

        // Generate all possible moves
        List<String> possibleActionStrings = ActionGenerator.generatePossibleActions(board, player, currentMoveCount);
        List<Action> possibleActions = parseActionStrings(possibleActionStrings, player);

        boolean timeLimitExceeded = false;

        for (Action action : possibleActions) {
            if (System.currentTimeMillis() - startTime > timeLimitMillis) {
                timeLimitExceeded = true;
                break;
            }

            Board boardAfterAction = board.copy();
            try {
                action.execute(boardAfterAction);

                double moveValue = minimax(
                    boardAfterAction, 
                    maxDepth - 1, 
                    false,
                    Double.NEGATIVE_INFINITY, 
                    Double.POSITIVE_INFINITY,
                    player, 
                    currentMoveCount + 1
                );

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
            if (!possibleActions.isEmpty()) {
                // Fall back to the first valid action if none was better
                bestAction = possibleActions.get(0);
            } else {
                throw new IllegalStateException("No valid moves available.");
            }
        }

        timeTakenMillis = System.currentTimeMillis() - startTime;
        return bestAction;
    }

    /**
     * The core Minimax recursive function with Alpha-Beta Pruning.
     */
    private double minimax(Board board,
                           int depth,
                           boolean isMaximizing,
                           double alpha,
                           double beta,
                           Player player,
                           int moveCount) throws InterruptedException {

        // Time-limit check
        if (System.currentTimeMillis() - startTime > timeLimitMillis) {
            throw new InterruptedException("Time limit exceeded during minimax.");
        }

        nodesEvaluated++;

        double currentEval = evalFunction.evaluate(board, aiPlayer);

        // Terminal conditions
        if (depth == 0 || board.isFull()
            || currentEval == Double.POSITIVE_INFINITY
            || currentEval == Double.NEGATIVE_INFINITY) {
            return currentEval;
        }

        // Determine which player is acting at this depth
        Player currentPlayer = isMaximizing ? player : player.getOpponent();

        List<String> possibleActionStrings = ActionGenerator.generatePossibleActions(board, currentPlayer, moveCount);
        List<Action> possibleActions = parseActionStrings(possibleActionStrings, currentPlayer);

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
     * Converts raw action strings into Action objects.
     */
    public List<Action> parseActionStrings(List<String> actionStrings, Player player) {
        List<Action> actions = new ArrayList<>();
        for (String actionStr : actionStrings) {
            try {
                Action action = Action.fromString(actionStr, player.getColor());
                actions.add(action);
            } catch (InvalidMoveException e) {
                // Skip invalid
            }
        }
        return actions;
    }

    public void clearTranspositionTable() {
        transpositionTable.clear();
    }

    public int getNodesEvaluated() {
        return nodesEvaluated;
    }

    public long getTimeTakenMillis() {
        return timeTakenMillis;
    }

    public int getPruneCount() {
        return pruneCount;
    }
}
