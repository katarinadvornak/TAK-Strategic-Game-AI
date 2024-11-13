package com.Tak.AI.evaluation;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.AI.learning.QLearningAgent;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
        int playerFlats = countFlatStones(board, player);
        int opponentFlats = countFlatStones(board, player.getOpponent());
        score += (playerFlats - opponentFlats) * 1.0;

        int playerRoadPotential = calculateRoadPotential(board, player);
        int opponentRoadPotential = calculateRoadPotential(board, player.getOpponent());
        score += (playerRoadPotential - opponentRoadPotential) * 4.0;

        int playerStackHeight = calculateStackHeight(board, player);
        int opponentStackHeight = calculateStackHeight(board, player.getOpponent());
        score += (playerStackHeight - opponentStackHeight) * 0.5;

        int playerCapstones = player.getRemainingPieces(Piece.PieceType.CAPSTONE);
        int opponentCapstones = player.getOpponent().getRemainingPieces(Piece.PieceType.CAPSTONE);
        score += (opponentCapstones - playerCapstones) * 3.0;

        int playerConnectedness = calculateConnectedness(board, player);
        int opponentConnectedness = calculateConnectedness(board, player.getOpponent());
        score += (playerConnectedness - opponentConnectedness) * 2.0;

        int playerBlocking = calculateBlocking(board, player);
        int opponentBlocking = calculateBlocking(board, player.getOpponent());
        score += (playerBlocking - opponentBlocking) * 3.0;

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
}
