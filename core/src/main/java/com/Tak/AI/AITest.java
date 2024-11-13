// File: core/src/main/java/com/Tak/AI/AITest.java
package com.Tak.AI;

import com.Tak.Logic.*;
import com.Tak.utils.Logger;
public class AITest {
    public static void main(String[] args) {
        // Initialize Logger for the test environment
        Logger.initialize();

        // Initialize TakGame with two AIPlayers (AI vs AI)
        int boardSize = 5;
        boolean useAI = true;
        int aiPlayersCount = 2; // Set to 2 for AI vs AI

        TakGame game = new TakGame(boardSize, useAI, aiPlayersCount); // 5x5 board with AI vs AI

        // **Logging**
        Logger.log("AITest", "Starting AI vs AI Test with Players:");
        for (Player p : game.getPlayers()) {
            Logger.log("AITest", p.getClass().getSimpleName() + " - Color: " + p.getColor());
        }

        int numGames = 1; // Start with 1 game for initial testing
        for (int i = 0; i < numGames; i++) {
            Logger.log("AITest", "======================================");
            Logger.log("AITest", "Starting Game " + (i + 1));
            game.resetGame(true); // Reset both board and scores

            while (!game.isGameEnded()) {
                Player currentPlayer = game.getCurrentPlayer();
                Logger.log("AITest", "Current Player: " + currentPlayer.getColor());
                try {
                    currentPlayer.makeMove(game);
                    Logger.log("AITest", "Move executed successfully by " + currentPlayer.getColor());
                } catch (InvalidMoveException | GameOverException e) {
                    Logger.log("AITest", "Error during game: " + e.getMessage());
                    break;
                }
            }

            Logger.log("AITest", "Game " + (i + 1) + " ended. Winner: " + 
                (game.getWinner() != null ? game.getWinner().getColor() : "Tie"));

                
            // Save AI States after each game
            for (Player p : game.getPlayers()) {
                if (p instanceof AIPlayer) {
                    ((AIPlayer) p).saveState();
                    Logger.log("AITest", "Saved state for AIPlayer: " + p.getColor());
                }
            }
        }

        Logger.log("AITest", "All games completed.");
    }
}


