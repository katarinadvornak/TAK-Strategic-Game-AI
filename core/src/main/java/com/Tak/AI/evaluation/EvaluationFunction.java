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
 * It assigns scores based on various strategic factors, adjusting its strategy
 * dynamically based on whether the AI is ahead or behind.
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
     * Heuristic evaluation function that adjusts weights dynamically based on whether
     * the AI is ahead or behind.
     *
     * @param board  The current game board.
     * @param player The player (AI) for whom the evaluation is being done.
     * @return A numerical score representing the heuristic evaluation.
     */
    private double heuristicEvaluation(Board board, Player player) {
        double score = 0.0;

        // Immediate Threat Detection
        boolean opponentThreat = roadChecker.isPlayerCloseToWinning(board, player.getOpponent());
        if (opponentThreat) {
            score -= 1000.0; // Large penalty to prioritize blocking
        }

        // Immediate Winning Opportunity
        boolean canWinNext = roadChecker.isPlayerCloseToWinning(board, player);
        if (canWinNext) {
            score += 1000.0; // Large reward to prioritize winning
        }

        // Base evaluation before dynamic adjustments
        double baseScore = score;

        // Road Potential Difference
        int playerRoadPotential = roadChecker.calculateRoadPotential(board, player);
        int opponentRoadPotential = roadChecker.calculateRoadPotential(board, player.getOpponent());
        double roadPotentialScore = (playerRoadPotential - opponentRoadPotential) * 100.0;

        // Stacking Mechanics
        double stackingScore = evaluateStacking(board, player) * 50.0;

        // Blocking Potential
        double blockingScore = evaluateBlockingPotential(board, player) * 50.0;

        // Capstone Utilization
        double capstoneScore = evaluateCapstoneUtilization(board, player) * 30.0;

        // Flat Stones Difference
        int flatStoneDifference = countFlatStones(board, player) - countFlatStones(board, player.getOpponent());
        double flatStoneScore = flatStoneDifference * 10.0;

        // Total current evaluation
        double currentEvaluation = baseScore + roadPotentialScore + stackingScore + blockingScore + capstoneScore + flatStoneScore;

        // Dynamic weight adjustments based on current evaluation
        if (currentEvaluation < 0) {
            // AI is behind, adopt defensive strategy
            blockingScore *= 1.5; // Increase blocking priority
            roadPotentialScore *= 0.8; // Decrease own road building priority
            stackingScore *= 1.0; // Keep stacking priority
        } else {
            // AI is ahead, adopt offensive strategy
            roadPotentialScore *= 1.2; // Increase own road building priority
            blockingScore *= 0.8; // Decrease blocking priority
            stackingScore *= 1.0; // Keep stacking priority
        }

        // Recalculate total score with adjusted weights
        score = baseScore + roadPotentialScore + stackingScore + blockingScore + capstoneScore + flatStoneScore;

        return score;
    }

    /**
     * Evaluates stacking mechanics to assess control over stacks and mobility.
     *
     * @param board  The game board.
     * @param player The player.
     * @return A score representing stacking benefits.
     */
    private double evaluateStacking(Board board, Player player) {
        double stackingScore = 0.0;
        int size = board.getSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    // Determine who controls the stack (owner of the top piece)
                    Player stackOwner = stack.getTopPiece().getOwner();
                    if (stackOwner.equals(player)) {
                        stackingScore += stack.size() * 2; // Reward for larger stacks
                        // Additional bonus if the stack can move towards completing a road
                        if (canMoveStackTowardsWin(board, x, y, player)) {
                            stackingScore += 5.0;
                        }
                    } else if (stackOwner.equals(player.getOpponent())) {
                        stackingScore -= stack.size(); // Penalty if opponent controls larger stacks
                    }
                }
            }
        }
        return stackingScore;
    }

    /**
     * Checks if the stack at (x, y) can be moved towards completing a road.
     *
     * @param board  The game board.
     * @param x      The X coordinate.
     * @param y      The Y coordinate.
     * @param player The player.
     * @return True if the stack can be moved towards a road completion, false otherwise.
     */
    private boolean canMoveStackTowardsWin(Board board, int x, int y, Player player) {
        // Determine the target direction based on player's color
        Direction targetDirection = getPlayerRoadDirection(player);
        if (targetDirection == null) {
            return false; // Undefined direction
        }

        // Check if the stack is not already at the edge in the target direction
        if (isAtEdge(board, x, y, targetDirection)) {
            return false;
        }

        // Check if moving in the target direction is possible
        int newX = x + targetDirection.getDeltaX();
        int newY = y + targetDirection.getDeltaY();
        return board.isWithinBounds(newX, newY);
    }

    /**
     * Determines the road direction for a player based on their color.
     *
     * @param player The player.
     * @return The target direction for road-building.
     */
    private Direction getPlayerRoadDirection(Player player) {
        // Assuming BLUE aims left-right and GREEN aims top-bottom
        if (player.getColor() == Player.Color.BLUE) {
            return Direction.RIGHT; // Left to Right
        } else if (player.getColor() == Player.Color.GREEN) {
            return Direction.DOWN; // Top to Bottom
        }
        return null; // Undefined
    }

    /**
     * Checks if the position is at the edge in the specified direction.
     *
     * @param board     The game board.
     * @param x         The X coordinate.
     * @param y         The Y coordinate.
     * @param direction The direction to check.
     * @return True if at the edge, false otherwise.
     */
    private boolean isAtEdge(Board board, int x, int y, Direction direction) {
        int size = board.getSize();
        switch (direction) {
            case RIGHT:
                return x == size - 1;
            case LEFT:
                return x == 0;
            case DOWN:
                return y == size - 1;
            case UP:
                return y == 0;
            default:
                return false;
        }
    }

    /**
     * Evaluates the potential to block the opponent's road-building efforts.
     *
     * @param board  The game board.
     * @param player The player.
     * @return A score representing blocking potential.
     */
    private double evaluateBlockingPotential(Board board, Player player) {
        double blockingScore = 0.0;
        int size = board.getSize();
        Player opponent = player.getOpponent();

        // Iterate over all positions on the board
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // Check if this position is adjacent to the opponent's road
                if (isAdjacentToOpponentRoad(board, x, y, opponent)) {
                    PieceStack stack = board.getBoardStack(x, y);
                    if (stack.isEmpty()) {
                        blockingScore += 1.0; // Potential to block
                    } else {
                        Piece topPiece = stack.getTopPiece();
                        if (topPiece.getOwner().equals(player)) {
                            blockingScore += 1.5; // Already blocking
                        }
                    }
                }
            }
        }
        return blockingScore;
    }

    /**
     * Checks if the position is adjacent to the opponent's road pieces.
     *
     * @param board    The game board.
     * @param x        The X coordinate.
     * @param y        The Y coordinate.
     * @param opponent The opponent player.
     * @return True if adjacent to opponent's road, false otherwise.
     */
    private boolean isAdjacentToOpponentRoad(Board board, int x, int y, Player opponent) {
        for (Direction dir : Direction.values()) {
            int adjX = x + dir.getDeltaX();
            int adjY = y + dir.getDeltaY();
            if (board.isWithinBounds(adjX, adjY)) {
                PieceStack adjStack = board.getBoardStack(adjX, adjY);
                if (!adjStack.isEmpty()) {
                    Piece topPiece = adjStack.getTopPiece();
                    if (topPiece.getOwner().equals(opponent) && topPiece.canBePartOfRoad()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Evaluates the utilization of capstones on the board.
     *
     * @param board  The game board.
     * @param player The player.
     * @return A score representing capstone utilization.
     */
    private double evaluateCapstoneUtilization(Board board, Player player) {
        double capstoneScore = 0.0;
        int size = board.getSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getPieceType() == Piece.PieceType.CAPSTONE && topPiece.getOwner().equals(player)) {
                        capstoneScore += evaluateCapstonePosition(board, x, y, player);
                    }
                }
            }
        }
        return capstoneScore;
    }

    /**
     * Evaluates the impact of a capstone at a given position.
     *
     * @param board  The game board.
     * @param x      The X coordinate.
     * @param y      The Y coordinate.
     * @param player The player.
     * @return A score representing the capstone's impact.
     */
    private double evaluateCapstonePosition(Board board, int x, int y, Player player) {
        double impactScore = 0.0;
        Player opponent = player.getOpponent();

        for (Direction dir : Direction.values()) {
            int adjX = x + dir.getDeltaX();
            int adjY = y + dir.getDeltaY();
            if (board.isWithinBounds(adjX, adjY)) {
                PieceStack adjStack = board.getBoardStack(adjX, adjY);
                if (!adjStack.isEmpty()) {
                    Piece topPiece = adjStack.getTopPiece();
                    if (topPiece.getOwner().equals(opponent) && topPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
                        impactScore += 2.0; // Capstone can flatten opponent's wall
                    }
                }
            }
        }
        return impactScore;
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
                    if (topPiece.getOwner().equals(player) && topPiece.getPieceType() == Piece.PieceType.FLAT_STONE) {
                        count++;
                    }
                }
            }
        }
        return count;
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
        sb.append(player.getColor() == Player.Color.BLUE ? "B_" : "W_");

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
}
