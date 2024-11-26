// File: com/Tak/AI/Test/ComplexityAnalysisTest.java
package com.Tak.AI.experiments;

import com.Tak.AI.players.MinimaxAgent;
import com.Tak.AI.players.RandomAIPlayer;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.models.Player.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * ComplexityAnalysisTest runs multiple games between MinimaxAgent and RandomAIPlayer
 * across different search depths and board sizes, collecting complexity metrics.
 */
public class ComplexityAnalysisTest {

    /**
     * Runs a series of games between MinimaxAgent and RandomAIPlayer at various depths and board sizes.
     */
    public static void runExtendedAnalysis() {
        int[] boardSizes = {4,5,6,7}; // Different board sizes
        int[] depths = {1,2,3,4}; // Different search depths
        int gamesPerDepth = 10; // Number of games per depth

        for (int boardSize : boardSizes) {
            for (int depth : depths) {
                System.out.println("========================================");
                System.out.println("Analyzing Minimax Depth: " + depth + " on " + boardSize + "x" + boardSize + " board");
                System.out.println("========================================");

                // Initialize players
                MinimaxAgent minimaxPlayer = new MinimaxAgent(Color.GREEN, 21, 1, 1, depth);
                RandomAIPlayer randomPlayer = new RandomAIPlayer(Color.BLUE, 21, 1, 1);

                // Set opponents
                minimaxPlayer.setOpponent(randomPlayer);
                randomPlayer.setOpponent(minimaxPlayer);

                // Initialize TakGame with the two players
                List<Player> players = new ArrayList<>();
                players.add(randomPlayer); // Player 0: RandomAIPlayer
                players.add(minimaxPlayer); // Player 1: MinimaxAgent
                TakGame game = new TakGame(boardSize, players);

                // Metrics to collect
                int minimaxWins = 0;
                int randomWins = 0;
                int draws = 0;
                int totalNodesEvaluated = 0;
                long totalTimeTaken = 0;

                // Run multiple games per depth
                for (int gameNum = 1; gameNum <= gamesPerDepth; gameNum++) {
                    System.out.println("Starting Game " + gameNum + " for depth " + depth + " on " + boardSize + "x" + boardSize + " board");
                    game.resetGame(false); // Reset the game without resetting player scores

                    try {
                        while (!game.isGameEnded()) {
                            Player currentPlayer = game.getCurrentPlayer();
                            currentPlayer.makeMove(game);
                        }

                        Player winner = game.getWinner();
                        if (winner == null) {
                            draws++;
                            System.out.println("Game " + gameNum + " ended in a draw.");
                        } else if (winner instanceof MinimaxAgent) {
                            minimaxWins++;
                            System.out.println("Game " + gameNum + " won by MinimaxAgent.");
                        } else if (winner instanceof RandomAIPlayer) {
                            randomWins++;
                            System.out.println("Game " + gameNum + " won by RandomAIPlayer.");
                        }

                        // Collect complexity metrics from MinimaxAgent
                        totalNodesEvaluated += minimaxPlayer.getNodesEvaluated();
                        totalTimeTaken += minimaxPlayer.getTimeTakenMillis();

                        // Print the final board state
                        //System.out.println("Final Board State for Game " + gameNum + ":");
                        //game.getBoard().printBoard();
                        System.out.println(); // Add a blank line for readability

                    } catch (InvalidMoveException e) {
                        System.err.println("Game " + gameNum + " encountered an invalid move: " + e.getMessage());
                    } catch (GameOverException e) {
                        System.out.println("Game " + gameNum + " detected game over: " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Game " + gameNum + " encountered an unexpected error: " + e.getMessage());
                    }
                }

                // Calculate average metrics
                double averageNodes = (double) totalNodesEvaluated / gamesPerDepth;
                double averageTime = (double) totalTimeTaken / gamesPerDepth;

                // Output summarized results
                System.out.println("\n=== Complexity Analysis Summary for Minimax Depth " + depth + " on " + boardSize + "x" + boardSize + " Board ===");
                System.out.println("Total Games Run       : " + gamesPerDepth);
                System.out.println("MinimaxAgent Wins     : " + minimaxWins);
                System.out.println("RandomAIPlayer Wins   : " + randomWins);
                System.out.println("Draws                 : " + draws);
                System.out.printf("Average Nodes Evaluated: %.2f%n", averageNodes);
                System.out.printf("Average Time Taken     : %.2f ms%n", averageTime);
                System.out.println("=====================================================\n");
            }
        }

    }

    /**
     * The main method to execute the extended complexity analysis test.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        runExtendedAnalysis();
    }
}
