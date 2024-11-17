// File: core/src/main/java/com/Tak/AI/evaluation/EvaluationFunction.java
package com.Tak.AI.evaluation;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The EvaluationFunction class evaluates the game board from the AI's perspective.
 * It assigns scores based on various strategic factors, focusing on blocking the opponent
 * and progressing the AI's own road-building efforts.
 */
public class EvaluationFunction implements Serializable {
    private static final long serialVersionUID = 1L;

    private RoadConnectivity roadChecker;
    private transient Map<String, Double> evaluationCache;

    /**
     * Default constructor. Initializes necessary components.
     */
    public EvaluationFunction() {
        this.roadChecker = new RoadConnectivity();
        this.evaluationCache = new HashMap<>();
    }

    /**
     * Evaluates the current board state and returns a score.
     *
     * @param board  The current game board.
     * @param player The player for whom the evaluation is being done.
     * @return A numerical score representing the desirability of the board state.
     */
    public double evaluate(Board board, Player player) {
        String stateHash = generateStateHash(board, player);

        if (evaluationCache.containsKey(stateHash)) {
            return evaluationCache.get(stateHash);
        }

        // Check for road win conditions
        if (roadChecker.checkForRoadWin(player, board)) {
            Logger.log("EvaluationFunction", player.getColor() + " has completed a road and wins!");
            evaluationCache.put(stateHash, Double.POSITIVE_INFINITY);
            return Double.POSITIVE_INFINITY;
        } else if (roadChecker.checkForRoadWin(player.getOpponent(), board)) {
            evaluationCache.put(stateHash, Double.NEGATIVE_INFINITY);
            return Double.NEGATIVE_INFINITY;
        }

        // Evaluate using heuristic
        double heuristicScore = heuristicEvaluation(board, player);

        evaluationCache.put(stateHash, heuristicScore);

        return heuristicScore;
    }

    /**
     * Heuristic evaluation function focusing on blocking the opponent and progressing the AI's own road.
     *
     * @param board  The current game board.
     * @param player The player (AI) for whom the evaluation is being done.
     * @return A numerical score representing the heuristic evaluation.
     */
    private double heuristicEvaluation(Board board, Player player) {
        double score = 0.0;

        // Immediate Threat Detection (Very High Priority)
        boolean opponentThreat = roadChecker.isOpponentCloseToWinning(board, player.getOpponent());
        if (opponentThreat) {
            score -= 1000.0; // Large penalty to prioritize blocking
        }

        // Immediate Winning Opportunity
        boolean canWinNext = roadChecker.isPlayerCloseToWinning(board, player);
        if (canWinNext) {
            score += 1000.0; // Large reward to prioritize winning
        }

        // Road Potential Difference (High Priority)
        int playerRoadPotential = roadChecker.calculateRoadPotential(board, player);
        int opponentRoadPotential = roadChecker.calculateRoadPotential(board, player.getOpponent());
        score += (playerRoadPotential - opponentRoadPotential) * 100.0;

        // Pattern Recognition (Medium Priority)
        double patternScore = analyzePatterns(board, player);
        score += patternScore * 50.0;

        // Flat Stones Difference (Low Priority)
        int flatStoneDifference = countFlatStones(board, player) - countFlatStones(board, player.getOpponent());
        score += flatStoneDifference * 10.0;

        // Capstones Remaining (Low Priority)
        int capstoneDifference = player.getRemainingPieces(Piece.PieceType.CAPSTONE)
                - player.getOpponent().getRemainingPieces(Piece.PieceType.CAPSTONE);
        score += capstoneDifference * 20.0;

        return score;
    }

