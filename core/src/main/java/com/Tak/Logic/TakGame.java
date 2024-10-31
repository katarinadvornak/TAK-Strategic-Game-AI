package com.Tak.Logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.Tak.AI.AIPlayer;

/**
 * The TakGame class manages the overall game flow.
 * It handles player turns, move execution, and checks for win conditions.
 */
public class TakGame {

    private Board board;              // The game board
    private Player player1;           // Player 1 (Black)
    private Player player2;           // Player 2 (White)
    private Player currentPlayer;     // The player whose turn it is
    private WinChecker winChecker;    // Utility to check win conditions
    private boolean gameEnded;        // Flag to indicate if the game has ended
    private Player winner;            // The winning player, if any
    private int moveCount;            // To track the number of moves made

    /**
     * Constructor to initialize the game with the board and players.
     *
     * @param boardSize The size of the board.
     * @param useAI     Whether to use an AI player.
     */
    public TakGame(int boardSize, boolean useAI) {
        this.board = new Board(boardSize);
        int flatStones = calculateFlatStones(boardSize);
        int capstones = calculateCapstones(boardSize);
        int standingStones = calculateStandingStones(boardSize);

        if (useAI) {
            this.player1 = new HumanPlayer(Player.Color.BLACK, flatStones, standingStones, capstones);
            this.player2 = new AIPlayer(Player.Color.WHITE, flatStones, standingStones, capstones, 3); // Example searchDepth = 3
        } else {
            this.player1 = new HumanPlayer(Player.Color.BLACK, flatStones, standingStones, capstones);
            this.player2 = new HumanPlayer(Player.Color.WHITE, flatStones, standingStones, capstones);
        }
        // Initialize currentPlayer to Player 1 (Black)
        this.currentPlayer = player1;

        this.winChecker = new WinChecker();
        this.gameEnded = false;
        this.moveCount = 0;
    }

    /**
     * Calculates the number of flat stones based on board size.
     *
     * @param boardSize The size of the board.
     * @return The number of flat stones.
     */
    private int calculateFlatStones(int boardSize) {
        switch (boardSize) {
            case 3:
                return 10;
            case 4:
                return 15;
            case 5:
                return 21;
            case 6:
                return 30;
            case 8:
                return 50;
            default:
                return 21;
        }
    }

    private int calculateStandingStones(int boardSize) {
        switch (boardSize) {
            case 3:
                return 0; // Or appropriate value
            case 4:
                return 0; // Or appropriate value
            case 5:
                return 10; // Adjust based on game rules
            case 6:
                return 15;
            case 8:
                return 25;
            default:
                return 10;
        }
    }

