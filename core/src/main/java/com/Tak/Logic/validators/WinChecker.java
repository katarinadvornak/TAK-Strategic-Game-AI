package com.Tak.Logic.validators;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.Player;
import com.Tak.AI.evaluation.RoadConnectivity;
import com.Tak.Logic.utils.Logger;

import java.util.List;

/**
 * The WinChecker class is responsible for determining if a player has won the game.
 * It checks for road wins and flat wins.
 */
public class WinChecker {

    private RoadConnectivity roadChecker;

    public WinChecker() {
        this.roadChecker = new RoadConnectivity();
    }

    /**
     * Checks whether the player has achieved a road win.
     *
     * @param player The player whose road win is being checked.
     * @param board  The current state of the board.
     * @return true if the player has achieved a road win, false otherwise.
     */
    public boolean checkForRoadWin(Player player, Board board) {
        boolean hasRoad = roadChecker.checkForRoadWin(player, board);
        if (hasRoad) {
            //Logger.log("WinChecker", "Player " + player.getColor() + " has achieved a road win.");
        }
        return hasRoad;
    }

    /**
     * Determines the player with the most visible flat stones if the board is full.
     *
     * @param board   The current game board.
     * @param players The list of players.
     * @return The player with the most flat stones, or null if it's a tie.
     */
    public Player getTopPlayer(Board board, List<Player> players) {
        if (!board.isFull()) {
            return null; // Board is not full yet
        }

        Player topPlayer = null;
        int maxFlats = -1;
        boolean isTie = false;

        for (Player player : players) {
            int flats = countFlatStones(player, board);
            //Logger.log("WinChecker", "Player " + player.getColor() + " has " + flats + " flat stone(s).");
            if (flats > maxFlats) {
                maxFlats = flats;
                topPlayer = player;
                isTie = false;
            } else if (flats == maxFlats) {
                isTie = true;
            }
        }

        if (isTie) {
            //Logger.log("WinChecker", "The game is a tie based on flat stones.");
            return null; // Tie
        } else {
            //Logger.log("WinChecker", "Player " + topPlayer.getColor() + " wins by majority of flat stones.");
            return topPlayer;
        }
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
                if (piece != null && piece.getOwner().equals(player) && piece.getPieceType() == Piece.PieceType.FLAT_STONE) {
                    flatStoneCount++;
                }
            }
        }
        return flatStoneCount;
    }
}
