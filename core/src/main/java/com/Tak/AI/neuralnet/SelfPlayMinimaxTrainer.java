package com.Tak.AI.neuralnet;

import com.Tak.AI.players.MinimaxAgent;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.players.HumanPlayer; // Example import if needed

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates self-play or AI vs. AI training using Minimax + NeuralNetwork.
 *
 * <p><strong>Basic Steps (Potential Outline):</strong>
 * <ol>
 *   <li>Create a TakGame with at least one MinimaxAgent that uses NeuralNetworkEvaluator.</li>
 *   <li>Simulate a full game (AI vs. AI or AI vs. random/human).</li>
 *   <li>Gather (state -> outcome) or (move -> outcome) data at each step or at final result.</li>
 *   <li>Call <code>nnTrainer.train(...)</code> or <code>nnTrainer.trainAll(...)</code> with that data.</li>
 *   <li>Repeat for multiple games to (hopefully) improve the network's evaluation ability.</li>
 * </ol>
 *
 * <p><strong>Caution:</strong> If you want the network to <em>continuously</em> learn as it plays,
 * you may want an approach like temporal difference or Q-learning. This class shows a simplistic
 * "collect data then train" pipeline.
 */
public class SelfPlayMinimaxTrainer {

    private NeuralNetworkTrainer nnTrainer; 
    private int numberOfGames;

    /**
     * Constructs a SelfPlayMinimaxTrainer with specified parameters.
     *
     * @param nnTrainer     The trainer for the neural net (provides backprop calls).
     * @param numberOfGames How many self-play or AI-vs-AI games to run.
     */
    public SelfPlayMinimaxTrainer(NeuralNetworkTrainer nnTrainer, int numberOfGames) {
        this.nnTrainer = nnTrainer;
        this.numberOfGames = numberOfGames;
    }

    /**
     * Runs the self-play or AI-vs-AI games using Minimax to generate training data,
     * then updates the NeuralNetwork accordingly.
     *
     * <p><strong>TODO Implementation Outline:</strong>
     * <ol>
     *   <li>For i in [0..numberOfGames):
     *       <ul>
     *         <li>Build a <code>TakGame</code> with MinimaxAgent (which uses {@link NeuralNetworkEvaluator}).</li>
     *         <li>Play until game over.</li>
     *         <li>Collect states (and possibly moves or final outcomes).</li>
     *         <li>Generate target labels for each state (e.g., +1 for winner's states, -1 for loser's states).</li>
     *         <li>Call <code>nnTrainer.train(...)</code> or <code>trainBatch(...)</code> with those pairs.</li>
     *       </ul>
     *   </li>
     *   <li>Optionally log progress or measure if the AI is improving.</li>
     * </ol>
     */
    public void runTraining() {
        //TODO
    }

    /**
     * Example method that simulates a single game, returning a list of (input, target) pairs.
     * You can decide how to label states or how to compute "targets" for each state.
     */
    private List<TrainingExample> playOneGame() {
        List<TrainingExample> examples = new ArrayList<>();

        TakGame game = createExampleTakGame();

        while (!game.isGameEnded()) {
            try {
                Player current = game.getCurrentPlayer();
                current.makeMove(game);

                //TODO Record board state -> future outcome or immediate reward.
                // e.g., double[] features = BoardToInputsConverter.convert(game.getBoard(), current);
                // examples.add(...) with placeholder for target?

            } catch (InvalidMoveException | GameOverException e) {
                // skip or handle
                break;
            }
        }

        //TODO Decide how to label each recorded board state with a final outcome (+1 if won, -1 if lost, etc.)

        return examples;
    }

    /**
     * Creates a minimal example TakGame with two MinimaxAgents
     * that each use a {@link NeuralNetworkEvaluator} referencing the same neural network.
     */
    private TakGame createExampleTakGame() {
        //TODO Return a new TakGame with your chosen board size and players.
        // e.g., 
        //  TakGame game = new TakGame(5, true, 2);
        // Then override the default AI players with new MinimaxAgents that
        // have a new NeuralNetworkEvaluator(nnTrainer.getNetwork()) as the evaluation function.

        return null; // placeholder
    }

    /**
     * Trains the network on a list of states and targets from one game.
     *
     * @param examples A list of input-target pairs gleaned from the game.
     */
    private void trainFromGameData(List<TrainingExample> examples) {
        // For each example, do:
        for (TrainingExample ex : examples) {
            double[] inputs = ex.getInput();
            double[] targets = ex.getTarget();
            nnTrainer.train(inputs, targets);
        }
    }

    /**
     * A small class to hold (input, target) pairs for training from a single move or state.
     */
    private static class TrainingExample {
        private double[] input;
        private double[] target;

        public TrainingExample(double[] input, double[] target) {
            this.input = input;
            this.target = target;
        }

        public double[] getInput() {
            return input;
        }

        public double[] getTarget() {
            return target;
        }
    }
}
