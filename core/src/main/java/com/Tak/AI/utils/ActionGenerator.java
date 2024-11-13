package com.Tak.AI.utils;

import com.Tak.AI.actions.Action;
import com.Tak.AI.actions.Move;
import com.Tak.AI.actions.Placement;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for generating all possible valid actions (placements and moves) for a given player based on the current board state.
 */
public class ActionGenerator {

    /**
     * Generates all possible actions (placements and moves) for a given player based on the current board state.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate actions.
     * @param moveCount The current move count in the game.
     * @return A list of valid action strings.
     */
    public static List<String> generatePossibleActions(Board board, Player player, int moveCount) {
        List<String> actions = new ArrayList<>();
        List<String> placements = generatePlacements(board, player, moveCount);
        List<String> moveActions = generateMoveActions(board, player, moveCount);
        actions.addAll(placements);
        actions.addAll(moveActions);
        return actions;
    }

    /**
     * Generates limited actions (placements only) for the opponent.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate limited actions.
     * @param moveCount The current move count in the game.
     * @return A list of valid action strings limited to placements.
     */
    public static List<String> generateLimitedActions(Board board, Player player, int moveCount) {
        return generatePlacements(board, player, moveCount);
    }

    /**
     * Generates all possible placement actions for a given player.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate placements.
     * @param moveCount The current move count in the game.
     * @return A list of valid placement action strings.
     */
    private static List<String> generatePlacements(Board board, Player player, int moveCount) {
        List<String> placements = new ArrayList<>();
        int size = board.getSize();

        boolean isInitialMove = moveCount < 2;
        Piece.PieceType pieceType = Piece.PieceType.FLAT_STONE;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (board.canPlacePiece(x, y)) {
                    if (isInitialMove) {
                        if (player.getOpponent().hasPieces(pieceType)) {
                            String action = "PLACE_FLAT_STONE " + x + " " + y;
                            placements.add(action);
                        }
                    } else {
                        if (player.hasPieces(pieceType)) {
                            String action = "PLACE_FLAT_STONE " + x + " " + y;
                            placements.add(action);
                        }
                        if (player.hasPieces(Piece.PieceType.CAPSTONE)) {
                            String capstoneAction = "PLACE_CAPSTONE " + x + " " + y;
                            placements.add(capstoneAction);
                        }
                    }
                }
            }
        }
        return placements;
    }

    /**
     * Generates all possible move actions for a given player based on the current board state.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate move actions.
     * @param moveCount The current move count in the game.
     * @return A list of valid move action strings.
     */
    private static List<String> generateMoveActions(Board board, Player player, int moveCount) {
        List<String> moveActions = new ArrayList<>();
        int size = board.getSize();
        int carryLimit = board.getCarryLimit();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece != null && topPiece.getOwner().equals(player)) {
                    PieceStack stack = board.getBoardStack(x, y);
                    int stackSize = stack.size();

                    for (int numPieces = 1; numPieces <= Math.min(carryLimit, stackSize); numPieces++) {
                        for (Direction direction : Direction.values()) {
                            List<List<Integer>> dropSequences = generateDropSequences(numPieces);
                            for (List<Integer> drops : dropSequences) {
                                if (isValidMove(board, x, y, direction, drops)) {
                                    StringBuilder actionBuilder = new StringBuilder();
                                    actionBuilder.append("MOVE ")
                                             .append(x).append(" ")
                                             .append(y).append(" ")
                                             .append(direction.name()).append(" ")
                                             .append(numPieces);
                                    for (int drop : drops) {
                                        actionBuilder.append(" ").append(drop);
                                    }
                                    moveActions.add(actionBuilder.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        return moveActions;
    }

    /**
     * Validates whether a move action is within the board boundaries.
     *
     * @param board     The game board.
     * @param fromX     The starting X coordinate.
     * @param fromY     The starting Y coordinate.
     * @param direction The direction of movement.
     * @param drops     The drop counts along the path.
     * @return True if the move is valid, false otherwise.
     */
    private static boolean isValidMove(Board board, int fromX, int fromY, Direction direction, List<Integer> drops) {
        int x = fromX;
        int y = fromY;
        for (int drop : drops) {
            x += direction.getDeltaX();
            y += direction.getDeltaY();

            if (!board.isWithinBounds(x, y)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates all possible drop sequences for a move action.
     *
     * @param totalDrops The total number of pieces to drop.
     * @return A list of all valid drop sequences.
     */
    private static List<List<Integer>> generateDropSequences(int totalDrops) {
        List<List<Integer>> sequences = new ArrayList<>();
        generateDropSequencesHelper(totalDrops, new ArrayList<>(), sequences);
        return sequences;
    }

    /**
     * Helper method to recursively generate all possible drop sequences.
     *
     * @param remaining The remaining number of pieces to drop.
     * @param current   The current drop sequence being built.
     * @param sequences The list of all valid drop sequences.
     */
    private static void generateDropSequencesHelper(int remaining, List<Integer> current, List<List<Integer>> sequences) {
        if (remaining == 0) {
            sequences.add(new ArrayList<>(current));
            return;
        }
        for (int i = 1; i <= remaining; i++) {
            current.add(i);
            generateDropSequencesHelper(remaining - i, current, sequences);
            current.remove(current.size() - 1);
        }
    }
}
