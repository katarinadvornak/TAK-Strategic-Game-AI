package com.Tak.Logic;

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
    private Player winner;
    private int moveCount;            // To track the number of moves made

    /**
     * Constructor to initialize the game with the board and players.
     *
     * @param boardSize The size of the board.
     */
    public TakGame(int boardSize) {
        this.board = new Board(boardSize);
        int flatStones = calculateFlatStones(boardSize);
        int capstones = calculateCapstones(boardSize);
        int standingStones = calculateStandingStones(boardSize);

        this.player1 = new Player(Player.Color.BLACK, flatStones, standingStones, capstones);
        this.player2 = new Player(Player.Color.WHITE, flatStones, standingStones, capstones);

        // Initialize currentPlayer to Player 2 (White)
        this.currentPlayer = player2;

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
     * @throws GameOverException     If the game has ended.
     * @throws InvalidMoveException  If the move is invalid.
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

        if (currentPlayer.hasPiecesLeft(pieceType)) {
            Piece piece = new Piece(pieceType, pieceOwner);
            board.placePiece(x, y, piece, currentPlayer);
            currentPlayer.decrementPiece(pieceType);
            moveCount++;
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
    private Player getOpponentPlayer() {
        return (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Checks for win conditions after a move and updates scores accordingly.
     */
    private void checkWinConditions() {
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
     */
    public void resetGame() {
        board.resetBoard();
        currentPlayer = player2; // Start with White as per Tak rules
        gameEnded = false;
        moveCount = 0;
        int flatStones = calculateFlatStones(board.getSize());
        int standingStones = calculateStandingStones(board.getSize());
        int capstones = calculateCapstones(board.getSize());
        player1.resetPieces(flatStones, standingStones, capstones);
        player2.resetPieces(flatStones, standingStones, capstones);
        // Optionally reset scores if you want scores to reset with a new game
        // player1.resetScore();
        // player2.resetScore();
    }
}
