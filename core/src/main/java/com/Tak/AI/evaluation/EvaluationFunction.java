package com.Tak.AI.evaluation;

import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.AI.actions.Action;
import com.Tak.AI.learning.QLearningAgent;
import com.Tak.AI.utils.ActionGenerator;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The EvaluationFunction class evaluates the game board from the AI's perspective.
 * It assigns scores based on various strategic factors.
 */
public class EvaluationFunction implements Serializable {
    private static final long serialVersionUID = 1L;

    private RoadConnectivity roadChecker;
    private QLearningAgent qLearningAgent;
    private transient Map<String, Double> evaluationCache;

    /**
     * Default constructor for cases where QLearningAgent is not used.
     */
    public EvaluationFunction() {
        this.roadChecker = new RoadConnectivity();
        this.qLearningAgent = null;
        this.evaluationCache = new HashMap<>();
    }

    /**
     * Constructor when QLearningAgent is used.
     *
     * @param qLearningAgent The QLearningAgent used to assist in board evaluation.
     */
    public EvaluationFunction(QLearningAgent qLearningAgent) {
        this.roadChecker = new RoadConnectivity();
        this.qLearningAgent = qLearningAgent;
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
        String stateHash = generateStateHashSymmetrically(board, player);

        if (evaluationCache.containsKey(stateHash)) {
            
            return evaluationCache.get(stateHash);
        }
        if (roadChecker.checkForRoadWin(player, board)) {
            Logger.log("EvaluationFunction", player.getColor() + " has completed a road and wins!");
            evaluationCache.put(stateHash, Double.POSITIVE_INFINITY);
            return Double.POSITIVE_INFINITY;
        } else if (roadChecker.checkForRoadWin(player.getOpponent(), board)) {
            evaluationCache.put(stateHash, Double.NEGATIVE_INFINITY);
            return Double.NEGATIVE_INFINITY;
        }

        double qValue = 0.0;
        if (qLearningAgent != null) {
            qValue = qLearningAgent.getStateValue(stateHash);
            if (Double.isNaN(qValue)) {
                qValue = 0.0;
            }
        }

        double heuristicScore = heuristicEvaluation(board, player);
        double totalScore = heuristicScore + qValue;
        totalScore = clamp(totalScore, -1000.0, 1000.0);
        evaluationCache.put(stateHash, totalScore);

        return totalScore;
    }

    /**
     * Clamps a value between a minimum and maximum bound.
     *
     * @param value The value to clamp.
     * @param min   The minimum bound.
     * @param max   The maximum bound.
     * @return The clamped value.
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Enhanced heuristic evaluation function considering multiple strategic factors.
     *
     * @param board  The current game board.
     * @param player The player for whom the evaluation is being done.
     * @return A numerical score representing the heuristic evaluation.
     */
    private double heuristicEvaluation(Board board, Player player) {
        double score = 0.0;
        int size = board.getSize();

        // 1. Flat Stones Count (Increased Weighting)
        int playerFlats = countFlatStones(board, player);
        int opponentFlats = countFlatStones(board, player.getOpponent());
        score += (playerFlats - opponentFlats) * 2.0; // Increased from 1.0 to 2.0

        // 2. Road Potential
        int playerRoadPotential = calculateRoadPotential(board, player);
        int opponentRoadPotential = calculateRoadPotential(board, player.getOpponent());
        score += (playerRoadPotential - opponentRoadPotential) * 4.0;

        // 3. Stack Height (Increased Weighting)
        int playerStackHeight = calculateStackHeight(board, player);
        int opponentStackHeight = calculateStackHeight(board, player.getOpponent());
        score += (playerStackHeight - opponentStackHeight) * 1.0; // Increased from 0.5 to 1.0

        // 4. Capstones Remaining
        int playerCapstones = player.getRemainingPieces(Piece.PieceType.CAPSTONE);
        int opponentCapstones = player.getOpponent().getRemainingPieces(Piece.PieceType.CAPSTONE);
        score += (opponentCapstones - playerCapstones) * 3.0;

        // 5. Connectedness
        int playerConnectedness = calculateConnectedness(board, player);
        int opponentConnectedness = calculateConnectedness(board, player.getOpponent());
        score += (playerConnectedness - opponentConnectedness) * 2.0;

        // 6. Blocking Potential
        int playerBlocking = calculateBlocking(board, player);
        int opponentBlocking = calculateBlocking(board, player.getOpponent());
        score += (playerBlocking - opponentBlocking) * 3.0;

        // 7. Control of Key Areas
        double controlScore = calculateControl(board, player);
        score += controlScore; // Weight already included in the method

        // 8. Mobility and Flexibility
        double mobilityScore = calculateMobility(board, player);
        score += mobilityScore; // Weight already included in the method

        // 9. Threat Detection
        double threatScore = calculateThreat(board, player);
        score += threatScore; // Weight already included in the method

        // Clamp the score to prevent extreme values
        score = clamp(score, -500.0, 500.0);
        return score;
    }

