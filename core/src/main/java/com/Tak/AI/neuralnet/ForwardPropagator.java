package com.Tak.AI.neuralnet;

/**
 * Handles the forward pass logic for a feedforward neural network.
 * 
 * <p><strong>SRP:</strong> Only focuses on computing hidden layer activations and
 * output layer activations from given inputs & weights, for a single-hidden-layer design.
 *
 * <p><strong>Detailed Steps (TODO in code):</strong>
 * <ol>
 *   <li>Multiply <code>inputs</code> by <code>weightsInputHidden</code> to get hidden sums.</li>
 *   <li>Apply the <code>applyActivation(...)</code> to each hidden sum, producing hidden layer activations.</li>
 *   <li>Multiply hidden activations by <code>weightsHiddenOutput</code> to get output sums.</li>
 *   <li>Apply activation again to get <em>final</em> output activations. Return that array.</li>
 * </ol>
 */
public class ForwardPropagator {

    /**
     * Performs a forward pass on a single-layer hidden network.
     *
     * @param inputs            The input array.
     * @param weightsInputHidden  The input-to-hidden weight matrix.
     * @param weightsHiddenOutput The hidden-to-output weight matrix.
     * @param activationFunction  The activation function to apply for hidden and output neurons.
     * @return The output array from the network (size = number of output neurons).
     */
    public static double[] forward(double[] inputs,
                                   double[][] weightsInputHidden,
                                   double[][] weightsHiddenOutput,
                                   ActivationFunction activationFunction) {
        //TODO
        // 1) hidden layer weighted sum & activation
        // 2) output layer weighted sum & activation
        return new double[weightsHiddenOutput[0].length];
    }

    /**
     * Applies the chosen activation function to a single value.
     *
     * @param value     The input sum to activate (z).
     * @param function  The chosen ActivationFunction (SIGMOID, TANH, or LINEAR).
     * @return The activated value f(z).
     *
     * <p><strong>Examples:</strong>
     * <ul>
     *   <li><code>SIGMOID: 1 / (1 + exp(-z))</code></li>
     *   <li><code>TANH: Math.tanh(z)</code></li>
     *   <li><code>LINEAR: z</code></li>
     * </ul>
     */
    public static double applyActivation(double value, ActivationFunction function) {
        //TODO
        switch (function) {
            case LINEAR:
                return value;
            case SIGMOID:
                return 1.0 / (1.0 + Math.exp(-value));
            case TANH:
                return Math.tanh(value);
            default:
                return value;
        }
    }
}
