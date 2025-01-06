// File: core/src/main/java/com/Tak/AI/evaluation/RoadConnectivity.java
package com.Tak.AI.utils;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;
import java.io.Serializable;
import java.util.*;

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

        // Check Left-Right connection
        for (int y = 0; y < size; y++) {
            Piece topPiece = board.getPieceAt(0, y);
            if (topPiece != null && topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                Set<String> visited = new HashSet<>();
                if (hasRoadDFS(player, board, 0, y, visited, size, true)) {
                    return true;
                }
            }
        }

        // Check Top-Bottom connection
        for (int x = 0; x < size; x++) {
            Piece topPiece = board.getPieceAt(x, 0);
            if (topPiece != null && topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                Set<String> visited = new HashSet<>();
                if (hasRoadDFS(player, board, x, 0, visited, size, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs DFS to check for a continuous road.
     *
     * @param player        The player whose road is being checked.
     * @param board         The game board.
     * @param x             Current X coordinate.
     * @param y             Current Y coordinate.
     * @param visited       Set to keep track of visited positions.
     * @param size          Size of the board.
     * @param isHorizontal  True if checking Left-Right, false if checking Top-Bottom.
     * @return True if a continuous road is found, false otherwise.
     */
    private boolean hasRoadDFS(Player player, Board board, int x, int y, Set<String> visited, int size, boolean isHorizontal) {
        if (isHorizontal && x == size - 1) {
            return true;
        }
        if (!isHorizontal && y == size - 1) {
            return true;
        }

        visited.add(x + "," + y);

        for (Direction dir : Direction.values()) {
            int newX = x + dir.getDeltaX();
            int newY = y + dir.getDeltaY();

            if (board.isWithinBounds(newX, newY) && !visited.contains(newX + "," + newY)) {
                PieceStack stack = board.getBoardStack(newX, newY);
                if (!stack.isEmpty()) {
                    Piece topPiece = stack.getTopPiece();
                    if (topPiece.getOwner().equals(player) && topPiece.canBePartOfRoad()) {
                        if (hasRoadDFS(player, board, newX, newY, visited, size, isHorizontal)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the opponent is close to winning (e.g., one move away).
     *
     * @param board    The game board.
     * @param opponent The opponent player.
     * @return True if the opponent is close to winning, false otherwise.
     */
    public boolean isOpponentCloseToWinning(Board board, Player opponent) {
        // Implement logic to determine if the opponent is close to winning
        // For simplicity, check if opponent's road potential is at max - 1
        int opponentRoadPotential = calculateRoadPotential(board, opponent);
        return opponentRoadPotential >= board.getSize() - 1;
    }

    /**
     * Calculates the road potential for a player.
     *
     * @param board  The game board.
     * @param player The player.
     * @return An integer representing road completion potential.
     */
    public int calculateRoadPotential(Board board, Player player) {
        // Implement a heuristic to estimate the player's road potential
        // For simplicity, return the longest path length
        int maxPathLength = 0;

        // Check horizontal paths
        for (int y = 0; y < board.getSize(); y++) {
            int pathLength = 0;
            for (int x = 0; x < board.getSize(); x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty() && stack.getTopPiece().getOwner().equals(player) && stack.getTopPiece().canBePartOfRoad()) {
                    pathLength++;
                    maxPathLength = Math.max(maxPathLength, pathLength);
                } else {
                    pathLength = 0;
                }
            }
        }

        // Check vertical paths
        for (int x = 0; x < board.getSize(); x++) {
            int pathLength = 0;
            for (int y = 0; y < board.getSize(); y++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (!stack.isEmpty() && stack.getTopPiece().getOwner().equals(player) && stack.getTopPiece().canBePartOfRoad()) {
                    pathLength++;
                    maxPathLength = Math.max(maxPathLength, pathLength);
                } else {
                    pathLength = 0;
                }
            }
        }

        return maxPathLength;
    }

    /**
     * Calculates the connectedness of the player's pieces towards road completion.
     *
     * @param board  The game board.
     * @param player The player.
     * @return An integer representing connectedness.
     */
    public int calculateConnectedness(Board board, Player player) {
        // Implement a method to calculate connectedness
        // For simplicity, return the number of connected components
        int connectedComponents = 0;
        Set<String> visited = new HashSet<>();

        for (int y = 0; y < board.getSize(); y++) {
            for (int x = 0; x < board.getSize(); x++) {
                String key = x + "," + y;
                if (!visited.contains(key)) {
                    PieceStack stack = board.getBoardStack(x, y);
                    if (!stack.isEmpty() && stack.getTopPiece().getOwner().equals(player) && stack.getTopPiece().canBePartOfRoad()) {
                        exploreConnectedComponent(board, player, x, y, visited);
                        connectedComponents++;
                    }
                }
            }
        }

        return connectedComponents;
    }

    private void exploreConnectedComponent(Board board, Player player, int x, int y, Set<String> visited) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{x, y});
        visited.add(x + "," + y);

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int currX = current[0];
            int currY = current[1];

            for (Direction dir : Direction.values()) {
                int newX = currX + dir.getDeltaX();
                int newY = currY + dir.getDeltaY();
                String key = newX + "," + newY;

                if (board.isWithinBounds(newX, newY) && !visited.contains(key)) {
                    PieceStack adjStack = board.getBoardStack(newX, newY);
                    if (!adjStack.isEmpty() && adjStack.getTopPiece().getOwner().equals(player) && adjStack.getTopPiece().canBePartOfRoad()) {
                        stack.push(new int[]{newX, newY});
                        visited.add(key);
                    }
                }
            }
        }
    }

    /**
     * Calculates the blocking potential for the player.
     *
     * @param board  The game board.
     * @param player The player performing the blocking.
     * @return An integer representing blocking potential.
     */
    public int calculateBlockingPotential(Board board, Player player) {
        // Implement logic to calculate blocking potential
        // For simplicity, count the number of opponent's road paths that can be blocked
        int blockingPotential = 0;
        Player opponent = player.getOpponent();

        for (int y = 0; y < board.getSize(); y++) {
            for (int x = 0; x < board.getSize(); x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (stack.isEmpty()) {
                    continue;
                }
                Piece topPiece = stack.getTopPiece();
                if (topPiece.getOwner().equals(opponent) && topPiece.canBePartOfRoad()) {
                    for (Direction dir : Direction.values()) {
                        int adjX = x + dir.getDeltaX();
                        int adjY = y + dir.getDeltaY();
                        if (board.isWithinBounds(adjX, adjY)) {
                            PieceStack adjStack = board.getBoardStack(adjX, adjY);
                            if (adjStack.isEmpty()) {
                                blockingPotential++;
                            } else {
                                Piece adjTopPiece = adjStack.getTopPiece();
                                if (!adjTopPiece.getOwner().equals(opponent)) {
                                    blockingPotential++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return blockingPotential;
    }
    /**
     * Determines if the player is close to winning (e.g., one move away).
     *
     * @param board  The game board.
     * @param player The player.
     * @return True if the player is close to winning, false otherwise.
     */
    public boolean isPlayerCloseToWinning(Board board, Player player) {
        // Implement logic similar to isOpponentCloseToWinning
        // For simplicity, check if the player's road potential is close to the board size
        int playerRoadPotential = calculateRoadPotential(board, player);
        return playerRoadPotential >= board.getSize() - 1;
    }
}
