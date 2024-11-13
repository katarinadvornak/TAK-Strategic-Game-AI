// File: core/src/main/java/com/Tak/AI/ActionGenerator.java
package com.Tak.AI;

import com.Tak.Logic.Action;
import com.Tak.Logic.Board;
import com.Tak.Logic.Direction;
import com.Tak.Logic.InvalidMoveException;
import com.Tak.Logic.Player;
import com.Tak.Logic.Piece;
import com.Tak.Logic.Placement;
import com.Tak.AI.MoveAction;

import java.util.ArrayList;
import java.util.List;

/**
 * The ActionGenerator class is responsible for generating all possible actions
 * (placements and moves) for a given player based on the current board state.
 */
public class ActionGenerator {

    /**
     * Generates all possible actions (placements and moves) for the given player.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate actions.
     * @param moveCount The current move count in the game.
     * @return A list of possible actions.
     */
    public static List<Action> generatePossibleActions(Board board, Player player, int moveCount) {
        List<Action> actions = new ArrayList<>();
        if (player == null) {
            System.err.println("ActionGenerator.generatePossibleActions: Player is null!");
            return actions;
        }
        List<Action> placements = generatePlacements(board, player, moveCount);
        System.out.println("Generated " + placements.size() + " placement actions.");
        actions.addAll(placements);
        
        List<Action> moves = generateMoves(board, player, moveCount);
        System.out.println("Generated " + moves.size() + " move actions.");
        actions.addAll(moves);
        
        System.out.println("Total actions generated for " + player.getColor() + ": " + actions.size());
        return actions;
    }

    /**
     * Generates all possible placement actions for the given player.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate placement actions.
     * @param moveCount The current move count in the game.
     * @return A list of placement actions.
     */
    private static List<Action> generatePlacements(Board board, Player player, int moveCount) {
        List<Action> placements = new ArrayList<>();
        int size = board.getSize();
    
        if (player == null) {
            System.err.println("ActionGenerator.generatePlacements: Player is null!");
            return placements;
        }
    
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // Only allow placement on empty squares
                if (board.getBoardPosition(x, y).isEmpty()) {
                    // Determine which piece types the player can place
                    List<Piece.PieceType> availablePieceTypes = new ArrayList<>();
    
                    // During the first two moves, only flat stones can be placed
                    if (moveCount < 2) {
                        if (player.hasPiecesLeft(Piece.PieceType.FLAT_STONE)) {
                            availablePieceTypes.add(Piece.PieceType.FLAT_STONE);
                        }
                    } else {
                        // After first two moves, all piece types can be placed if available
                        for (Piece.PieceType type : Piece.PieceType.values()) {
                            if (player.hasPiecesLeft(type)) {
                                availablePieceTypes.add(type);
                            }
                        }
                    }
    
                    // Create placement actions for each available piece type
                    for (Piece.PieceType type : availablePieceTypes) {
                        // Implement validation
                        if (isValidPlacement(board, x, y, type, player, moveCount)) {
                            Placement placement = new Placement(x, y, type, player);
                            placements.add(placement);
                            System.out.println("Generated Placement: " + type + " at (" + x + ", " + y + ") for " + player.getColor());
                        } else {
                            System.out.println("Invalid Placement Attempt: " + type + " at (" + x + ", " + y + ") for " + player.getColor());
                        }
                    }
                }
            }
        }
    
        System.out.println("Total Placements Generated: " + placements.size());
        return placements;
    }
    
    
    /**
     * Validates if placing a piece is legal without executing the action.
     *
     * @param board     The current game board.
     * @param x         The x-coordinate for placement.
     * @param y         The y-coordinate for placement.
     * @param type      The type of piece to place.
     * @param player    The player attempting the placement.
     * @param moveCount The current move count.
     * @return True if the placement is valid, false otherwise.
     */
    private static boolean isValidPlacement(Board board, int x, int y, Piece.PieceType type, Player player, int moveCount) {
        // Example rules:
        // - No stacking on non-empty squares (already checked)
        // - Limit the number of capstones a player can place
        if (type == Piece.PieceType.CAPSTONE && player.getRemainingPieces(Piece.PieceType.CAPSTONE) <= 0) {
            System.out.println("Invalid Placement: No capstones left for " + player.getColor());
            return false;
        }

        // Additional rules can be implemented here
        // For example, restrict certain placements based on game progression
        // TODO: Add more placement validation rules as per game mechanics

        return true;
    }

    /**
     * Generates all possible move actions for the given player and board state.
     *
     * @param board     The current game board.
     * @param player    The player for whom to generate move actions.
     * @param moveCount The current move count in the game.
     * @return A list of move actions.
     */
    private static List<Action> generateMoves(Board board, Player player, int moveCount) {
        List<Action> movements = new ArrayList<>();
        int size = board.getSize();

        if (player == null) {
            System.err.println("ActionGenerator.generateMoves: Player is null!");
            return movements;
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                List<Piece> stack = board.getBoardPosition(x, y);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.get(stack.size() - 1);
                    if (topPiece.getOwner() == player) {
                        int maxCarry = Math.min(board.getCarryLimit(), stack.size());

                        // Generate all possible number of pieces to move (1 to maxCarry)
                        for (int numPieces = 1; numPieces <= maxCarry; numPieces++) {
                            // Generate all possible directions
                            for (Direction dir : Direction.values()) {
                                // Generate all possible drop counts for this move
                                List<int[]> dropCountsList = generateDropCounts(numPieces, 1, numPieces);
                                for (int[] dropCounts : dropCountsList) {
                                    // Convert dropCounts array to List<Integer>
                                    List<Integer> dropCountsListConverted = new ArrayList<>();
                                    for (int count : dropCounts) {
                                        dropCountsListConverted.add(count);
                                    }

                                    MoveAction move = new MoveAction(x, y, dir, numPieces, dropCountsListConverted, player);
                                    // Validate the movement action before adding
                                    try {
                                        move.execute(board.copy()); // Use a deep copy to test validity
                                        movements.add(move);
                                        System.out.println("Generated MoveAction: " + move.toString() + " for " + player.getColor());
                                    } catch (InvalidMoveException e) {
                                        // Invalid move, skip adding this action
                                        System.out.println("Invalid MoveAction: " + move.toString() + " | Reason: " + e.getMessage());
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Total MoveActions Generated: " + movements.size());
        return movements;
    }

    /**
     * Recursively generates all possible drop counts sequences that sum up to totalPieces,
     * ensuring that no step violates stack height constraints.
     *
     * @param totalPieces The total number of pieces to drop.
     * @param min         The minimum number of pieces to drop at each step.
     * @param max         The maximum number of pieces to drop at each step.
     * @return A list of drop counts arrays.
     */
    private static List<int[]> generateDropCounts(int totalPieces, int min, int max) {
        List<int[]> result = new ArrayList<>();
        generateDropCountsHelper(totalPieces, min, max, new ArrayList<>(), result);
        return result;
    }

    /**
     * Helper method to generate drop counts recursively.
     */
    private static void generateDropCountsHelper(int remaining, int min, int max, List<Integer> current, List<int[]> result) {
        if (remaining == 0) {
            // Convert List<Integer> to int[]
            int[] dropCounts = current.stream().mapToInt(i -> i).toArray();
            result.add(dropCounts);
            return;
        }

        for (int i = min; i <= Math.min(max, remaining); i++) {
            current.add(i);
            generateDropCountsHelper(remaining - i, min, max, current, result);
            current.remove(current.size() - 1);
        }
    }
}
