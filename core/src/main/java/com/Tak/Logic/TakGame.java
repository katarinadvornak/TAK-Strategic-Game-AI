// File: core/src/main/java/com/Tak/Logic/TakGame.java
package com.Tak.Logic;

import com.Tak.AI.AIPlayer;
import com.Tak.utils.Logger; // Ensure correct package name (Utils with uppercase 'U')
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The TakGame class manages the game state, including the board, players, move counts, and win conditions.
 */
public class TakGame {
    
    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private int moveCount;
    private boolean isGameEnded;
    private Player winner;
    
    /**
     * Constructs a TakGame with the specified board size.
     *
     * @param boardSize       The size of the board (e.g., 5 for a 5x5 board).
     * @param useAI           Whether to include AI players.
     * @param aiPlayersCount  Number of AI players (0 for all human players, 1 for one AI player, 2 for AI vs AI).
     */
    public TakGame(int boardSize, boolean useAI, int aiPlayersCount) {
        board = new Board(boardSize);
        players = new ArrayList<>();
        board.setPlayers(players); // Set the Board's players list
        currentPlayerIndex = 0;
        moveCount = 0;
        isGameEnded = false;
        winner = null;
        initializePlayers(useAI, aiPlayersCount); // **Invoke players initialization**
    }
    
    /**
     * Initializes players based on whether AI is used and the number of AI players.
     *
     * @param useAI          Whether to include AI players.
     * @param aiPlayersCount Number of AI players to include.
     */
    private void initializePlayers(boolean useAI, int aiPlayersCount) {
        if (!useAI || aiPlayersCount == 0) {
            // Add two HumanPlayers
            Player player1 = new HumanPlayer(Player.Color.BLACK, 15, 6, 1); // Human Player BLACK
            Player player2 = new HumanPlayer(Player.Color.WHITE, 15, 6, 1); // Human Player WHITE
            player1.setOpponent(player2);
            player2.setOpponent(player1);
            players.add(player1);
            players.add(player2);
            Logger.log("TakGame", "Added two HumanPlayers: BLACK and WHITE.");
        } else {
            // Add AIPlayers based on aiPlayersCount
            if (aiPlayersCount == 1) {
                Player player1 = new HumanPlayer(Player.Color.BLACK, 15, 6, 1); // Human Player BLACK
                AIPlayer aiPlayer = new AIPlayer(Player.Color.WHITE, 15, 6, 1, 3, true, true); // AI Player WHITE
                player1.setOpponent(aiPlayer);
                aiPlayer.setOpponent(player1);
                players.add(player1);
                players.add(aiPlayer);
                Logger.log("TakGame", "Added HumanPlayer BLACK and AIPlayer WHITE.");
            } else if (aiPlayersCount == 2) {
                AIPlayer aiPlayer1 = new AIPlayer(Player.Color.BLACK, 15, 6, 1, 3, true, true); // AI Player BLACK
                AIPlayer aiPlayer2 = new AIPlayer(Player.Color.WHITE, 15, 6, 1, 3, true, true); // AI Player WHITE
                aiPlayer1.setOpponent(aiPlayer2);
                aiPlayer2.setOpponent(aiPlayer1);
                players.add(aiPlayer1);
                players.add(aiPlayer2);
                Logger.log("TakGame", "Added two AIPlayers: BLACK and WHITE.");
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
                Logger.log("TakGame", "Set opponents for players.");
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
     *
     * @param fromX        The starting X coordinate.
     * @param fromY        The starting Y coordinate.
     * @param direction    The direction to move.
     * @param dropCounts   The number of pieces to drop at each step.
     * @throws InvalidMoveException If the move is invalid.
     * @throws GameOverException    If the game has already ended.
     */
    public void moveStack(int fromX, int fromY, Direction direction, int[] dropCounts) throws InvalidMoveException, GameOverException {
        Player currentPlayer = getCurrentPlayer();
        Logger.log("TakGame", currentPlayer.getColor() + " is moving stack from (" + fromX + ", " + fromY + ") " + direction + " with drop counts " + Arrays.toString(dropCounts));
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
        move.execute(board);
        Logger.log("TakGame", currentPlayer.getColor() + " moved stack from (" + fromX + ", " + fromY + ") " + direction);
        // Update player's piece counts if pieces are placed
        // This depends on how the game logic handles captures or piece transformations
        
        incrementMoveCount();
        checkWinConditions();
        switchPlayer();
    }

    /**
     * Returns the current player.
     *
     * @return The current Player.
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException("Players list is empty. No current player available.");
        }
        return players.get(currentPlayerIndex);
    }
    public void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Player newPlayer = getCurrentPlayer();
        Logger.log("TakGame", "Switched to player: " + newPlayer.getColor());
    }


    public void placePiece(int x, int y, Piece.PieceType pieceType) throws InvalidMoveException {
        Player currentPlayer = getCurrentPlayer();
        Logger.log("TakGame", currentPlayer.getColor() + " is placing a " + pieceType + " at (" + x + ", " + y + ")");
        Piece piece = new Piece(pieceType, currentPlayer);
        board.placePiece(x, y, piece, currentPlayer);
        currentPlayer.decrementPiece(pieceType);
        Logger.log("TakGame", currentPlayer.getColor() + " placed a " + pieceType + " at (" + x + ", " + y + ")");
        incrementMoveCount();
        checkWinConditions();
        switchPlayer();
    }


    /**
     * Increments the move count.
     */
    public void incrementMoveCount() {
        moveCount++;
        Logger.log("TakGame", "Move count incremented to: " + moveCount);
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
    public void checkWinConditions() {
        WinChecker winChecker = new WinChecker();
        for (Player player : players) {
            if (winChecker.checkForRoadWin(player, board)) {
                isGameEnded = true;
                winner = player;
                Logger.log("TakGame", "Win condition met. Winner: " + player.getColor());
                return;
            }
        }
        
        if (board.isFull()) {
            Player topPlayer = winChecker.getTopPlayer(board);
            if (topPlayer != null) {
                isGameEnded = true;
                winner = topPlayer;
                Logger.log("TakGame", "Board is full. Winner: " + topPlayer.getColor());
            } else {
                isGameEnded = true;
                winner = null; // Tie
                Logger.log("TakGame", "Board is full. The game is a tie.");
            }
        } else {
            Logger.log("TakGame", "No win conditions met yet.");
        }
    }
    
    
    /**
     * Resets the game state.
     *
     * @param resetScores Whether to reset player scores.
     */
    public void resetGame(boolean resetScores) {
        board.reset();
        moveCount = 0;
        isGameEnded = false;
        winner = null;
        currentPlayerIndex = 0;
        
        for (Player player : players) {
            player.resetPieces(15, 6, 1); 
            if (resetScores) {
                player.resetScore();
            }
        }
        Logger.log("TakGame", "Game has been reset. Move count: " + moveCount + ", Game ended: " + isGameEnded);
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
     * Returns Player1 (Black).
     *
     * @return Player1.
     */
    public Player getPlayer1() {
        return players.get(0);
    }
    
    /**
     * Returns Player2 (White).
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
    
}