    /**
     * Calculates the number of capstones based on board size.
     *
     * @param boardSize The size of the board.
     * @return The number of capstones.
     */
    private int calculateCapstones(int boardSize) {
        switch (boardSize) {
            case 5:
                return 1;
            case 6:
                return 1;
            case 8:
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Returns the current move count.
     *
     * @return The move count.
     */
    public int getMoveCount() {
        return this.moveCount;
    }

    /**
     * Increments the move count.
     */
    public void incrementMoveCount() {
        this.moveCount++;
    }

    /**
     * Gets the winner of the game.
     *
     * @return The winning player, or null if no winner yet.
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Starts the game. Any initial setup can be done here.
     */
    public void startGame() {
        // Initialization logic if needed
    }

    /**
     * Places a piece on the board for the current player.
     *
     * @param x         The X coordinate.
     * @param y         The Y coordinate.
     * @param pieceType The type of piece to place.
     * @throws GameOverException    If the game has ended.
     * @throws InvalidMoveException If the move is invalid.
     */
    public void placePiece(int x, int y, Piece.PieceType pieceType) throws GameOverException, InvalidMoveException {
        if (gameEnded) {
            throw new GameOverException("Game has ended.");
        }

        Player pieceOwner = currentPlayer;

        if (moveCount < 2) {
            // For the first two moves, players place opponent's flat stones
            if (pieceType != Piece.PieceType.FLAT_STONE) {
                throw new InvalidMoveException("Only flat stones can be placed on the first two moves.");
            }
            // The piece owner is the opponent
            pieceOwner = getOpponentPlayer();
        }

        if (pieceOwner.hasPiecesLeft(pieceType)) {
            Piece piece = new Piece(pieceType, pieceOwner);
            board.placePiece(x, y, piece, currentPlayer);
            pieceOwner.decrementPiece(pieceType); // Decrement from the actual owner
            incrementMoveCount();
            checkWinConditions();
            if (!gameEnded) {
                switchPlayer();
            }
        } else {
            throw new InvalidMoveException("No remaining pieces of this type.");
        }
    }

    /**
     * Gets the opponent player.
     *
     * @return The opponent player.
     */
    public Player getOpponentPlayer() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Checks for win conditions after a move and updates scores accordingly.
     */
    public void checkWinConditions() {
        if (winChecker.checkForRoadWin(currentPlayer, board)) {
            System.out.println(currentPlayer.getColor() + " wins by road!");
            winner = currentPlayer;
            gameEnded = true;
            currentPlayer.incrementScore(1);
        } else {
            Player flatWinPlayer = winChecker.checkForFlatWin(this);
            if (flatWinPlayer != null) {
                System.out.println(flatWinPlayer.getColor() + " wins by flat count!");
                winner = flatWinPlayer;
                gameEnded = true;
                flatWinPlayer.incrementScore(1);
            }
        }
    }

    /**
     * Switches the current player.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Gets the current game board.
     *
     * @return The game board.
     */
    public Board getBoard() {
        return this.board;
    }

    /**
     * Gets the current player.
     *
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Gets Player 1 (Black).
     *
     * @return Player 1.
     */
    public Player getPlayer1() {
        return this.player1;
    }

    /**
     * Gets Player 2 (White).
     *
     * @return Player 2.
     */
    public Player getPlayer2() {
        return this.player2;
    }

    /**
     * Checks if the game has ended.
     *
     * @return true if the game has ended, false otherwise.
     */
    public boolean isGameEnded() {
        return this.gameEnded;
    }

    /**
     * Resets the game to its initial state.
     * Optionally resets player scores if desired.
     *
     * @param resetScores Whether to reset player scores.
     */
    public void resetGame(boolean resetScores) {
        board.resetBoard();
        currentPlayer = player1; // Start with Black as per Tak rules
        gameEnded = false;
        moveCount = 0;
        int flatStones = calculateFlatStones(board.getSize());
        int standingStones = calculateStandingStones(board.getSize());
        int capstones = calculateCapstones(board.getSize());
        player1.resetPieces(flatStones, standingStones, capstones);
        player2.resetPieces(flatStones, standingStones, capstones);
        if (resetScores) {
            player1.resetScore();
            player2.resetScore();
        }
    }

    /**
     * Moves a stack of pieces starting from a position in a specified direction,
     * dropping a specified number of pieces along the way.
     *
     * @param startX     The starting X coordinate.
     * @param startY     The starting Y coordinate.
     * @param direction  The direction to move.
     * @param dropCounts An array specifying how many pieces to drop at each step.
     * @throws InvalidMoveException If the move is invalid.
     * @throws GameOverException    If the game has ended.
     */
    public void moveStack(int startX, int startY, Direction direction, int[] dropCounts) throws InvalidMoveException, GameOverException {
        System.out.println("moveStack called with startX: " + startX + ", startY: " + startY + ", direction: " + direction + ", dropCounts: " + Arrays.toString(dropCounts));

        if (gameEnded) {
            throw new GameOverException("Game has ended.");
        }

        List<Piece> stack = board.getBoardPosition(startX, startY);
        if (stack.isEmpty()) {
            throw new InvalidMoveException("No pieces to move at the selected position.");
        }

        Piece topPiece = stack.get(stack.size() - 1);
        if (topPiece.getOwner() != currentPlayer) {
            throw new InvalidMoveException("You can only move stacks where your piece is on top.");
        }

        int totalPiecesToMove = 0;
        for (int count : dropCounts) {
            if (count <= 0) {
                throw new InvalidMoveException("Drop counts must be positive integers.");
            }
            totalPiecesToMove += count;
        }

        int maxCarryLimit = Math.min(5, stack.size()); // Max pieces that can be carried
        if (totalPiecesToMove > maxCarryLimit) {
            throw new InvalidMoveException("Cannot move that many pieces.");
        }

        // Calculate movement increments
        int dx = 0, dy = 0;
        switch (direction) {
            case UP:    dy = 1;  break;
            case DOWN:  dy = -1; break;
            case LEFT:  dx = -1; break;
            case RIGHT: dx = 1;  break;
            default:
                throw new InvalidMoveException("Invalid direction.");
        }

        int x = startX;
        int y = startY;
        List<Piece> movingPieces = new ArrayList<>(stack.subList(stack.size() - totalPiecesToMove, stack.size()));
        stack.subList(stack.size() - totalPiecesToMove, stack.size()).clear();

        for (int i = 0; i < dropCounts.length; i++) {
            int dropCount = dropCounts[i];

            x += dx;
            y += dy;

            if (!board.isValidPosition(x, y)) {
                throw new InvalidMoveException("Move goes off the board.");
            }

            if (movingPieces.isEmpty()) {
                throw new InvalidMoveException("No more pieces to drop.");
            }

            if (dropCount > movingPieces.size()) {
                throw new InvalidMoveException("Not enough pieces to drop.");
            }

            List<Piece> destinationStack = board.getBoardPosition(x, y);

            // Enforce max stack height
            if (destinationStack.size() + dropCount > 5) {
                throw new InvalidMoveException("Cannot exceed maximum stack height of 5.");
            }

            // Handle capstone crushing standing stones
            Piece topDestinationPiece = destinationStack.isEmpty() ? null : destinationStack.get(destinationStack.size() - 1);

            if (topDestinationPiece != null) {
                if (topDestinationPiece.getPieceType() == Piece.PieceType.STANDING_STONE) {
                    if (movingPieces.get(0).getPieceType() == Piece.PieceType.CAPSTONE && dropCount == 1) {
                        // Capstone crushes the standing stone
                        topDestinationPiece.setPieceType(Piece.PieceType.FLAT_STONE);
                    } else {
                        throw new InvalidMoveException("Cannot move onto a standing stone unless dropping a capstone.");
                    }
                } else if (topDestinationPiece.getPieceType() == Piece.PieceType.CAPSTONE) {
                    throw new InvalidMoveException("Cannot move onto a capstone.");
                }
            }

            // Drop pieces
            List<Piece> piecesToDrop = new ArrayList<>(movingPieces.subList(0, dropCount));
            destinationStack.addAll(piecesToDrop);
            movingPieces.subList(0, dropCount).clear();

            // If all pieces have been dropped, break out of the loop
            if (movingPieces.isEmpty()) {
                break;
            }
        }

        if (!movingPieces.isEmpty()) {
            throw new InvalidMoveException("All pieces must be dropped during the move.");
        }

        incrementMoveCount();
        checkWinConditions();
        if (!gameEnded) {
            switchPlayer();
        }
        System.out.println("Move completed successfully.");
    }
}
