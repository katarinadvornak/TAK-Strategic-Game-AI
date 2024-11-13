// File: core/src/main/java/com/Tak/AI/MoveAction.java
package com.Tak.AI;

import com.Tak.Logic.Board;
import com.Tak.Logic.Direction;
import com.Tak.Logic.InvalidMoveException;
import com.Tak.Logic.Piece;
import com.Tak.Logic.Player;
import com.Tak.Logic.Action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The MoveAction class represents a movement action where a player moves a stack of pieces in a direction,
 * dropping pieces along the way.
 */
public class MoveAction extends Action implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private int fromX;
    private int fromY;
    private Direction direction;
    private int numPieces;
    private List<Integer> dropCounts;
    private List<Piece> movedPieces;
    private List<Destination> destinations; // To track previous state for undo
    private boolean executed;
    private Player player; // Added field

    /**
     * Constructs a MoveAction.
     *
     * @param fromX      The starting X coordinate.
     * @param fromY      The starting Y coordinate.
     * @param direction  The direction to move.
     * @param numPieces  The number of pieces to move.
     * @param dropCounts The drop counts for each step.
     * @param player     The player making the move.
     */
    public MoveAction(int fromX, int fromY, Direction direction, int numPieces, List<Integer> dropCounts, Player player) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.direction = direction;
        this.numPieces = numPieces;
        this.dropCounts = new ArrayList<>(dropCounts);
        this.movedPieces = new ArrayList<>();
        this.destinations = new ArrayList<>();
        this.executed = false;
        this.player = player;
    }

    @Override
    public void execute(Board board) throws InvalidMoveException {
        if (executed) {
            throw new InvalidMoveException("Action has already been executed.");
        }
        
        // Retrieve the stack from the starting position
        List<Piece> stack = board.getBoardPosition(fromX, fromY);
        if (stack.size() < numPieces) {
            throw new InvalidMoveException("Not enough pieces to move.");
        }
        
        // Extract the pieces to move
        movedPieces = new ArrayList<>(stack.subList(stack.size() - numPieces, stack.size()));
        stack.subList(stack.size() - numPieces, stack.size()).clear();
        System.out.println("AIPlayer " + player.getColor() + " is moving " + numPieces + " pieces from (" + fromX + ", " + fromY + ") " + direction);
        
        // Initialize current position
        int x = fromX;
        int y = fromY;
        for (int count : dropCounts) {
            // Move in the specified direction
            switch (direction) {
                case UP:
                    y += 1;
                    break;
                case DOWN:
                    y -= 1;
                    break;
                case LEFT:
                    x -= 1;
                    break;
                case RIGHT:
                    x += 1;
                    break;
            }
            
            // Validate position
            if (!board.isWithinBounds(x, y)) {
                // Undo the move before throwing exception
                undo(board);
                throw new InvalidMoveException("Move goes out of bounds.");
            }
            
            // Retrieve destination stack
            List<Piece> destStack = board.getBoardPosition(x, y);
            
            // Check stacking rules
            Piece movingTopPiece = movedPieces.get(movedPieces.size() - 1);
            if (!destStack.isEmpty()) {
                Piece destTopPiece = destStack.get(destStack.size() - 1);
                if (!canStackOnTop(movingTopPiece, destTopPiece)) {
                    // Undo the move before throwing exception
                    undo(board);
                    throw new InvalidMoveException("Cannot stack on top of the target stack.");
                }
                
                // Handle capstone crushing standing stones
                if (movingTopPiece.isCapstone() && destTopPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
                    destTopPiece.setPieceType(Piece.PieceType.FLAT_STONE);
                    System.out.println("Capstone crushed standing stone at (" + x + ", " + y + ")");
                }
            }
            
            // Drop pieces
            List<Piece> piecesToDrop = new ArrayList<>(movedPieces.subList(0, count));
            movedPieces.subList(0, count).clear();
            destStack.addAll(piecesToDrop);
            System.out.println("Dropped " + count + " piece(s) at (" + x + ", " + y + ")");
            
            // Track previous destinations for undo
            destinations.add(new Destination(x, y, new ArrayList<>(piecesToDrop)));
        }
        
        executed = true;
        System.out.println("MoveAction executed successfully.");
    }

    @Override
    public void undo(Board board) throws InvalidMoveException {
        if (!executed) {
            throw new InvalidMoveException("Action has not been executed yet.");
        }
        
        // Reverse the dropCounts to undo the move in reverse order
        for (int i = destinations.size() - 1; i >= 0; i--) {
            Destination dest = destinations.get(i);
            List<Piece> destStack = board.getBoardPosition(dest.getX(), dest.getY());
            
            // Remove the dropped pieces
            for (int j = 0; j < dest.getDroppedPieces().size(); j++) {
                if (destStack.isEmpty()) {
                    throw new InvalidMoveException("Cannot undo move: Destination stack is empty.");
                }
                Piece topPiece = destStack.get(destStack.size() - 1);
                Piece expectedPiece = dest.getDroppedPieces().get(j);
                if (!topPiece.equals(expectedPiece)) {
                    throw new InvalidMoveException("Cannot undo move: Piece mismatch.");
                }
                destStack.remove(destStack.size() - 1);
                System.out.println("Removed " + expectedPiece.getPieceType() + " from (" + dest.getX() + ", " + dest.getY() + ")");
            }
            
            // Restore to the original stack
            board.getBoardPosition(fromX, fromY).addAll(dest.getDroppedPieces());
            System.out.println("Restored " + dest.getDroppedPieces().size() + " piece(s) to (" + fromX + ", " + fromY + ")");
        }
        
        // Restore the moved pieces to the original stack
        List<Piece> originalStack = board.getBoardPosition(fromX, fromY);
        originalStack.addAll(movedPieces);
        System.out.println("Restored moved pieces to (" + fromX + ", " + fromY + ")");
        
        // Clear tracking lists
        movedPieces.clear();
        destinations.clear();
        executed = false;
        System.out.println("MoveAction undone successfully.");
    }

    /**
     * Checks if the moving piece can be stacked on the target piece.
     *
     * @param movingPiece The piece being moved.
     * @param targetPiece The piece on top of the destination stack.
     * @return true if stacking is allowed, false otherwise.
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

    @Override
    public String toString() {
        return "Move " + numPieces + " piece(s) from (" + fromX + ", " + fromY + ") " + direction + " with drop counts " + dropCounts;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public Player getActionPlayer() {
        return this.player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveAction)) return false;
        MoveAction other = (MoveAction) obj;
        return this.fromX == other.fromX &&
               this.fromY == other.fromY &&
               this.direction == other.direction &&
               this.numPieces == other.numPieces &&
               this.dropCounts.equals(other.dropCounts) &&
               this.player.equals(other.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromX, fromY, direction, numPieces, dropCounts, player);
    }

    // Inner class Destination
    private static class Destination implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int x;
        private int y;
        private List<Piece> droppedPieces;

        public Destination(int x, int y, List<Piece> droppedPieces) {
            this.x = x;
            this.y = y;
            this.droppedPieces = droppedPieces;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public List<Piece> getDroppedPieces() {
            return droppedPieces;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Destination)) return false;
            Destination other = (Destination) obj;
            return this.x == other.x &&
                   this.y == other.y &&
                   this.droppedPieces.equals(other.droppedPieces);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, droppedPieces);
        }
    }
}