    /**
     * Determines if the move leads to a win for the player.
     *
     * @param score  The current score after evaluation.
     * @param player The player to check.
     * @return True if it's a winning move, false otherwise.
     */
    public boolean isWinningMove(double score, Player player) {
        return score >= 1000.0;
    }

    /**
     * Determines if the move blocks the opponent's potential win.
     *
     * @param score   The current score after evaluation.
     * @param player  The player making the move.
     * @param board   The game board after the move.
     * @return True if it's a blocking move, false otherwise.
     */
    public boolean isBlockingMove(double score, Player player, Board board) {
        return score >= 500.0 && score < 1000.0;
    }

    /**
     * Generates a symmetric state hash to reduce redundancy due to board symmetries.
     *
     * @param board  The game board.
     * @param player The current player.
     * @return A unique string representing the board state.
     */
    public String generateStateHashSymmetrically(Board board, Player player) {
        // Implement symmetry handling if needed. For now, return the standard hash.
        return generateStateHash(board, player);
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

    /**
     * Calculates the road completion potential for a player.
     *
     * @param board  The game board.
     * @param player The player.
     * @return An integer representing road completion potential.
     */
    public int calculateRoadPotential(Board board, Player player) {
        int maxSequence = 0;
        int size = board.getSize();

        // Check horizontal sequences
        for (int y = 0; y < size; y++) {
            int currentSequence = 0;
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                        currentSequence++;
                        maxSequence = Math.max(maxSequence, currentSequence);
                    } else {
                        currentSequence = 0;
                    }
                } else {
                    currentSequence = 0;
                }
            }
        }

        // Check vertical sequences
        for (int x = 0; x < size; x++) {
            int currentSequence = 0;
            for (int y = 0; y < size; y++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                        currentSequence++;
                        maxSequence = Math.max(maxSequence, currentSequence);
                    } else {
                        currentSequence = 0;
                    }
                } else {
                    currentSequence = 0;
                }
            }
        }

        return maxSequence;
    }

    /**
     * Calculates the total stack height for a player.
     * Favoring higher stacks can provide strategic advantages.
     *
     * @param board  The game board.
     * @param player The player.
     * @return The total stack height.
     */
    private int calculateStackHeight(Board board, Player player) {
        int totalHeight = 0;
        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner() == player) {
                        totalHeight += stack.size();
                    }
                }
            }
        }
        return totalHeight;
    }

    /**
     * Calculates the connectedness of the player's pieces towards road completion.
     * Connectedness is measured by the number of connected pieces in a potential road path.
     *
     * @param board  The game board.
     * @param player The player.
     * @return An integer representing connectedness.
     */
    private int calculateConnectedness(Board board, Player player) {
        int connectedPieces = 0;
        int size = board.getSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                        for (Direction dir : Direction.values()) {
                            int adjX = x + dir.getDeltaX();
                            int adjY = y + dir.getDeltaY();
                            if (board.isWithinBounds(adjX, adjY)) {
                                PieceStack adjStack = board.getBoardStack(adjX, adjY);
                                if (!adjStack.isEmpty()) {
                                    Piece adjTopPiece = adjStack.getTopPiece();
                                    if (adjTopPiece.getOwner().equals(player) && adjTopPiece.canBePartOfRoad()) {
                                        connectedPieces++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return connectedPieces;
    }

    /**
     * Calculates the blocking potential of the player's pieces against the opponent's road.
     * Blocking potential is measured by the number of opponent's road paths interrupted.
     *
     * @param board  The game board.
     * @param player The player performing the blocking.
     * @return An integer representing blocking potential.
     */
    public int calculateBlocking(Board board, Player player) {
        int blocks = 0;
        Player opponent = player.getOpponent();
        RoadConnectivity roadChecker = new RoadConnectivity();

        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner().equals(opponent) && topPiece.canBePartOfRoad()) {
                        for (Direction dir : Direction.values()) {
                            int adjX = x + dir.getDeltaX();
                            int adjY = y + dir.getDeltaY();
                            if (board.isWithinBounds(adjX, adjY)) {
                                PieceStack adjStack = board.getBoardStack(adjX, adjY);
                                if (!adjStack.isEmpty()) {
                                    Piece adjTopPiece = adjStack.getTopPiece();
                                    if (adjTopPiece.getOwner().equals(player) && roadChecker.canBlockRoad(adjTopPiece)) {
                                        blocks++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * Calculates the control score based on the player's occupation of key areas.
     *
     * @param board  The current game board.
     * @param player The player for whom control is being evaluated.
     * @return The control score.
     */
    public double calculateControl(Board board, Player player) {
        double controlScore = 0.0;
        int size = board.getSize();
        int center = size / 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner().equals(player)) {
                        // Assign higher weight to center and edges
                        double distance = Math.sqrt(Math.pow(x - center, 2) + Math.pow(y - center, 2));
                        double positionWeight = (size - distance) / size; // Normalize distance
                        if (x == center && y == center) {
                            positionWeight *= 2.0; // Double weight for exact center
                        }
                        controlScore += positionWeight;
                    }
                }
            }
        }
        return controlScore * 2.0; // Weighting factor
    }

    /**
     * Calculates the mobility score based on the number of valid actions available.
     *
     * @param board  The current game board.
     * @param player The player for whom mobility is being evaluated.
     * @return The mobility score.
     */
    public double calculateMobility(Board board, Player player) {
        List<Action> validActions = getAllPossibleActions(board, player);
        int placementCount = 0;
        int moveCount = 0;

        for (Action action : validActions) {
            if (action instanceof com.Tak.AI.actions.Placement) {
                placementCount++;
            } else if (action instanceof com.Tak.AI.actions.Move) {
                moveCount++;
            }
        }

        // Assign different weights to placements and moves
        double mobilityScore = (placementCount * 0.2) + (moveCount * 0.1);
        return mobilityScore; // Adjusted weighting factors if necessary
    }

    /**
     * Calculates the threat score based on the opponent's road proximity.
     *
     * @param board  The current game board.
     * @param player The player for whom the threat is being evaluated.
     * @return The threat score.
     */
    public double calculateThreat(Board board, Player player) {
        Player opponent = player.getOpponent();
        int opponentRoadPotential = calculateRoadPotential(board, opponent);

        // Additional logic to detect imminent threats (e.g., opponent within 2 moves)
        boolean imminentThreat = isImminentThreat(board, opponent);

        if (imminentThreat) {
            return -5.0; // Higher penalty for imminent threats
        }

        return opponentRoadPotential * -3.0; // Existing penalty
    }

    /**
     * Determines if the opponent is within an imminent threat of completing a road.
     *
     * @param board    The current game board.
     * @param opponent The opponent player.
     * @return True if an imminent threat exists, false otherwise.
     */
    private boolean isImminentThreat(Board board, Player opponent) {
        // Simplistic check: opponent's road potential is at maximum - adjust as needed
        return calculateRoadPotential(board, opponent) >= board.getSize() - 1;
    }

    /**
     * Retrieves all possible actions for the player on the current board.
     *
     * @param board  The current game board.
     * @param player The player for whom to generate actions.
     * @return A list of valid Action objects.
     */
    private List<Action> getAllPossibleActions(Board board, Player player) {
        // Utilize the ActionGenerator to retrieve possible actions
        return ActionGenerator.generatePossibleActions(board, player, board.getMoveCount())
                              .stream()
                              .map(actionStr -> {
                                  try {
                                      return Action.fromString(actionStr, player.getColor());
                                  } catch (InvalidMoveException e) {
                                      Logger.log("EvaluationFunction", "Invalid Action String: " + actionStr + " | Exception: " + e.getMessage());
                                      return null;
                                  }
                              })
                              .filter(Objects::nonNull)
                              .collect(Collectors.toList());
    }

    /**
     * Determines if the move leads to a win for the player.
     *
     * @param score  The current score after evaluation.
     * @param player The player to check.
     * @return True if it's a winning move, false otherwise.
     */
    public boolean isWinningMoveEnhanced(double score, Player player) {
        return score >= 1000.0;
    }

    /**
     * Determines if the move blocks the opponent's potential win.
     *
     * @param score   The current score after evaluation.
     * @param player  The player making the move.
     * @param board   The game board after the move.
     * @return True if it's a blocking move, false otherwise.
     */
    public boolean isBlockingMoveEnhanced(double score, Player player, Board board) {
        return score >= 500.0 && score < 1000.0;
    }
}
