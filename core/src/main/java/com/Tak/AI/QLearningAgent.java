// File: core/src/main/java/com/Tak/AI/QLearningAgent.java
package com.Tak.AI;

import com.Tak.Logic.*;
import com.Tak.Logic.Piece.PieceType;
import com.Tak.utils.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 * The QLearningAgent class implements the Q-Learning algorithm for the TAK game.
 * It maintains a Q-Table mapping state-action pairs to Q-values and updates them based on game outcomes.
 */
public class QLearningAgent {
    
    private Map<String, Map<Action, Double>> qTable; // Q-Table: State -> (Action -> Q-Value)
    private double learningRate; // α
    private double discountFactor; // γ
    private double explorationRate; // ε
    
    private static final String QTABLE_FILE = "qtable.ser"; // File to save the Q-Table
    
    /**
     * Constructs a QLearningAgent with default parameters.
     */
    public QLearningAgent() {
        qTable = new HashMap<>();
        this.learningRate = 0.1;
        this.discountFactor = 0.95;
        this.explorationRate = 1.0;
        loadQTable(); // Load existing Q-Table if available
    }
    
    /**
     * Generates a unique hash representing the current state for this player.
     *
     * @param board The current game board.
     * @param player The AIPlayer instance.
     * @return A string representing the state.
     */
    public String generateStateHash(Board board, AIPlayer player) {
        StringBuilder sb = new StringBuilder();
        sb.append(player.getColor().toString()).append("_");
        for (int y = 0; y < board.getSize(); y++) {
            for (int x = 0; x < board.getSize(); x++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece == null) {
                    sb.append("E"); // Empty
                } else {
                    sb.append(topPiece.getPieceType().toString().charAt(0));
                    sb.append(topPiece.getOwner().getColor().toString().charAt(0));
                }
                sb.append(",");
            }
            sb.append(";");
        }
        return sb.toString();
    }
    
    /**
     * Selects the best action based on the current Q-Table for a given state.
     *
     * @param state The current state hash.
     * @return The action with the highest Q-value.
     */
    public Action selectBestAction(String state) {
        if (!qTable.containsKey(state)) {
            return null; // No known actions for this state
        }
        Map<Action, Double> actions = qTable.get(state);
        Action bestAction = null;
        double maxQ = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Action, Double> entry : actions.entrySet()) {
            if (entry.getValue() > maxQ) {
                maxQ = entry.getValue();
                bestAction = entry.getKey();
            }
        }
        return bestAction;
    }
    
    /**
     * Selects a random valid action from the current board state.
     *
     * @param board The current game board.
     * @param player The AIPlayer instance.
     * @return A randomly selected action.
     */
    public Action selectRandomAction(Board board, AIPlayer player) {
        List<Action> possibleActions = getAllPossibleActions(board, player);
        if (possibleActions.isEmpty()) {
            return null;
        }
        return possibleActions.get(new java.util.Random().nextInt(possibleActions.size()));
    }
    
    /**
     * Retrieves all possible actions from the current board state for the player.
     *
     * @param board  The current game board.
     * @param player The player for whom to retrieve actions.
     * @return A list of possible actions.
     */
    private List<Action> getAllPossibleActions(Board board, AIPlayer player) {
        List<Action> actions = new ArrayList<>();
        int size = board.getSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Piece topPiece = board.getPieceAt(x, y);
                if (topPiece != null && topPiece.getOwner() == player && topPiece.canBePartOfRoad()) {
                    // Possible placement actions
                    if (player.hasPiecesLeft(Piece.PieceType.FLAT_STONE)) {
                        actions.add(new Placement(x, y, Piece.PieceType.FLAT_STONE, player));
                    }
                    if (player.hasPiecesLeft(Piece.PieceType.CAPSTONE)) {
                        actions.add(new Placement(x, y, Piece.PieceType.CAPSTONE, player));
                    }
                    
                    // Possible move actions
                    for (Direction dir : Direction.values()) {
                        // Generate possible drop counts based on carry limit
                        int carryLimit = board.getCarryLimit();
                        List<List<Integer>> possibleDrops = generateDropCounts(carryLimit, 4); // Assuming max 4 steps
                        for (List<Integer> drop : possibleDrops) {
                            int totalDrops = drop.stream().mapToInt(Integer::intValue).sum();
                            if (totalDrops <= carryLimit) {
                                MoveAction move = new MoveAction(x, y, dir, totalDrops, drop, player);
                                actions.add(move);
                            }
                        }
                    }
                }
            }
        }
        return actions;
    }
    
    /**
     * Generates all possible drop counts for a given carry limit and number of steps.
     *
     * @param carryLimit The maximum number of pieces to carry.
     * @param steps      The number of steps to distribute the drops.
     * @return A list of possible drop counts.
     */
    private List<List<Integer>> generateDropCounts(int carryLimit, int steps) {
        List<List<Integer>> results = new ArrayList<>();
        generateDropCountsHelper(carryLimit, steps, new ArrayList<>(), results);
        return results;
    }
    
    /**
     * Helper method to recursively generate drop counts.
     */
    private void generateDropCountsHelper(int remaining, int steps, List<Integer> current, List<List<Integer>> results) {
        if (steps == 1) {
            current.add(remaining);
            results.add(new ArrayList<>(current));
            current.remove(current.size() - 1);
            return;
        }
        for (int i = 1; i <= remaining - (steps - 1); i++) {
            current.add(i);
            generateDropCountsHelper(remaining - i, steps - 1, current, results);
            current.remove(current.size() - 1);
        }
    }
    
    /**
     * Updates the Q-Table based on the observed transition.
     *
     * @param state The previous state.
     * @param action The action taken.
     * @param reward The observed reward.
     * @param nextState The resulting state after the action.
     */
    public void updateQTable(String state, Action action, double reward, String nextState) {
        qTable.putIfAbsent(state, new HashMap<>());
        Map<Action, Double> actions = qTable.get(state);
        double oldQ = actions.getOrDefault(action, 0.0);
        double maxFutureQ = 0.0;
        if (qTable.containsKey(nextState)) {
            maxFutureQ = qTable.get(nextState).values().stream().mapToDouble(v -> v).max().orElse(0.0);
        }
        double newQ = oldQ + learningRate * (reward + discountFactor * maxFutureQ - oldQ);
        actions.put(action, newQ);
        
        Logger.log("QLearningAgent", "Updated Q-Value for State: " + state + ", Action: " + action.toString() + " | Old Q: " + oldQ + ", New Q: " + newQ);
    }
    
    /**
     * Saves the Q-Table to a file for persistence.
     */
    public void saveQTable() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(QTABLE_FILE))) {
            oos.writeObject(qTable);
            Logger.log("QLearningAgent", "Q-Table saved successfully.");
        } catch (IOException e) {
            Logger.log("QLearningAgent", "Failed to save Q-Table: " + e.getMessage());
        }
    }
    
    /**
     * Loads the Q-Table from a file if available.
     */
    @SuppressWarnings("unchecked")
    public void loadQTable() {
        File file = new File(QTABLE_FILE);
        if (!file.exists()) {
            Logger.log("QLearningAgent", "Q-Table file not found. Starting fresh.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            qTable = (Map<String, Map<Action, Double>>) ois.readObject();
            Logger.log("QLearningAgent", "Q-Table loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            Logger.log("QLearningAgent", "Failed to load Q-Table: " + e.getMessage());
        }
    }
    
    /**
     * Resets the Q-Table, removing all learned Q-Values.
     */
    public void resetQTable() {
        qTable.clear();
        saveQTable();
        Logger.log("QLearningAgent", "Q-Table has been reset.");
    }
    
    /**
     * Copies the Q-Table to create a duplicate agent.
     *
     * @return A new QLearningAgent instance with the same Q-Table.
     */
    public QLearningAgent copy() {
        QLearningAgent copy = new QLearningAgent();
        copy.qTable = new HashMap<>();
        for (Map.Entry<String, Map<Action, Double>> entry : this.qTable.entrySet()) {
            copy.qTable.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        copy.learningRate = this.learningRate;
        copy.discountFactor = this.discountFactor;
        copy.explorationRate = this.explorationRate;
        return copy;
    }
}
