package com.Tak.Logic;

/**
 * Exception thrown when an action is attempted after the game has ended.
 */
public class GameOverException extends Exception {
    public GameOverException(String message) {
        super(message);
    }
}
