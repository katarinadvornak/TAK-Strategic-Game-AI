// File: core/src/main/java/com/Tak/AI/StateActionPair.java
package com.Tak.AI;

import com.Tak.Logic.Action;
import com.Tak.Logic.Board;
import com.Tak.Logic.Piece;

import java.util.List;
import java.util.Objects;

/**
 * The StateActionPair class represents a combination of a game state and an action,
 * used as a key in the Q-table for reinforcement learning.
 */
public class StateActionPair {
    
    private final String stateHash;
    private final Action action;
    
    /**
     * Constructs a StateActionPair with the specified state and action.
     *
     * @param board  The game board state.
     * @param action The action taken.
     */
    public StateActionPair(Board board, Action action) {
        this.stateHash = hashBoard(board);
        this.action = action;
    }
    
    /**
     * Generates a unique hash for the board state.
     *
     * @param board The game board.
     * @return A unique string representing the board state.
     */
    private String hashBoard(Board board) {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                List<Piece> stack = board.getBoardPosition(x, y);
                if (stack.isEmpty()) {
                    sb.append("E"); // Empty
                } else {
                    Piece topPiece = stack.get(stack.size() - 1);
                    sb.append(topPiece.getPieceType().toString().charAt(0));
                    sb.append(topPiece.getOwner().toString().charAt(0));
                    sb.append(stack.size()); // Include stack height for better differentiation
                }
                sb.append(",");
            }
            sb.append(";");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StateActionPair)) return false;
        StateActionPair other = (StateActionPair) obj;
        return Objects.equals(this.stateHash, other.stateHash) &&
               Objects.equals(this.action, other.action);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(stateHash, action);
    }
    
    public Action getAction() {
        return action;
    }
    
    public String getStateHash() {
        return stateHash;
    }
}
