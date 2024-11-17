package com.Tak.Logic.validators;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Move;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import com.Tak.Logic.models.Piece;

/**
 * Validates moves according to game rules.
 */
public class MoveValidator {
    private Board board;
    private Player currentPlayer;

    public MoveValidator(Board board, Player currentPlayer) {
        this.board = board;
        this.currentPlayer = currentPlayer;
    }
    
    /**
     * Validates a move.
     *
     * @param fromX Starting X coordinate.
     * @param fromY Starting Y coordinate.
     * @param move  The move details.
     * @return true if the move is valid, false otherwise.
     */
    public boolean isValidMove(int fromX, int fromY, Move move) {
        // 1. Check if starting position is within bounds
        if (!board.isWithinBounds(fromX, fromY)) {
            Logger.log("MoveValidator", "Starting position (" + fromX + ", " + fromY + ") is out of bounds.");
            return false;
        }

        // 2. Check if there are enough pieces to move
        if (board.getStackSize(fromX, fromY) < move.getNumberOfPieces()) {
            Logger.log("MoveValidator", "Not enough pieces to move from the source stack at (" + fromX + ", " + fromY + ").");
            return false;
        }

        // 3. Check if the top piece belongs to the current player
        Piece topPiece = board.getPieceAt(fromX, fromY);
        if (topPiece == null || !topPiece.getOwner().equals(currentPlayer)) {
            Logger.log("MoveValidator", "Top piece at (" + fromX + ", " + fromY + ") does not belong to the current player.");
            return false;
        }

        // 4. Check if number of pieces to move does not exceed carry limit
        if (move.getNumberOfPieces() > board.getCarryLimit()) {
            Logger.log("MoveValidator", "Number of pieces to move (" + move.getNumberOfPieces() + ") exceeds carry limit (" + board.getCarryLimit() + ").");
            return false;
        }

        // 5. Check if drop counts sum up correctly and are positive
        int totalDrops = 0;
        for (int count : move.getDropCounts()) {
            if (count <= 0) {
                Logger.log("MoveValidator", "Invalid drop count: " + count + ". Must be a positive integer.");
                return false;
            }
            totalDrops += count;
        }
        if (totalDrops != move.getNumberOfPieces()) {
            Logger.log("MoveValidator", "Sum of drop counts (" + totalDrops + ") does not equal the number of pieces to move (" + move.getNumberOfPieces() + ").");
            return false;
        }

        // 6. Validate each drop position and stacking rules
        int currentX = fromX;
        int currentY = fromY;
        for (int count : move.getDropCounts()) {
            // Move in the specified direction
            currentX += move.getDirection().getDeltaX();
            currentY += move.getDirection().getDeltaY();

            // Check if new position is within bounds
            if (!board.isWithinBounds(currentX, currentY)) {
                Logger.log("MoveValidator", "Drop position (" + currentX + ", " + currentY + ") is out of bounds.");
                return false;
            }

            // Check stacking rules
            Piece destinationTop = board.getPieceAt(currentX, currentY);
            if (destinationTop != null) {
                Piece movingPiece = move.getMovedPieces().isEmpty() ? topPiece : move.getMovedPieces().get(0); // Top moving piece
                if (!canStackOnTop(movingPiece, destinationTop)) {
                    Logger.log("MoveValidator", "Cannot stack " + movingPiece.getPieceType() + " on top of " + destinationTop.getPieceType() + " at (" + currentX + ", " + currentY + ").");
                    return false;
                }
            }
        }

        // All validations passed
        Logger.log("MoveValidator", "Move from (" + fromX + ", " + fromY + ") is valid.");
        return true;
    }

    /**
     * Checks if the moving piece can be legally stacked on top of the target piece.
     *
     * @param movingPiece The piece attempting to stack.
     * @param targetPiece The top piece at the target location.
     * @return true if the moving piece can be stacked, false otherwise.
     */
    private boolean canStackOnTop(Piece movingPiece, Piece targetPiece) {
        if (targetPiece.getPieceType() == Piece.PieceType.CAPSTONE) {
            return false; // Cannot stack on top of a capstone
        }
        if (targetPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
            return movingPiece.isCapstone(); // Only capstone can flatten standing stone
        }
        return true; // Flat stones can be stacked on flat stones
    }
}
