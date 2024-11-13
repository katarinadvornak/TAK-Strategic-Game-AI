// File: core/src/main/java/com/Tak/AI/EvaluationFunction.java
package com.Tak.AI;

import java.util.ArrayList;
import java.util.List;

import com.Tak.Logic.*;
import com.Tak.Logic.Piece.PieceType;

/**
 * The EvaluationFunction class evaluates the game board from the AI's perspective.
 * It assigns scores based on various strategic factors.
 */
public class EvaluationFunction {
    
    private RoadConnectivity roadChecker;
    
    public EvaluationFunction() {
        this.roadChecker = new RoadConnectivity();
    }

    /**
     * Evaluates the current board state and returns a score.
     *
     * @param board  The current game board.
     * @param player The Player instance (can be AIPlayer or HumanPlayer).
     * @return The evaluated score of the board.
     */
    public double evaluate(Board board, Player player) {
        double score = 0.0;
        
        // Road Potential
        score += evaluateRoadPotential(board, player) * 10;
        
        // Blocking Potential
        score += evaluateBlockingPotential(board, player) * 15;
        
        // Mobility
        score += evaluatePieceMobility(board, player) * 5;
        
        // Central Control
        score += evaluateCentralControl(board, player) * 2;
        
        return score;
    }
    
    /**
     * Evaluates the road potential for the player.
     *
     * @param board  The current game board.
     * @param player The Player instance.
     * @return The road potential score.
     */
    public double evaluateRoadPotential(Board board, Player player) {
        return roadChecker.evaluateRoadPotential(board, player);
    }
    
    /**
     * Evaluates the blocking potential against the opponent.
     *
     * @param board  The current game board.
     * @param player The Player instance.
     * @return The blocking potential score.
     */
    public double evaluateBlockingPotential(Board board, Player player) {
        Player opponent = player.getOpponent();
        double opponentRoadPotential = roadChecker.evaluateRoadPotential(board, opponent);
        return opponentRoadPotential > 0 ? -100.0 : 0.0;
    }
    
    /**
     * Evaluates the mobility of the player.
     *
     * @param board  The current game board.
     * @param player The Player instance.
     * @return The mobility score.
     */
    public double evaluatePieceMobility(Board board, Player player) {
        int mobility = countValidMoves(board, player);
        return mobility * 2.0; // Each valid move contributes positively
    }
    
    /**
     * Evaluates the central control of the player.
     *
     * @param board  The current game board.
     * @param player The Player instance.
     * @return The central control score.
     */
    public double evaluateCentralControl(Board board, Player player) {
        double centralScore = 0.0;
        int size = board.getSize();
        double center = (size - 1) / 2.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece != null && topPiece.getOwner() == player) {
                    double distance = Math.sqrt(Math.pow(x - center, 2) + Math.pow(y - center, 2));
                    centralScore += (size - distance); // Closer to center yields higher score
                }
            }
        }
        return centralScore;
    }
    
    /**
     * Counts the number of valid moves available to the player.
     *
     * @param board  The current game board.
     * @param player The Player instance.
     * @return The number of valid moves.
     */
    private int countValidMoves(Board board, Player player) {
        int count = 0;
        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece != null && topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                    // Possible placement actions
                    if (player.hasPiecesLeft(Piece.PieceType.FLAT_STONE) || player.hasPiecesLeft(Piece.PieceType.CAPSTONE)) {
                        count++;
                    }
                    
                    // Possible move actions
                    for (Direction dir : Direction.values()) {
                        // Generate possible drop counts based on carry limit
                        int carryLimit = board.getCarryLimit();
                        List<List<Integer>> possibleDrops = generateDropCounts(carryLimit, 4); // Assuming max 4 steps
                        for (List<Integer> drop : possibleDrops) {
                            int totalDrops = drop.stream().mapToInt(Integer::intValue).sum();
                            if (totalDrops <= carryLimit) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count;
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

    /**
     * Creates a copy of this EvaluationFunction.
     *
     * @return A new EvaluationFunction instance.
     */
    public EvaluationFunction copy() {
        return new EvaluationFunction();
    }
}