    /**
     * Analyzes the board for patterns that could lead to a win or threat.
     *
     * @param board  The current game board.
     * @param player The player to analyze patterns for.
     * @return A score representing the pattern analysis.
     */
    private double analyzePatterns(Board board, Player player) {
        double bestPatternScore = 0.0;
        int size = board.getSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Check horizontal and vertical lines through (x, y)
                double patternScore = evaluateLinePatterns(board, player, x, y);
                if (patternScore > bestPatternScore) {
                    bestPatternScore = patternScore;
                }
            }
        }

        return bestPatternScore;
    }

    /**
     * Evaluates line patterns (rows and columns) passing through a given position.
     *
     * @param board  The game board.
     * @param player The player.
     * @param x      The X coordinate.
     * @param y      The Y coordinate.
     * @return A score representing the line pattern.
     */
    private double evaluateLinePatterns(Board board, Player player, int x, int y) {
        int size = board.getSize();
        double score = 0.0;

        // Horizontal Line
        int playerCount = 0;
        int opponentCount = 0;
        for (int i = 0; i < size; i++) {
            PieceStack stack = board.getBoardStack(i, y);
            if (!stack.isEmpty()) {
                Piece topPiece = stack.getTopPiece();
                if (topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                    playerCount++;
                } else if (topPiece.getOwner() == player.getOpponent() && topPiece.canBePartOfRoad()) {
                    opponentCount++;
                }
            }
        }
        score += patternScore(playerCount, opponentCount, size);

        // Vertical Line
        playerCount = 0;
        opponentCount = 0;
        for (int i = 0; i < size; i++) {
            PieceStack stack = board.getBoardStack(x, i);
            if (!stack.isEmpty()) {
                Piece topPiece = stack.getTopPiece();
                if (topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                    playerCount++;
                } else if (topPiece.getOwner() == player.getOpponent() && topPiece.canBePartOfRoad()) {
                    opponentCount++;
                }
            }
        }
        score += patternScore(playerCount, opponentCount, size);

        return score;
    }

    /**
     * Calculates the pattern score based on counts of player's and opponent's pieces.
     *
     * @param playerCount   Number of player's pieces in the line.
     * @param opponentCount Number of opponent's pieces in the line.
     * @param size          The size of the board.
     * @return A score representing the potential of the line.
     */
    private double patternScore(int playerCount, int opponentCount, int size) {
        if (opponentCount == 0 && playerCount > 1) {
            // Favor lines where the AI has multiple pieces and the opponent has none
            return playerCount / (double) size;
        } else if (playerCount == 0 && opponentCount > 1) {
            // Penalize lines where the opponent has multiple pieces and the AI has none
            return -opponentCount / (double) size;
        }
        return 0.0;
    }

    /**
     * Generates a unique hash for the board state including player turn.
     *
     * @param board  The game board.
     * @param player The current player.
     * @return A unique string representing the board state.
     */
    private String generateStateHash(Board board, Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(player.getColor() == Player.Color.BLACK ? "B_" : "W_");

        for (int y = 0; y < board.getSize(); y++) {
            for (int x = 0; x < board.getSize(); x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (stack.isEmpty()) {
                    sb.append("E");
                } else {
                    Piece topPiece = stack.getTopPiece();
                    sb.append(topPiece.getPieceType().toString().charAt(0));
                    sb.append(topPiece.getOwner().getColor().toString().charAt(0));
                    sb.append(stack.size());
                }
                sb.append(",");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    /**
     * Counts the number of flat stones for a player.
     *
     * @param board  The current game board.
     * @param player The player whose flat stones are being counted.
     * @return The number of flat stones.
     */
    private int countFlatStones(Board board, Player player) {
        int count = 0;
        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner() == player && topPiece.getPieceType() == Piece.PieceType.FLAT_STONE) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    // The rest of the methods remain the same, utilizing roadChecker.

    /**
     * Determines if the player is close to winning.
     *
     * @param board  The game board.
     * @param player The player.
     * @return True if the player is close to winning, false otherwise.
     */
    private boolean isPlayerCloseToWinning(Board board, Player player) {
        // Implement logic to determine if the player is one move away from winning
        return roadChecker.isPlayerCloseToWinning(board, player);
    }
}
