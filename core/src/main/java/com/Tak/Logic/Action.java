package com.Tak.Logic;

/**
 * The Action class represents an action that can be taken by a player,
 * including the placement and movement of pieces.
 */
public abstract class Action {

    /**
     * Executes the action on the given game board.
     *
     * @param board The game board on which to execute the action.
     * @throws InvalidMoveException If the action is invalid according to game rules.
     */
    public abstract void execute(Board board) throws InvalidMoveException;

    /**
     * Undoes the action on the given game board.
     *
     * @param board The game board on which to undo the action.
     */
    public abstract void undo(Board board);
}
