package com.Tak.AI;

import com.Tak.Logic.*;
import java.util.Objects;

/**
 * The StateActionPair class represents a combination of a game state and an action,
 * used as a key in the Q-table for reinforcement learning.
 */
public class StateActionPair {

    private Board state;
    private Move action;

    /**
     * Constructs a StateActionPair with the specified state and action.
     *
     * @param state  The game state.
     * @param action The action taken in that state.
     */
    public StateActionPair(Board state, Move action) {
        // Initialize the state-action pair
        this.state = state.copy();
        this.action = action; // Assuming Move is immutable or acceptable to reference directly
    }

    /**
     * Checks if this StateActionPair is equal to another object.
     *
     * @param obj The object to compare.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StateActionPair)) return false;
        StateActionPair other = (StateActionPair) obj;
        return Objects.equals(state, other.state) && Objects.equals(action, other.action);
    }

    /**
     * Returns a hash code for this StateActionPair.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(state, action);
    }

    // Getters for state and action

    /**
     * Gets the state part of the StateActionPair.
     *
     * @return The game state.
     */
    public Board getState() {
        return state;
    }

    /**
     * Gets the action part of the StateActionPair.
     *
     * @return The action taken.
     */
    public Move getAction() {
        return action;
    }
}
