// File: core/src/main/java/com/Tak/Logic/WinChecker.java
package com.Tak.Logic;

import java.util.Objects;
import com.Tak.AI.RoadConnectivity;
/**
 * The WinChecker class is responsible for determining if a player has won the game.
 * It checks for road wins and flat wins.
 */
public class WinChecker {

    /**
     * Checks whether the current player has achieved a road win.
     * A road win occurs when a continuous line of flat stones or capstones
     * connects two opposite sides of the board (horizontally or vertically).
     *
     * @param player The player whose road win is being checked.
     * @param board  The current state of the board.
     * @return true if the player has achieved a road win, false otherwise.
     */
    public boolean checkForRoadWin(Player player, Board board) {
        RoadConnectivity roadChecker = new RoadConnectivity();
        return roadChecker.checkForRoadWin(player, board);
    }

    /**
     * Determines the player with the most visible flat stones if the board is full.
     *
     * @param board The current game board.
     * @return The player with the most flat stones, or null if it's a tie.
     */
    public Player getTopPlayer(Board board) {
        if (board.isFull()) {
            Player player1 = board.getPlayers().get(0);
            Player player2 = board.getPlayers().get(1);
            int player1Flats = countFlatStones(player1, board);
            int player2Flats = countFlatStones(player2, board);
            
            if (player1Flats > player2Flats) {
                return player1;
            } else if (player2Flats > player1Flats) {
                return player2;
            }
        }
        return null; // Tie or board not full
    }

    /**
     * Counts the number of flat stones owned by the player on top of stacks.
     *
     * @param player The player.
     * @param board  The game board.
     * @return The count of flat stones.
     */
    private int countFlatStones(Player player, Board board) {
        int flatStoneCount = 0;
        int size = board.getSize();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Piece piece = board.getPieceAt(x, y);
                if (piece != null && piece.getOwner() == player && piece.getPieceType() == Piece.PieceType.FLAT_STONE) {
                    flatStoneCount++;
                }
            }
        }
        return flatStoneCount;
    }
}
