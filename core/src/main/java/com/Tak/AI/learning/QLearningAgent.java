package com.Tak.AI.learning;

import com.Tak.AI.actions.Action;
import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.AI.utils.ActionGenerator;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.utils.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * The QLearningAgent class implements the Q-Learning algorithm for the TAK game.
 * It maintains a Q-Table mapping state-action pairs to Q-values and updates
 * them based on game outcomes. Incorporates state abstraction via symmetry
 * and ensures robust serialization/deserialization of the Q-Table.
 *
 * Implemented as a Singleton to prevent multiple instances.
 */
public final class QLearningAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final QLearningAgent INSTANCE = new QLearningAgent();

    private Map<String, Map<Action, Double>> qTable;
    private double learningRate;
    private double discountFactor;
    private List<Experience> episodeHistory;
    private static final String QTABLE_FILE = "qtable.ser.gz";
    private transient ExecutorService saveExecutor;
    private transient final Lock saveLock = new ReentrantLock();
    private transient final Lock qTableLock = new ReentrantLock();
    private transient final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private transient final AtomicBoolean isSaveQueued = new AtomicBoolean(false);
    private int maxQTableSize = 10000;
    private int pruningThreshold = 100;
    private Map<String, Integer> stateUpdateFrequency;

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private QLearningAgent() {
        qTable = new HashMap<>();
        stateUpdateFrequency = new HashMap<>();
        this.learningRate = 0.1;
        this.discountFactor = 0.9;
        this.episodeHistory = new ArrayList<>();
        loadQTable();
        initializeTransientFields();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Initializes transient fields.
     */
    private void initializeTransientFields() {
        saveExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Provides access to the Singleton instance of QLearningAgent.
     *
     * @return The singleton instance.
     */
    public static QLearningAgent getInstance() {
        return INSTANCE;
    }

    /**
     * Ensures that the Singleton pattern is maintained during deserialization.
     *
     * @return The singleton instance.
     * @throws ObjectStreamException If an error occurs during deserialization.
     */
    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }

    /**
     * Custom deserialization to reinitialize transient fields.
     *
     * @param ois The ObjectInputStream from which the object is being deserialized.
     * @throws IOException If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        initializeTransientFields();
    }

    /**
     * Generates a unique hash representing the current state for this player,
     * considering symmetrical board configurations to reduce Q-Table size.
     *
     * @param board The game board.
     * @param player The current player.
     * @return A unique string representing the board state with symmetry.
     */
    public String generateStateHashSymmetrically(Board board, Player player) {
        List<String> symHashes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Board rotated = board.rotateClockwise(i);
            symHashes.add(generateStateHash(rotated, player));
        }
        String minHash = Collections.min(symHashes);
        return minHash;
    }

    /**
     * Generates a unique hash for the board state without considering symmetry.
     *
     * @param board The game board.
     * @param player The current player.
     * @return A unique string representing the board state.
     */
    private String generateStateHash(Board board, Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(player.getColor().toString()).append("_");
        sb.append(board.getMoveCount()).append("_");
        sb.append(player.getRemainingPieces(Piece.PieceType.FLAT_STONE)).append("_");
        sb.append(player.getRemainingPieces(Piece.PieceType.CAPSTONE)).append("_");
        sb.append(player.getOpponent().getRemainingPieces(Piece.PieceType.FLAT_STONE)).append("_");
        sb.append(player.getOpponent().getRemainingPieces(Piece.PieceType.CAPSTONE)).append("_");

        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getCols(); x++) {
                PieceStack stack = board.getBoardStack(x, y);
                if (stack.isEmpty()) {
                    sb.append("E,");
                } else {
                    Piece topPiece = stack.getTopPiece();
                    sb.append(topPiece.getPieceType().toString().charAt(0));
                    sb.append(topPiece.getOwner().getColor().toString().charAt(0));
                    sb.append(stack.size());
                }
            }
            sb.append(";");
        }
        String stateHash = sb.toString();
        return stateHash;
    }

    /**
     * Selects an action based on the current state using ε-greedy strategy.
     *
     * @param board The current game board.
     * @param player The player for whom to choose an action.
     * @param explorationRate The current exploration rate (ε).
     * @return The selected Action object, or null if no actions are available.
     */
    public Action chooseAction(Board board, Player player, double explorationRate) {
        String state = generateStateHashSymmetrically(board, player);
        List<Action> validActions = getAllPossibleActions(board, player);

        if (validActions.isEmpty()) {
            return null;
        }

        qTableLock.lock();
        try {
            qTable.putIfAbsent(state, new HashMap<>());
            Map<Action, Double> actionsMap = qTable.get(state);
            for (Action action : validActions) {
                actionsMap.putIfAbsent(action, 0.0);
            }
        } finally {
            qTableLock.unlock();
        }

        Action selectedAction;

        if (Math.random() < explorationRate) {
            selectedAction = validActions.get(new Random().nextInt(validActions.size()));
        } else {
            double maxQ = Double.NEGATIVE_INFINITY;
            List<Action> bestActions = new ArrayList<>();

            qTableLock.lock();
            try {
                Map<Action, Double> actionsMap = qTable.get(state);
                for (Map.Entry<Action, Double> entry : actionsMap.entrySet()) {
                    double q = entry.getValue();
                    if (q > maxQ) {
                        maxQ = q;
                        bestActions.clear();
                        bestActions.add(entry.getKey());
                    } else if (q == maxQ) {
                        bestActions.add(entry.getKey());
                    }
                }
            } finally {
                qTableLock.unlock();
            }

            if (bestActions.isEmpty()) {
                selectedAction = validActions.get(new Random().nextInt(validActions.size()));
            } else {
                selectedAction = bestActions.get(new Random().nextInt(bestActions.size()));
            }
        }

        episodeHistory.add(new Experience(state, selectedAction, 0.0, null));
        queueSaveTask();

        return selectedAction;
    }

    /**
     * Records an experience consisting of a state and an action.
     *
     * @param state The current state hash.
     * @param action The action taken.
     */
    public void recordExperience(String state, Action action) {
        qTableLock.lock();
        try {
            qTable.putIfAbsent(state, new HashMap<>());
            Map<Action, Double> actionsMap = qTable.get(state);
            actionsMap.putIfAbsent(action, 0.0);
        } finally {
            qTableLock.unlock();
        }

        episodeHistory.add(new Experience(state, action, 0.0, null));
        queueSaveTask();
    }

    /**
     * Parses an action string into an Action object.
     *
     * @param actionStr The action string.
     * @param player The player performing the action.
     * @return The corresponding Action object, or null if parsing fails.
     */
    private Action parseActionString(String actionStr, Player player) {
        try {
            return Action.fromString(actionStr, player.getColor());
        } catch (InvalidMoveException e) {
            return null;
        }
    }

    /**
     * Updates the Q-Table based on the observed transition using Temporal Difference (TD) Learning.
     *
     * @param board The current game board after the action.
     * @param player The player who took the action.
     * @param reward The reward received after taking the action.
     * @param explorationRate The current exploration rate (ε).
     */
    public void updateQTable(Board board, Player player, double reward, double explorationRate) {
        if (episodeHistory.isEmpty()) {
            return;
        }

        Experience lastExperience = episodeHistory.get(episodeHistory.size() - 1);
        String currentState = lastExperience.state;
        Action action = lastExperience.action;
        String newState = generateStateHashSymmetrically(board, player);

        qTableLock.lock();
        try {
            qTable.putIfAbsent(newState, new HashMap<>());
            Map<Action, Double> newStateActions = qTable.get(newState);
            for (Action a : getAllPossibleActions(board, player)) {
                newStateActions.putIfAbsent(a, 0.0);
            }

            double maxFutureQ = newStateActions.values().stream().mapToDouble(v -> v).max().orElse(0.0);
            Map<Action, Double> actionsMap = qTable.get(currentState);
            if (actionsMap == null) {
                actionsMap = new HashMap<>();
                qTable.put(currentState, actionsMap);
            }
            double currentQ = actionsMap.getOrDefault(action, 0.0);
            double updatedQ = currentQ + learningRate * (reward + discountFactor * maxFutureQ - currentQ);
            updatedQ = clamp(updatedQ, -1000.0, 1000.0);

            if (Double.isFinite(updatedQ)) {
                actionsMap.put(action, updatedQ);
                stateUpdateFrequency.put(currentState, stateUpdateFrequency.getOrDefault(currentState, 0) + 1);
            } else {
                actionsMap.put(action, 0.0);
            }
        } finally {
            qTableLock.unlock();
        }

        pruneQTable();
        lastExperience.reward = reward;
        lastExperience.nextState = newState;
        queueSaveTask();
    }

    /**
     * Updates the Q-Table with the final reward after the game ends using Monte Carlo Learning.
     *
     * @param finalReward The final reward from the game outcome.
     */
    public void updateQTableAfterGame(double finalReward) {
        if (episodeHistory.isEmpty()) {
            return;
        }

        double cumulativeReward = finalReward;
        List<Experience> reversedHistory = new ArrayList<>(episodeHistory);
        Collections.reverse(reversedHistory);

        qTableLock.lock();
        try {
            for (Experience exp : reversedHistory) {
                qTable.putIfAbsent(exp.state, new HashMap<>());
                Map<Action, Double> actionsMap = qTable.get(exp.state);
                actionsMap.putIfAbsent(exp.action, 0.0);

                double oldQ = actionsMap.getOrDefault(exp.action, 0.0);
                double newQ = oldQ + learningRate * (cumulativeReward - oldQ);
                newQ = clamp(newQ, -1000.0, 1000.0);

                if (Double.isFinite(newQ)) {
                    actionsMap.put(exp.action, newQ);
                    stateUpdateFrequency.put(exp.state, stateUpdateFrequency.getOrDefault(exp.state, 0) + 1);
                } else {
                    actionsMap.put(exp.action, 0.0);
                }

                cumulativeReward *= discountFactor;
            }
        } finally {
            qTableLock.unlock();
        }

        episodeHistory.clear();
        pruneQTable();
        queueSaveTask();
    }

    /**
     * Clamps a value between a minimum and maximum bound.
     *
     * @param value The value to clamp.
     * @param min The minimum bound.
     * @param max The maximum bound.
     * @return The clamped value.
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Retrieves the value of a given state based on the Q-values.
     *
     * @param stateHash The hash representing the game state.
     * @return The maximum Q-Value for the state, or 0.0 if the state is unknown.
     */
    public double getStateValue(String stateHash) {
        qTableLock.lock();
        try {
            if (qTable.containsKey(stateHash)) {
                Map<Action, Double> actions = qTable.get(stateHash);
                double maxQ = actions.values().stream().mapToDouble(v -> v).max().orElse(0.0);
                return maxQ;
            } else {
                return 0.0;
            }
        } finally {
            qTableLock.unlock();
        }
    }

    /**
     * Saves the Q-Table to a file asynchronously.
     */
    public void saveQTableAsync() {
        queueSaveTask();
    }

    /**
     * Queues a save task if one is not already queued.
     */
    private void queueSaveTask() {
        if (isSaveQueued.compareAndSet(false, true)) {
            saveExecutor.submit(() -> {
                try {
                    saveQTableWithRetries(5, 1000);
                } finally {
                    isSaveQueued.set(false);
                }
            });
        }
    }

    /**
     * Saves the Q-Table to a file with retries and exponential backoff.
     *
     * @param maxRetries Number of maximum retry attempts.
     * @param initialDelayMillis Initial delay before the first retry in milliseconds.
     */
    private void saveQTableWithRetries(int maxRetries, long initialDelayMillis) {
        saveLock.lock();
        try {
            String tempFileName = "qtable_" + UUID.randomUUID().toString() + ".ser.tmp";
            Path tempPath = Paths.get(tempFileName);
            Path finalPath = Paths.get(QTABLE_FILE);
            int attempt = 0;
            long delay = initialDelayMillis;
            boolean success = false;

            while (attempt < maxRetries && !success) {
                try {
                    cleanUpTempFiles();

                    try (GZIPOutputStream gzos = new GZIPOutputStream(Files.newOutputStream(tempPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
                        ObjectOutputStream oos = new ObjectOutputStream(gzos)) {
                        oos.writeObject(qTable);
                        oos.flush();
                        gzos.finish();
                    }

                    Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    success = true;
                } catch (IOException e) {
                    attempt++;
                    try {
                        Files.deleteIfExists(tempPath);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(delay);
                            delay *= 2;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        } finally {
            saveLock.unlock();
        }
    }

    /**
     * Cleans up existing temporary Q-Table files to prevent conflicts.
     */
    private void cleanUpTempFiles() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "qtable_*.ser.tmp")) {
            for (Path entry : stream) {
                try {
                    Files.deleteIfExists(entry);
                } catch (IOException e) {
                    continue;
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * Loads the Q-Table from a file.
     */
    public void loadQTable() {
        File file = new File(QTABLE_FILE);
        if (!file.exists()) {
            qTable = new HashMap<>();
            return;
        }
        if (file.length() == 0) {
            qTable = new HashMap<>();
            return;
        }
        qTableLock.lock();
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(file));
            ObjectInputStream ois = new ObjectInputStream(gzis)) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                qTable = (Map<String, Map<Action, Double>>) obj;
            } else {
                throw new IOException("Invalid Q-Table format.");
            }
        } catch (EOFException e) {
            qTable = new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            qTable = new HashMap<>();
        } finally {
            qTableLock.unlock();
        }
    }

    /**
     * Resets the Q-Table, removing all learned Q-Values.
     */
    public void resetQTable() {
        qTableLock.lock();
        try {
            qTable.clear();
            stateUpdateFrequency.clear();
        } finally {
            qTableLock.unlock();
        }
        synchronized (episodeHistory) {
            episodeHistory.clear();
        }
        saveQTableAsync();
    }

    /**
     * Shuts down the asynchronous executor service gracefully.
     * Ensures that all pending save operations are completed.
     */
    public void shutdown() {
        if (isShuttingDown.compareAndSet(false, true)) {
            saveQTableAsync();
            saveExecutor.shutdown();
            try {
                if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    saveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                saveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Copies the Q-Table to create a duplicate agent.
     *
     * @return A reference to the singleton QLearningAgent instance.
     */
    public QLearningAgent copy() {
        return this;
    }

    /**
     * Retrieves all possible actions from the current board state for the player.
     *
     * @param board The current game board.
     * @param player The player for whom to generate actions.
     * @return A list of valid Action objects.
     */
    private List<Action> getAllPossibleActions(Board board, Player player) {
        List<String> actionStrings = ActionGenerator.generatePossibleActions(board, player, board.getMoveCount());
        List<Action> actions = new ArrayList<>();
        for (String actionStr : actionStrings) {
            Action action = parseActionString(actionStr, player);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    /**
     * Inner class to represent an experience tuple.
     */
    private static class Experience implements Serializable {
        private static final long serialVersionUID = 1L;

        String state;
        Action action;
        double reward;
        String nextState;

        Experience(String state, Action action, double reward, String nextState) {
            this.state = state;
            this.action = action;
            this.reward = reward;
            this.nextState = nextState;
        }
    }

    /**
     * Prunes the Q-Table to control its size.
     *
     * Removes states that haven't been updated beyond the pruning threshold
     * and if the Q-Table exceeds the maximum allowed size.
     */
    private void pruneQTable() {
        qTableLock.lock();
        try {
            if (qTable.size() <= maxQTableSize) {
                return;
            }

            List<Map.Entry<String, Integer>> sortedStates = new ArrayList<>(stateUpdateFrequency.entrySet());
            sortedStates.sort(Comparator.comparingInt(Map.Entry::getValue));

            int statesToPrune = qTable.size() - maxQTableSize;
            int pruned = 0;

            for (Map.Entry<String, Integer> entry : sortedStates) {
                if (pruned >= statesToPrune) {
                    break;
                }
                String state = entry.getKey();
                qTable.remove(state);
                stateUpdateFrequency.remove(state);
                pruned++;
            }
        } finally {
            qTableLock.unlock();
        }
    }
}
