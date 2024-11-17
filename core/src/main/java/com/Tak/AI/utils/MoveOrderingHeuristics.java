package com.Tak.AI.utils;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;

import java.util.Comparator;
import java.util.List;

/**
 * Utility class for ordering moves to optimize Minimax performance.
 */
public class MoveOrderingHeuristics {

    /**
     * Orders the list of possible actions based on a heuristic.
     *
     * @param moves        The list of possible actions to order.
     * @param board        The current game board.
     * @param player       The current player.
     * @param evalFunction The evaluation function to assess move quality.
     */
    public static void orderMoves(List<Action> moves, Board board, Player player, EvaluationFunction evalFunction) {
        try {
            // Sort the moves by the evaluated score in descending order
            moves.sort(Comparator
                .comparingDouble((Action a) -> {
                    double score = evaluateAction(a, board, player, evalFunction);
                    return Double.isNaN(score) ? Double.NEGATIVE_INFINITY : score; // Treat NaN as lowest value
                })
                .reversed()
                .thenComparingInt(moves::indexOf)); // Maintain original order for ties

            // Debug logging for sorted order
            Logger.debug("MoveOrdering", "Order of moves after sorting:");
            for (Action action : moves) {
                Logger.debug("MoveOrdering", "Action: " + action + " | Score: " +
                    evaluateAction(action, board, player, evalFunction));
            }
        } catch (Exception e) {
            Logger.log("MoveOrdering", "Exception during move ordering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Evaluates the desirability of an action based on various game-specific heuristics.
     *
     * @param action    The action to evaluate.
     * @param board     The current game board.
     * @param player    The current player.
     * @param evalFunc  The evaluation function to use for assessment.
     * @return A numerical value representing the action's desirability.
     */
    private static double evaluateAction(Action action, Board board, Player player, EvaluationFunction evalFunc) {
        double score = 0.0;
        Board hypotheticalBoard = board.copy();
        try {
            action.execute(hypotheticalBoard);
            score = evalFunc.evaluate(hypotheticalBoard, player);

            if (Double.isNaN(score)) {
                score = 0.0; // Default to neutral score if NaN
            }

            // Additional heuristics can be added here if necessary

        } catch (InvalidMoveException e) {
            score = Double.NEGATIVE_INFINITY;
        }
        return score;
    }
}
