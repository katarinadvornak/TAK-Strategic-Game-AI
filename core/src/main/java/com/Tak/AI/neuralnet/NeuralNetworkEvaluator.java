package com.Tak.AI.neuralnet;

import com.Tak.AI.evaluation.IEvaluationFunction;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;

import java.io.Serializable;

/**
 * An evaluator that uses a NeuralNetwork to score the board state.
 * It implements IEvaluationFunction, so it can be plugged into Minimax or other search algorithms.
 * 
 * <p><strong>SRP:</strong> This class only bridges the Tak board with the neural net's forward pass.
 */
public class NeuralNetworkEvaluator implements IEvaluationFunction, Serializable {
    private static final long serialVersionUID = 1L;

    private NeuralNetwork network;

    /**
     * Constructs a NeuralNetworkEvaluator with a given neural network.
     *
     * @param network The NeuralNetwork instance to use for evaluation.
     */
    public NeuralNetworkEvaluator(NeuralNetwork network) {
        this.network = network;
    }

    /**
     * Evaluates the given board state for the specified player using the neural network.
     *
     * @param board  The current game board.
     * @param player The player for whom the evaluation is being performed.
     * @return A numerical score from the perspective of 'player'.
     *
     * <p><strong>Implementation Approach (TODO):</strong>
     * <ol>
     *   <li>Call {@link BoardToInputsConverter#convert(Board, Player)} to get the input array.</li>
     *   <li>Call {@link NeuralNetwork#forward(double[])} with those inputs.</li>
     *   <li>Interpret the output (if multiple neurons, decide how to combine them).
     *       E.g., if <code>outputSize == 1</code>, just return <code>outputs[0]</code>.</li>
     *   <li>Ensure the sign or magnitude matches your intended scale 
     *       (e.g., a bigger positive value means a better board for that player).</li>
     * </ol>
     */
    @Override
    public double evaluate(Board board, Player player) {
        double[] inputs = BoardToInputsConverter.convert(board, player);
        double[] outputs = network.forward(inputs);
        //TODO If outputSize > 1, consider using outputs[0] - outputs[1], or other logic
        return (outputs.length > 0) ? outputs[0] : 0.0;
    }

    /**
     * (Optional) For IEvaluationFunction usage:
     * Determine how deep the AI search should go based on board size or complexity.
     *
     * @param size The size of the board (e.g., 5 for a 5x5).
     * @return The recommended search depth for Minimax or other search algorithms.
     */
    public int depthFromBoardSize(int size) {
        //TODO Possibly base it on size (larger board => smaller depth or some heuristic).
        return 3; // placeholder
    }
}
