package com.Tak.AI.learning;

import com.Tak.AI.actions.Action;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;

import java.util.List;
import java.util.Objects;
import java.io.Serializable;

/**
 * Represents a combination of a game state and an action, used as a key in the Q-table for reinforcement learning.
 */
public class StateActionPair implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * Checks if this StateActionPair is equal to another object.
     *
     * @param obj The object to compare.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StateActionPair)) return false;
        StateActionPair other = (StateActionPair) obj;
        return Objects.equals(this.stateHash, other.stateHash) && Objects.equals(this.action, other.action);
    }

    /**
     * Computes the hash code for this StateActionPair.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(stateHash, action);
    }

    /**
     * Gets the action associated with this StateActionPair.
     *
     * @return The action.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Gets the state hash associated with this StateActionPair.
     *
     * @return The state hash.
     */
    public String getStateHash() {
        return stateHash;
    }
}
