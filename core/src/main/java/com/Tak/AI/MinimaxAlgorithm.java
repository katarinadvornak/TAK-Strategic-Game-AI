// File: core/src/main/java/com/Tak/AI/MinimaxAlgorithm.java
package com.Tak.AI;

import com.Tak.Logic.*;
import com.Tak.Logic.Piece.PieceType;
import com.Tak.utils.Logger;

import java.util.List;
import java.util.ArrayList;

/**
 * The MinimaxAlgorithm class implements the Minimax algorithm with Alpha-Beta Pruning
 * for decision-making in the TAK game.
 */
public class MinimaxAlgorithm {
    
    private EvaluationFunction evalFunction;
    private int maxDepth;
    private AIPlayer aiPlayer;
    
    /**
     * Constructs a MinimaxAlgorithm with the specified evaluation function and search depth.
     *
     * @param evalFunction The evaluation function to score game states.
     * @param maxDepth     The maximum depth to search the game tree.
     * @param aiPlayer     The AIPlayer instance using this algorithm.
     */
    public MinimaxAlgorithm(EvaluationFunction evalFunction, int maxDepth, AIPlayer aiPlayer) {
        this.evalFunction = evalFunction;
        this.maxDepth = maxDepth;
        this.aiPlayer = aiPlayer;
    }
    
    /**
     * Finds the best move for the AIPlayer using the Minimax algorithm.
     *
     * @param board         The current game board.
     * @param player        The AIPlayer instance.
     * @param currentMoveCount The current move count in the game.
     * @return The best Action to perform.
     */
    public Action findBestMove(Board board, AIPlayer player, int currentMoveCount) {
        double bestValue = Double.NEGATIVE_INFINITY;
        Action bestAction = null;
        
        // Utilize ActionGenerator to get all possible actions (placements and moves)
        List<Action> possibleActions = ActionGenerator.generatePossibleActions(board, player, currentMoveCount);
        
        // Apply move ordering heuristics
        MoveOrderingHeuristics.orderMoves(possibleActions, board, player);
        
        for (Action action : possibleActions) {
            Board clonedBoard = board.copy();
            try {
                action.execute(clonedBoard);
                double moveValue = minimax(clonedBoard, maxDepth - 1, false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, player);
                Logger.log("Minimax", "Action: " + action.toString() + " | Value: " + moveValue);
                if (moveValue > bestValue) {
                    bestValue = moveValue;
                    bestAction = action;
                }
            } catch (InvalidMoveException e) {
                Logger.log("Minimax", "Invalid move encountered during Minimax: " + e.getMessage());
                continue;
            }
        }
        return bestAction;
    }
    
    
    /**
     * The core Minimax recursive function with Alpha-Beta Pruning.
     *
     * @param board        The current game board.
     * @param depth        The remaining depth to search.
     * @param isMaximizing Flag indicating whether the current layer is maximizing or minimizing.
     * @param alpha        The Alpha value for pruning.
     * @param beta         The Beta value for pruning.
     * @param player       The AIPlayer instance.
     * @return The evaluated score of the board.
     */
    private double minimax(Board board, int depth, boolean isMaximizing, double alpha, double beta, AIPlayer player) {
        if (depth == 0 || board.isFull() || new RoadConnectivity().checkForRoadWin(player, board) || new RoadConnectivity().checkForRoadWin(player.getOpponent(), board)) {
            return evalFunction.evaluate(board, player);
        }
        
        Player currentPlayer = isMaximizing ? player : player.getOpponent();
        List<Action> possibleActions = getAllPossibleActions(board, currentPlayer);
        
        // Apply move ordering heuristics
        MoveOrderingHeuristics.orderMoves(possibleActions, board, player);
        
        if (isMaximizing) {
            double maxEval = Double.NEGATIVE_INFINITY;
            for (Action action : possibleActions) {
                Board clonedBoard = board.copy();
                try {
                    action.execute(clonedBoard);
                    double eval = minimax(clonedBoard, depth - 1, false, alpha, beta, player);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break; // Beta cut-off
                    }
                } catch (InvalidMoveException e) {
                    Logger.log("Minimax", "Invalid move during Minimax: " + e.getMessage());
                    continue;
                }
            }
            return maxEval;
        } else {
            double minEval = Double.POSITIVE_INFINITY;
            for (Action action : possibleActions) {
                Board clonedBoard = board.copy();
                try {
                    action.execute(clonedBoard);
                    double eval = minimax(clonedBoard, depth - 1, true, alpha, beta, player);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        break; // Alpha cut-off
                    }
                } catch (InvalidMoveException e) {
                    Logger.log("Minimax", "Invalid move during Minimax: " + e.getMessage());
                    continue;
                }
            }
            return minEval;
        }
    }
    
    /**
     * Retrieves all possible actions for a given player from the current board state.
     *
     * @param board  The current game board.
     * @param player The player for whom to retrieve actions.
     * @return A list of possible actions.
     */
    private List<Action> getAllPossibleActions(Board board, Player player) {
        List<Action> actions = new ArrayList<>();
        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece != null && topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                    // Possible placement actions
                    if (player.hasPiecesLeft(Piece.PieceType.FLAT_STONE)) {
                        actions.add(new Placement(x, y, Piece.PieceType.FLAT_STONE, player));
                    }
                    if (player.hasPiecesLeft(Piece.PieceType.CAPSTONE)) {
                        actions.add(new Placement(x, y, Piece.PieceType.CAPSTONE, player));
                    }
                    
                    // Possible move actions
                    for (Direction dir : Direction.values()) {
                        // Generate possible drop counts based on carry limit
                        int carryLimit = board.getCarryLimit();
                        List<List<Integer>> possibleDrops = generateDropCounts(carryLimit, 4); // Assuming max 4 steps
                        for (List<Integer> drop : possibleDrops) {
                            int totalDrops = drop.stream().mapToInt(Integer::intValue).sum();
                            if (totalDrops <= carryLimit) {
                                MoveAction move = new MoveAction(x, y, dir, totalDrops, drop, player);
                                actions.add(move);
                            }
                        }
                    }
                }
            }
        }
        return actions;
    }
    
    /**
     * Generates all possible drop counts for a given carry limit and number of steps.
     *
     * @param carryLimit The maximum number of pieces to carry.
     * @param steps      The number of steps to distribute the drops.
     * @return A list of possible drop counts.
     */
    private List<List<Integer>> generateDropCounts(int carryLimit, int steps) {
        List<List<Integer>> results = new ArrayList<>();
        generateDropCountsHelper(carryLimit, steps, new ArrayList<>(), results);
        return results;
    }
    
    /**
     * Helper method to recursively generate drop counts.
     */
    private void generateDropCountsHelper(int remaining, int steps, List<Integer> current, List<List<Integer>> results) {
        if (steps == 1) {
            current.add(remaining);
            results.add(new ArrayList<>(current));
            current.remove(current.size() - 1);
            return;
        }
        for (int i = 1; i <= remaining - (steps - 1); i++) {
            current.add(i);
            generateDropCountsHelper(remaining - i, steps - 1, current, results);
            current.remove(current.size() - 1);
        }
    }
}
