// File: core/src/main/java/com/Tak/AI/MoveOrderingHeuristics.java
package com.Tak.AI;

import com.Tak.Logic.Action;
import com.Tak.Logic.Board;
import com.Tak.Logic.InvalidMoveException;
import com.Tak.Logic.Player;
import com.Tak.Logic.Piece;
import com.Tak.Logic.Placement;
import java.util.List;
import java.util.Comparator;

/**
 * Utility class for ordering moves to optimize Minimax performance.
 */
public class MoveOrderingHeuristics {

    /**
     * Orders the list of possible actions based on a heuristic.
     * For example, prioritize capstone placements or moves that could lead to a victory.
     *
     * @param actions The list of possible actions to order.
     * @param board   The current game board.
     * @param player  The current player.
     */
    public static void orderMoves(List<Action> actions, Board board, Player player) {
        EvaluationFunction evalFunc = new EvaluationFunction();
        actions.sort(Comparator.comparingDouble((Action a) -> evaluateAction(a, board, player, evalFunc)).reversed());
    }

    /**
     * Evaluates the desirability of an action.
     * This method can be customized to implement different heuristics.
     *
     * @param action    The action to evaluate.
     * @param board     The current game board.
     * @param player    The current player.
     * @param evalFunc  The evaluation function instance.
     * @return A numerical value representing the action's desirability.
     */
    private static double evaluateAction(Action action, Board board, Player player, EvaluationFunction evalFunc) {
        double score = 0.0;

        if (action instanceof Placement) {
            Placement placement = (Placement) action;
            switch (placement.getPieceType()) {
                case CAPSTONE:
                    score += 5.0; // High priority for capstone placements
                    break;
                case STANDING_STONE:
                    score += 3.0; // Moderate priority
                    break;
                case FLAT_STONE:
                    score += 1.0; // Low priority
                    break;
            }
        } else if (action instanceof MoveAction) {
            MoveAction move = (MoveAction) action;
            // Prioritize moves that extend the player's road
            double preMoveRoad = evalFunc.evaluateRoadPotential(board, player);
            Board hypotheticalBoard = board.copy();
            try {
                move.execute(hypotheticalBoard);
                double postMoveRoad = evalFunc.evaluateRoadPotential(hypotheticalBoard, player);
                score += (postMoveRoad - preMoveRoad) * 2.0;
            } catch (InvalidMoveException e) {
                // Invalid move, assign a negative score
                score += -10.0;
                return score;
            }

            // Prioritize moves that block opponent's road
            Player opponent = player.getOpponent();
            double preMoveOpponentRoad = evalFunc.evaluateRoadPotential(board, opponent);
            double postMoveOpponentRoad = evalFunc.evaluateRoadPotential(hypotheticalBoard, opponent);
            score += (preMoveOpponentRoad - postMoveOpponentRoad) * 2.0;

            // Penalize moves that decrease player's mobility
            double preMoveMobility = evalFunc.evaluatePieceMobility(board, player);
            double postMoveMobility = evalFunc.evaluatePieceMobility(hypotheticalBoard, player);
            score += (postMoveMobility - preMoveMobility) * 1.0;
        }

        return score;
    }

    // TODO: Optimize evaluation to reduce the need for full move execution
    // Consider evaluating based on potential gains without executing the move
}
