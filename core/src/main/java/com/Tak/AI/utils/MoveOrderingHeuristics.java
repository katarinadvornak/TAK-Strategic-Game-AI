package com.Tak.AI.utils;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import com.Tak.Logic.models.Piece.PieceType;
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
            // Sort the moves by the evaluated score in descending order, handle NaN safely, and keep original order as a tiebreaker
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
            e.printStackTrace(); // Optional, for more detailed error trace
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

            if (action instanceof com.Tak.AI.actions.Move) {
                com.Tak.AI.actions.Move move = (com.Tak.AI.actions.Move) action;
                score += move.getDropCounts().stream().mapToInt(Integer::intValue).sum() * 0.1;
            } else if (action instanceof com.Tak.AI.actions.Placement) {
                com.Tak.AI.actions.Placement placement = (com.Tak.AI.actions.Placement) action;
                int center = board.getSize() / 2;
                int distance = Math.abs(placement.getX() - center) + Math.abs(placement.getY() - center);
                score += (board.getSize() - distance) * 0.5;
            }

            if (evalFunc.isWinningMove(score, player)) {
                score += 1000.0;
            }

            if (evalFunc.isBlockingMove(score, player, hypotheticalBoard)) {
                score += 500.0;
            }

            if (Double.isNaN(score)) {
                score = Double.NEGATIVE_INFINITY;
            }
        } catch (InvalidMoveException e) {
            score = Double.NEGATIVE_INFINITY;
        }
        return score;
    }
}
