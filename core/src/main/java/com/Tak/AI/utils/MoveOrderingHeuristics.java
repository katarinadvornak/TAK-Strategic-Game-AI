// File: com/Tak/AI/utils/MoveOrderingHeuristics.java
package com.Tak.AI.utils;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;

import java.util.Comparator;
import java.util.List;

/**
 * Provides utilities for ordering moves to enhance alpha-beta pruning efficiency.
 */
public class MoveOrderingHeuristics {

    /**
     * Orders the list of actions based on their evaluation scores.
     * Higher scoring moves are placed first for the maximizing player,
     * and lower scoring moves are placed first for the minimizing player.
     *
     * @param actions        The list of possible actions to be ordered.
     * @param board          The current game board.
     * @param player         The player making the moves.
     * @param evaluationFunc The evaluation function to assess the board.
     * @param isMaximizing   Indicates if the current node is maximizing or minimizing.
     */
    public static void orderMoves(List<Action> actions, Board board, Player player, EvaluationFunction evaluationFunc, boolean isMaximizing) {
        actions.sort(new Comparator<Action>() {
            @Override
            public int compare(Action a1, Action a2) {
                double score1 = evaluateAction(a1, board, player, evaluationFunc, isMaximizing);
                double score2 = evaluateAction(a2, board, player, evaluationFunc, isMaximizing);
                // Sort descending for maximizing, ascending for minimizing
                return isMaximizing ? Double.compare(score2, score1) : Double.compare(score1, score2);
            }

            /**
             * Evaluates the potential score of executing an action.
             *
             * @param action         The action to evaluate.
             * @param currentBoard   The current game board.
             * @param player         The player making the action.
             * @param evaluationFunc The evaluation function to assess the board.
             * @param isMaximizing   Indicates if the current node is maximizing or minimizing.
             * @return The evaluation score after executing the action.
             */
            private double evaluateAction(Action action, Board currentBoard, Player player, EvaluationFunction evaluationFunc, boolean isMaximizing) {
                Board boardCopy = currentBoard.copy();
                try {
                    action.execute(boardCopy);
                } catch (Exception e) {
                    // Assign extreme values based on node type to deprioritize invalid moves
                    return isMaximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                return evaluationFunc.evaluate(boardCopy, player);
            }
        });
    }
}
