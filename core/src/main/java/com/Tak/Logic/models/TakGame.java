// File: core/src/main/java/com/Tak/Logic/models/TakGame.java
package com.Tak.Logic.models;

import com.Tak.AI.players.MinimaxAgent;
import com.Tak.AI.players.RandomAIPlayer;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.players.HumanPlayer;
import com.Tak.Logic.utils.Logger;
import com.Tak.Logic.validators.MoveExecutor;
import com.Tak.Logic.validators.MoveValidator;
import com.Tak.Logic.validators.WinChecker;
import com.Tak.Logic.managers.GameStateManager;
import com.Tak.Logic.models.Player.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The TakGame class manages the game state, including the board, players, move counts, and win conditions.
 */
public class TakGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private int moveCount;
    private boolean isGameEnded;
    private Player winner;

    private transient WinChecker winChecker; // Marked as transient if not serializable
    private transient GameStateManager gameStateManager; // Marked as transient if not serializable

    /**
     * Constructs a TakGame with the specified board size, AI usage, and number of AI players.
     * This constructor initializes players internally.
     *
     * @param boardSize       The size of the board (e.g., 5 for a 5x5 board).
     * @param useAI           Whether to include AI players.
     * @param aiPlayersCount  Number of AI players (0 for all human players, 1 for one AI player, 2 for AI vs AI).
     */
    public TakGame(int boardSize, boolean useAI, int aiPlayersCount) {
        board = new Board(boardSize, boardSize);
        players = new ArrayList<>();
        board.setPlayers(players); // Set the Board's players list
        currentPlayerIndex = 0;
        moveCount = 0;
        isGameEnded = false;
        winner = null;

        initializePlayers(useAI, aiPlayersCount); // Initialize players
        winChecker = new WinChecker();
        gameStateManager = new GameStateManager(board, players);
    }

    /**
     * Constructs a TakGame with the specified board size and a custom list of players.
     * This constructor allows for more flexible player initialization.
     *
     * @param boardSize The size of the board (e.g., 5 for a 5x5 board).
     * @param players   The list of players participating in the game.
     */
    public TakGame(int boardSize, List<Player> players) {
        board = new Board(boardSize, boardSize);
        this.players = players;
        board.setPlayers(players); // Set the Board's players list
        currentPlayerIndex = 0;
        moveCount = 0;
        isGameEnded = false;
        winner = null;

        // Ensure opponents are set correctly
        if (players.size() >= 2) {
            players.get(0).setOpponent(players.get(1));
            players.get(1).setOpponent(players.get(0));
            //Logger.log("TakGame", "Set opponents for the first two players.");
        }

        //Logger.log("TakGame", "Initialized TakGame with custom players.");

        winChecker = new WinChecker();
        gameStateManager = new GameStateManager(board, players);
    }

    /**
     * Replaces an existing player with a new player and updates opponent references.
     *
     * @param oldPlayer The player to replace.
     * @param newPlayer The new player.
     */
    public synchronized void replacePlayer(Player oldPlayer, Player newPlayer) {
        int index = players.indexOf(oldPlayer);
        if (index != -1) {
            players.set(index, newPlayer);
            
            // Set opponents
            newPlayer.setOpponent(oldPlayer.getOpponent());
            oldPlayer.getOpponent().setOpponent(newPlayer); // Corrected: 'newPlayer' instead of 'newAI'
            
            // Update ownership of all Pieces owned by oldPlayer
            Board board = getBoard(); // Ensure this method correctly retrieves the board
            for (int x = 0; x < board.getSize(); x++) {
                for (int y = 0; y < board.getSize(); y++) {
                    List<Piece> stack = board.getBoardPosition(x, y);
                    for (Piece piece : stack) {
                        if (piece.getOwner().equals(oldPlayer)) {
                            piece.setOwner(newPlayer);
                            Logger.log("TakGame", "Updated piece at (" + x + ", " + y + ") to new owner: " + newPlayer.getColor());
                        }
                    }
                }
            }
    
            // If the replaced player was the current player, the currentPlayerIndex remains valid
            if (players.get(currentPlayerIndex).equals(oldPlayer)) {
                // currentPlayerIndex points to the new player
                // No action needed as players.set(index, newPlayer) already updated the list
            }
    
            Logger.log("TakGame", "Replaced player " + oldPlayer.getColor() + " with " + newPlayer.getColor());
        } else {
            Logger.log("TakGame", "Player to replace not found.");
        }
    }
    
        

    /**
     * Ends the game as a tie.
     */
    public void endGameAsTie() {
        isGameEnded = true;
        winner = null;
        //Logger.log("TakGame", "Game ended as a tie.");
    }

    /**
     * Initializes players based on whether AI is used and the number of AI players.
     *
     * @param useAI           Whether to include AI players.
     * @param aiPlayersCount  Number of AI players to include.
     */
    private void initializePlayers(boolean useAI, int aiPlayersCount) {
        if (!useAI || aiPlayersCount == 0) {
            // Add two HumanPlayers
            Player player1 = new HumanPlayer(Player.Color.BLUE, 21, 21, 1); // Human Player BLUE
            Player player2 = new HumanPlayer(Player.Color.GREEN, 21, 21, 1); // Human Player GREEN
            player1.setOpponent(player2);
            player2.setOpponent(player1);
            players.add(player1);
            players.add(player2);
            //Logger.log("TakGame", "Added two HumanPlayers: BLUE and GREEN.");
        } else {
            // Add AIPlayers based on aiPlayersCount
            if (aiPlayersCount == 1) {
                Player player1 = new HumanPlayer(Player.Color.BLUE, 15, 6, 1); // Human Player BLUE
                Player aiPlayer = new MinimaxAgent(Player.Color.GREEN, 21, 1, 1, 3); // Example piece counts
                player1.setOpponent(aiPlayer);
                aiPlayer.setOpponent(player1);
                players.add(player1);
                players.add(aiPlayer);
                Logger.log("TakGame", "Added HumanPlayer BLUE and AIPlayer GREEN.");
            } else if (aiPlayersCount == 2) {
                Player aiPlayer1 = new MinimaxAgent(Player.Color.BLUE, 21, 1, 1, 3);
                Player aiPlayer2 = new MinimaxAgent(Player.Color.GREEN, 21, 1, 1, 3);
                aiPlayer1.setOpponent(aiPlayer2);
                aiPlayer2.setOpponent(aiPlayer1);
                players.add(aiPlayer1);
                players.add(aiPlayer2);
                Logger.log("TakGame", "Added two AIPlayers: BLUE and GREEN.");
            } else {
                throw new IllegalArgumentException("aiPlayersCount must be 0, 1, or 2.");
            }
        }

        // Log the final players list
        Logger.log("TakGame", "Final Players List:");
        for (Player p : players) {
            Logger.log("TakGame", p.getClass().getSimpleName() + " - Color: " + p.getColor());
        }

    }

    /**
     * Adds a player to the game.
     *
     * @param player The player to add.
     */
    public void addPlayer(Player player) {
        // Ensure no duplicate players are added
        if (!players.contains(player)) {
            players.add(player);
            // Update opponents if necessary
            if (players.size() == 2) {
                players.get(0).setOpponent(players.get(1));
                players.get(1).setOpponent(players.get(0));
                //Logger.log("TakGame", "Set opponents for players.");
            }
        }
    }

    /**
     * Retrieves the opponent player.
     *
     * @return The opponent Player.
     */
    public Player getOpponentPlayer() {
        return getCurrentPlayer().getOpponent();
    }

    /**
     * Moves a stack of pieces on the board based on the specified direction and drop counts.
     * This method should be used by both human and AI players to ensure centralized move handling.
     *
     * @param fromX      The starting X coordinate.
     * @param fromY      The starting Y coordinate.
     * @param direction  The direction to move.
     * @param dropCounts The number of pieces to drop at each step.
     * @throws InvalidMoveException If the move is invalid.
     * @throws GameOverException    If the game has already ended.
     */
    public synchronized void moveStack(int fromX, int fromY, Direction direction, int[] dropCounts) throws InvalidMoveException, GameOverException {
        if (isGameEnded) {
            throw new GameOverException("The game has already ended.");
        }

        Player currentPlayer = getCurrentPlayer();
        //Logger.log("TakGame", currentPlayer.getColor() + " is moving stack from (" + fromX + ", " + fromY + ") " + direction + " with drop counts " + Arrays.toString(dropCounts));
        int numberOfPieces = 0;
        for (int count : dropCounts) {
            numberOfPieces += count;
        }
        if (numberOfPieces > board.getCarryLimit()) {
            throw new InvalidMoveException("Cannot carry more than " + board.getCarryLimit() + " pieces.");
        }

        List<Integer> dropCountsList = new ArrayList<>();
        for (int count : dropCounts) {
            dropCountsList.add(count);
        }

        Move move = new Move(fromX, fromY, direction, numberOfPieces, dropCountsList);
        move.setPlayer(currentPlayer); // Assign the current player to the move

        // Validate the move before executing
        MoveValidator validator = new MoveValidator(board, currentPlayer);
        if (validator.isValidMove(fromX, fromY, move)) {
            MoveExecutor executor = new MoveExecutor(board, currentPlayer);
            executor.executeMove(fromX, fromY, move);
            //Logger.log("TakGame", currentPlayer.getColor() + " moved stack from (" + fromX + ", " + fromY + ") " + direction);
        } else {
            throw new InvalidMoveException("Move validation failed.");
        }

        // Update move count and check for win conditions
        incrementMoveCount();
        checkWinConditions();
        switchPlayer();
    }

    /**
     * Places a piece on the board.
     * This method should be used by both human and AI players to ensure centralized move handling.
     *
     * @param x         The X coordinate.
     * @param y         The Y coordinate.
     * @param pieceType The type of piece to place.
     * @throws InvalidMoveException If the placement is invalid.
     * @throws GameOverException    If the game has already ended.
     */
    public synchronized void placePiece(int x, int y, Piece.PieceType pieceType) throws InvalidMoveException, GameOverException {
        placePiece(x, y, pieceType, getCurrentPlayer());
    }

    /**
     * Places a piece on the board with a specified owner.
     * This method should be used by both human and AI players to ensure centralized move handling.
     *
     * @param x          The X coordinate.
     * @param y          The Y coordinate.
     * @param pieceType  The type of piece to place.
     * @param pieceOwner The owner of the piece being placed.
     * @throws InvalidMoveException If the placement is invalid.
     * @throws GameOverException    If the game has already ended.
     */
    public synchronized void placePiece(int x, int y, Piece.PieceType pieceType, Player pieceOwner) throws InvalidMoveException, GameOverException {
        if (isGameEnded) {
            throw new GameOverException("The game has already ended.");
        }

        Player currentPlayer = getCurrentPlayer();
        //Logger.log("TakGame", currentPlayer.getColor() + " is placing a " + pieceType + " at (" + x + ", " + y + ") on behalf of " + pieceOwner.getColor());
        Piece piece = new Piece(pieceType, pieceOwner);
        board.placePiece(x, y, piece);
        pieceOwner.decrementPiece(pieceType);
        //Logger.log("TakGame", currentPlayer.getColor() + " placed a " + pieceType + " at (" + x + ", " + y + ")");
        incrementMoveCount();
        checkWinConditions();
        switchPlayer();
    }

    /**
     * Increments the move count.
     */
    public synchronized void incrementMoveCount() {
        moveCount++;
        //Logger.log("TakGame", "Move count incremented to: " + moveCount);
    }

    /**
     * Returns the current move count.
     *
     * @return The move count.
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Returns the game board.
     *
     * @return The Board instance.
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Checks for game end conditions and sets the winner if applicable.
     */
    public synchronized void checkWinConditions() {
        if (gameStateManager.isGameOver()) {
            isGameEnded = true;
            winner = gameStateManager.getWinner();
            if (winner != null) {
                winner.incrementScore(1); // Increment winner's score by 1 (or more as per game rules)
                //Logger.log("TakGame", "Player " + winner.getColor() + " wins the game!");
            } else {
                //Logger.log("TakGame", "The game ended in a tie.");
            }
            return;
        }
        // No win conditions met yet
        //Logger.log("TakGame", "No win conditions met yet.");
    }

    /**
     * Resets the game state.
     *
     * @param resetScores Whether to reset player scores.
     */
    public synchronized void resetGame(boolean resetScores) {
        board.reset();
        moveCount = 0;
        isGameEnded = false;
        winner = null;
        currentPlayerIndex = 0;

        gameStateManager.resetGame();

        for (Player player : players) {
            player.resetPieces(21, 1); // Standard Counts
            if (resetScores) {
                player.resetScore();
            }
        }
        //Logger.log("TakGame", "Game has been reset. Move count: " + moveCount + ", Game ended: " + isGameEnded);
    }

    /**
     * Switches the current player to the next player in the list.
     */
    public synchronized void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Player newPlayer = getCurrentPlayer();
        //Logger.log("TakGame", "Switched to player: " + newPlayer.getColor());
    }

    /**
     * Returns the current player.
     *
     * @return The current Player.
     */
    public synchronized Player getCurrentPlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException("Players list is empty. No current player available.");
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns the board size.
     *
     * @return The board size.
     */
    public int getBoardSize() {
        return board.getSize();
    }

    /**
     * Determines if the game has ended.
     *
     * @return True if the game has ended, false otherwise.
     */
    public boolean isGameEnded() {
        return isGameEnded;
    }

    /**
     * Returns the winner of the game.
     *
     * @return The winning Player, or null if it's a tie.
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Returns Player1 (BLUE).
     *
     * @return Player1.
     */
    public Player getPlayer1() {
        return players.get(0);
    }

    /**
     * Returns Player2 (GREEN).
     *
     * @return Player2.
     */
    public Player getPlayer2() {
        return players.get(1);
    }

    /**
     * Returns the list of players.
     *
     * @return The list of players.
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Checks if the board is full.
     *
     * @return True if the board is full, false otherwise.
     */
    public boolean isBoardFull() {
        return board.isFull();
    }

    // Serialization methods to handle transient fields
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        // No need to serialize transient fields
    }

    private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, java.io.IOException {
        in.defaultReadObject();
        // Re-initialize transient fields
        winChecker = new WinChecker();
        gameStateManager = new GameStateManager(board, players);
    }

    public void logFinalBoardState() {
        StringBuilder boardState = new StringBuilder();
        boardState.append("Final Board State:\n");

        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                Piece piece = board.getPieceAt(row, col);
                if (piece != null) {
                    boardState.append(piece.toString()).append(" ");
                } else {
                    boardState.append(". ");
                }
            }
            boardState.append("\n");
        }

        //Logger.log("Game", boardState.toString());
    }
}
