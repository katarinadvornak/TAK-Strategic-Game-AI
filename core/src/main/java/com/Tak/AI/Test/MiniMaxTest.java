package com.Tak.AI.Test;

import com.Tak.AI.players.MinimaxAgent;
import com.Tak.AI.players.RandomAIPlayer;
import com.Tak.AI.search.MinimaxAlgorithm;
import com.Tak.AI.search.MiniMaxAlgorithm2;
import java.util.ArrayList;
import java.util.List;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.utils.Logger;
import com.Tak.Logic.models.Player.Color;

public class MiniMaxTest {

    public static void singleMove(){
        // Set up a basic board scenario
        int boardSize = 5; // You can adjust this for larger or smaller boards

        // Initialize a simple Tak game with players
        Player whitePlayer = new MinimaxAgent(Color.WHITE, 21, 1, 1, 3); // Example piece counts
        Player blackPlayer = new MinimaxAgent(Color.BLACK, 21, 1, 1, 3);

        List<Player> players = new ArrayList<>();
        players.add(whitePlayer);
        players.add(blackPlayer);

        // Set opponents for AIPlayers
        whitePlayer.setOpponent(blackPlayer);
        blackPlayer.setOpponent(whitePlayer);

        TakGame game = new TakGame(boardSize, players);

        try {
             // Display initial message
        System.out.println("White player (MinimaxAgent) is thinking...");

        // Execute the chosen move and continue the game
        whitePlayer.makeMove(game);
        //blackPlayer.makeMove(game);

        } catch (InvalidMoveException e) {
            System.err.println("An invalid move was attempted: " + e.getMessage());
        } catch (GameOverException e) {
            System.out.println("Game over detected: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public static void gameAI(){
        // Set up a basic board scenario
        int boardSize = 5; // You can adjust this for larger or smaller boards
        int currentPlayerIndex = 0;

        // Initialize a simple Tak game with players
        Player minimaxPlayer = new MinimaxAgent(Color.WHITE, 21, 1, 1, 3); // Example piece counts
        Player randomPlayer = new RandomAIPlayer(Color.BLACK, 21, 1, 1);

        List<Player> players = new ArrayList<>();
        players.add(randomPlayer);
        players.add(minimaxPlayer);

        // Set opponents for AIPlayers
        minimaxPlayer.setOpponent(randomPlayer);
        randomPlayer.setOpponent(minimaxPlayer);
        int moveCount = 0;

        TakGame game = new TakGame(boardSize, players);

        while (!game.isGameEnded()) {
                Player currentPlayer = game.getCurrentPlayer();
                try {
                    currentPlayer.makeMove(game);
                    moveCount++;
                    game.switchPlayer();
                } catch (InvalidMoveException | GameOverException e) {
                    Logger.log("MiniMaxTest", "Exception encountered: " + e.getMessage());
                    break;
                }
        }

        // Determine the winner and update counts
        Player winner = game.getWinner();
        Logger.log("MiniMaxTest", " Winner: " + (winner == minimaxPlayer ? "Minimax AI" : "Random AI"));
        Board board = game.getBoard();
        board.printBoard();
        Logger.log("MiniMaxTest", " Move count: " + moveCount);
            


    }

    public static void main(String[] args) {
        gameAI();
        
    }
}

