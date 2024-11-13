// File: core/src/main/java/com/Tak/Logic/Action.java
package com.Tak.Logic;

import com.Tak.Logic.Player;

/**
 * The Action class represents a generic action in the Tak game.
 * It serves as an abstract base class for specific action types like Placement and MoveAction.
 */
public abstract class Action {
    
    /**
     * Executes the action on the given board.
     *
     * @param board The game board where the action will be executed.
     * @throws InvalidMoveException If the action is invalid or cannot be performed.
     */
    public abstract void execute(Board board) throws InvalidMoveException;
    
    /**
     * Undoes the action on the given board.
     *
     * @param board The game board where the action will be undone.
     * @throws InvalidMoveException If the action cannot be undone.
     */
    public abstract void undo(Board board) throws InvalidMoveException;
    
    /**
     * Provides a string representation of the action.
     *
     * @return A string describing the action.
     */
    @Override
    public abstract String toString();
    
    /**
     * Gets the player associated with this action.
     *
     * @return The player performing the action.
     */
    public abstract Player getPlayer();
    
    /**
     * Gets the player who is performing the action.
     *
     * @return The action player.
     */
    public abstract Player getActionPlayer();
}
