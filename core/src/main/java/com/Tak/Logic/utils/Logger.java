package com.Tak.Logic.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * The Logger class provides logging capabilities with different levels.
 */
public class Logger {
    public enum Level {
        INFO,
        DEBUG,
        ERROR
    }

    private static Level currentLevel = Level.INFO;
    private static PrintWriter writer;

    /**
     * Initializes the Logger.
     */
    public static void initialize() {
        try {
            writer = new PrintWriter(new FileWriter("game.log", true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize Logger: " + e.getMessage());
        }
    }

    /**
     * Sets the current logging level.
     *
     * @param level The logging level to set.
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }

    /**
     * Logs a message with the specified level.
     *
     * @param tag     The source of the log message.
     * @param message The log message.
     */
    public static void log(String tag, String message) {
        log(Level.INFO, tag, message);
    }

    /**
     * Logs a message with a specified level.
     *
     * @param level   The severity level.
     * @param tag     The source of the log message.
     * @param message The log message.
     */
    public static void log(Level level, String tag, String message) {
        if (level.ordinal() >= currentLevel.ordinal()) {
            String logMessage = String.format("%s [%s] [%s]: %s", 
                LocalDateTime.now(), level, tag, message);
            System.out.println(logMessage);
            if (writer != null) {
                writer.println(logMessage);
            }
        }
    }

    /**
     * Implements the debug logging level.
     *
     * @param tag     The source of the log message.
     * @param message The debug message.
     */
    public static void debug(String tag, String message) {
        log(Level.DEBUG, tag, message);
    }

    /**
     * Closes the Logger.
     */
    public static void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
