package com.Tak.Logic;

import java.util.ArrayList;
import java.util.List;

/**
 * The Move class represents a movement action in Tak.
 * It includes information about the move's starting position,
 * direction, number of pieces being moved, and drop counts.
 * The class provides methods to execute and undo the move on a game board.
 */
public class Move extends Action {

    private int startX;                 // Starting X coordinate.
    private int startY;                 // Starting Y coordinate.
    private Direction direction;        // Direction of movement.
    private int numberOfPieces;         // Number of pieces being moved.
    private List<Integer> dropCounts;   // Number of pieces to drop at each space.

    // State to store for undo functionality
    private List<Piece> movedPieces;    // Pieces that were moved during the execution
    private List<Integer> positionsX;   // X positions of the move path
    private List<Integer> positionsY;   // Y positions of the move path

    /**
     * Constructor to initialize a move.
     *
     * @param startX         The starting X coordinate.
     * @param startY         The starting Y coordinate.
     * @param direction      The direction of movement.
     * @param numberOfPieces The number of pieces being moved.
     * @param dropCounts     The list of drop counts.
     */
    public Move(int startX, int startY, Direction direction, int numberOfPieces, List<Integer> dropCounts) {
        this.startX = startX;
        this.startY = startY;
        this.direction = direction;
        this.numberOfPieces = numberOfPieces;
        this.dropCounts = dropCounts;

        // Initialize lists for undo functionality
        this.movedPieces = new ArrayList<>();
        this.positionsX = new ArrayList<>();
        this.positionsY = new ArrayList<>();
    }

    /**
     * Executes the move on the given game board.
     *
     * @param board The game board on which to execute the move.
     * @throws InvalidMoveException If the move is invalid according to game rules.
     */
    @Override
    public void execute(Board board) throws InvalidMoveException {
        int x = startX;
        int y = startY;

        List<Piece> fromStack = board.getBoardPosition(x, y);
        if (fromStack.size() < numberOfPieces) {
            throw new InvalidMoveException("Not enough pieces to move from the starting position.");
        }

        // Extract the pieces to move
        List<Piece> piecesToMove = new ArrayList<>(fromStack.subList(fromStack.size() - numberOfPieces, fromStack.size()));
        fromStack.subList(fromStack.size() - numberOfPieces, fromStack.size()).clear();

        // Store moved pieces for undo functionality
        movedPieces.addAll(piecesToMove);
        positionsX.add(x);
        positionsY.add(y);

        // Direction increments
        int dx = 0, dy = 0;
        switch (direction) {
            case UP:    dy = 1;  break;
            case DOWN:  dy = -1; break;
            case LEFT:  dx = -1; break;
            case RIGHT: dx = 1;  break;
            default:
                throw new InvalidMoveException("Invalid direction.");
        }

        // Perform the move, dropping pieces along the path
        for (int dropCount : dropCounts) {
            x += dx;
            y += dy;

            if (!board.isWithinBounds(x, y)) {
                throw new InvalidMoveException("Move goes out of bounds.");
            }

            List<Piece> destinationStack = board.getBoardPosition(x, y);

            // Check stacking rules
            if (!destinationStack.isEmpty()) {
                Piece topPiece = destinationStack.get(destinationStack.size() - 1);
                Piece movingPiece = piecesToMove.get(0);

                if (!canStackOnTop(movingPiece, topPiece)) {
                    throw new InvalidMoveException("Cannot stack on top of the destination stack.");
                }

                // Handle capstone flattening standing stones
                if (movingPiece.isCapstone() && topPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
                    topPiece.setPieceType(Piece.PieceType.FLAT_STONE);
                }
            }

            // Drop the pieces onto the destination stack
            List<Piece> piecesToDrop = new ArrayList<>(piecesToMove.subList(0, dropCount));
            piecesToMove.subList(0, dropCount).clear();
            destinationStack.addAll(piecesToDrop);

            // Store positions for undo functionality
            positionsX.add(x);
            positionsY.add(y);

            if (piecesToMove.isEmpty()) {
                break;
            }
        }

        if (!piecesToMove.isEmpty()) {
            throw new InvalidMoveException("Not all pieces were dropped during the move.");
        }
    }

    /**
     * Undoes the move on the given game board.
     *
     * @param board The game board on which to undo the move.
     */
    @Override
    public void undo(Board board) {
        // TODO: Implement the undo functionality for the move.
        // This should reverse the actions performed in the execute method.
        // Steps may include:
        // 1. Remove the moved pieces from their current positions.
        // 2. Restore them back to the original stack.
        // 3. Reverse any state changes (e.g., flattening of standing stones).
        throw new UnsupportedOperationException("Undo method not implemented for Move.");
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
            return false;
        }
        if (targetPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
            return movingPiece.isCapstone();
        }
        return true;
    }

    // Getters for properties if needed
    public int getStartX() {
        return this.startX;
    }

    public int getStartY() {
        return this.startY;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getNumberOfPieces() {
        return this.numberOfPieces;
    }

    public List<Integer> getDropCounts() {
        return this.dropCounts;
    }

    @Override
    public String toString() {
        return "Move: " + numberOfPieces + " pieces from (" + startX + ", " + startY + ") to " + direction + " with dropCounts " + dropCounts;
    }

    public int[] getDropCountsArray() {
        // Convert List<Integer> dropCounts to int[] for easier handling
        return this.dropCounts.stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public Player getPlayer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPlayer'");
    }

    @Override
    public Player getActionPlayer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActionPlayer'");
    }
}
