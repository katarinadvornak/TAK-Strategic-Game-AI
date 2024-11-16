package com.Tak.AI.evaluation;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import java.io.Serializable;
import java.util.*;

/**
 * RoadConnectivity class checks if a player has a continuous road connecting any two opposite sides
 * and provides utility functions related to road completion and blocking.
 */
public class RoadConnectivity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Checks for a road win for the specified player.
     *
     * @param player The player to check for a road win.
     * @param board  The current game board.
     * @return True if the player has a road win, false otherwise.
     */
    public boolean checkForRoadWin(Player player, Board board) {
        int size = board.getSize();

        //Logger.log("RoadConnectivity", "Checking road win for " + player.getColor());

        // Check Left-Right connection
        boolean leftRightWin = false;
        for (int y = 0; y < size; y++) {
            Piece topPiece = board.getPieceAt(0, y);
            if (topPiece != null && topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                Set<String> visited = new HashSet<>();
                if (hasRoadBFS(player, board, 0, y, visited, size, true)) {
                    Logger.log("RoadConnectivity", player.getColor() + " has a Left-Right road win.");
                    leftRightWin = true;
                    break;
                }
            }
        }

        // Check Top-Bottom connection only if Left-Right hasn't been satisfied
        boolean topBottomWin = false;
        if (!leftRightWin) {
            for (int x = 0; x < size; x++) {
                Piece topPiece = board.getPieceAt(x, 0);
                if (topPiece != null && topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                    Set<String> visited = new HashSet<>();
                    if (hasRoadBFS(player, board, x, 0, visited, size, false)) {
                        Logger.log("RoadConnectivity", player.getColor() + " has a Top-Bottom road win.");
                        topBottomWin = true;
                        break;
                    }
                }
            }
        }

        return leftRightWin || topBottomWin;
    }

    /**
     * Performs BFS to check for a continuous road.
     *
     * @param player        The player whose road is being checked.
     * @param board         The game board.
     * @param startX        Starting X coordinate.
     * @param startY        Starting Y coordinate.
     * @param visited       Set to keep track of visited positions.
     * @param size          Size of the board.
     * @param isHorizontal  True if checking Left-Right, false if checking Top-Bottom.
     * @return True if a continuous road is found, false otherwise.
     */
    private boolean hasRoadBFS(Player player, Board board, int startX, int startY, Set<String> visited, int size, boolean isHorizontal) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited.add(startX + "," + startY);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];
            if (isHorizontal && x == size - 1) {
                Logger.log("RoadConnectivity", "Reached opposite side at (" + x + "," + y + ")");
                return true;
            }
            if (!isHorizontal && y == size - 1) {
                Logger.log("RoadConnectivity", "Reached opposite side at (" + x + "," + y + ")");
                return true;
            }

            // Explore all orthogonal neighbors
            for (Direction dir : Direction.values()) {
                int newX = x + dir.getDeltaX();
                int newY = y + dir.getDeltaY();

                if (board.isWithinBounds(newX, newY) && !visited.contains(newX + "," + newY)) {
                    Piece topPiece = board.getBoardStack(newX, newY).getTopPiece();
                    if (topPiece != null && topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                        queue.add(new int[]{newX, newY});
                        visited.add(newX + "," + newY);
                        Logger.log("RoadConnectivity", "Adding (" + newX + "," + newY + ") to queue");
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if a piece can block the opponent's road.
     *
     * @param piece The piece to check.
     * @return True if the piece can block, false otherwise.
     */
    public boolean canBlockRoad(Piece piece) {
        return piece.getPieceType() == Piece.PieceType.CAPSTONE;
    }
}
