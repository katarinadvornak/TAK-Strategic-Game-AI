// File: core/src/main/java/com/Tak/AI/RoadConnectivity.java
package com.Tak.AI;

import com.Tak.Logic.Board;
import com.Tak.Logic.Piece;
import com.Tak.Logic.Player;

/**
 * The RoadConnectivity class provides methods to check for road connectivity
 * between opposing sides of the board for a given player.
 */
public class RoadConnectivity {
    
    /**
     * Checks if the specified player has a connected road across the board.
     *
     * @param player The player whose road win is being checked.
     * @param board  The current game board.
     * @return True if the player has a road victory, false otherwise.
     */
    public boolean checkForRoadWin(Player player, Board board) {
        int size = board.getSize();
        boolean[][] visited;

        // Check for horizontal road (left to right)
        for (int y = 0; y < size; y++) {
            visited = new boolean[size][size];
            if (dfsRoad(player, board, 0, y, visited, true)) {
                return true;
            }
        }

        // Check for vertical road (top to bottom)
        for (int x = 0; x < size; x++) {
            visited = new boolean[size][size];
            if (dfsRoad(player, board, x, 0, visited, false)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Evaluates the road potential for the player.
     *
     * @param board  The current game board.
     * @param player The player.
     * @return The road potential score.
     */
    public double evaluateRoadPotential(Board board, Player player) {
        return checkForRoadWin(player, board) ? 100.0 : 0.0;
    }

    /**
     * Performs a depth-first search to find a continuous road.
     *
     * @param player        The player.
     * @param board         The game board.
     * @param x             Current X coordinate.
     * @param y             Current Y coordinate.
     * @param visited       2D array to keep track of visited positions.
     * @param isHorizontal  true for horizontal road, false for vertical.
     * @return true if a road is found, false otherwise.
     */
    private boolean dfsRoad(Player player, Board board, int x, int y, boolean[][] visited, boolean isHorizontal) {
        if (!board.isWithinBounds(x, y)) {
            return false;
        }
        if (visited[x][y]) {
            return false;
        }
        Piece piece = board.getPieceAt(x, y);
        if (piece == null || piece.getOwner() != player || !piece.canBePartOfRoad()) {
            return false;
        }
        visited[x][y] = true;

        // Check if we've reached the opposite side
        if (isHorizontal && x == board.getSize() - 1) {
            return true;
        }
        if (!isHorizontal && y == board.getSize() - 1) {
            return true;
        }

        // Explore neighboring positions
        int[][] directions = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (dfsRoad(player, board, nx, ny, visited, isHorizontal)) {
                return true;
            }
        }
        return false;
    }
}
