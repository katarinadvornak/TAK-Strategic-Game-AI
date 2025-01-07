package com.Tak.AI.neuralnet;

/**
 * Handles the backpropagation logic and weight updates for a feedforward network.
 *
 * <p><strong>Single Responsibility (SRP):</strong>
 * This class focuses solely on calculating gradients (errors) and applying
 * them to the weight matrices for a network with one hidden layer.
 *
 * <p><strong>Workflow:</strong>
 * <ol>
 *   <li>Compute <em>output errors</em> (difference between actual and target outputs).</li>
 *   <li>Compute <em>hidden errors</em> (propagate output errors backwards).</li>
 *   <li>Use these errors to update <code>weightsHiddenOutput</code> (hidden-to-output) 
 *       and <code>weightsInputHidden</code> (input-to-hidden).</li>
 *   <li>Leverage an activation derivative function to scale errors properly.</li>
 * </ol>
 *
 * <p>These operations typically require you to have stored:
 * <ul>
 *   <li>The last forward pass's hidden activations</li>
 *   <li>The last forward pass's output activations</li>
 * </ul>
 * so that you can calculate derivatives and partial derivatives w.r.t. weights.
 */
public class BackPropagator {

    /**
     * Conducts the backpropagation and weight update in a single hidden-layer network.
     *
     * @param inputs                The input array used in the forward pass.
     * @param targets               The target output array (ground truth).
     * @param learningRate          The learning rate for weight updates.
     * @param weightsInputHidden    The input-to-hidden weights (to be updated).
     * @param weightsHiddenOutput   The hidden-to-output weights (to be updated).
     * @param hiddenLayerActivations The hidden layer neuron outputs from the forward pass.
     * @param outputActivations     The output layer neuron outputs from the forward pass.
     * @param activationFunction    The activation function used for both hidden and output neurons.
     *
     * <p><strong>TODO Implementation Outline:</strong>
     * <ol>
     *   <li>Compute output layer errors (e.g., (target - actual) * derivative( outputActivations ) ).</li>
     *   <li>Compute hidden layer errors (propagate output error backwards, also multiply by derivative( hiddenActivations ) ).</li>
     *   <li>Update each weight in <code>weightsHiddenOutput</code> based on the hidden->output gradient.</li>
     *   <li>Update each weight in <code>weightsInputHidden</code> based on the input->hidden gradient.</li>
     * </ol>
     * <p>
     * Note: The derivative logic depends on <code>activateDerivative(...)</code> below.
     */
    public static void backpropagate(
            double[] inputs,
            double[] targets,
            double learningRate,
            double[][] weightsInputHidden,
            double[][] weightsHiddenOutput,
            double[] hiddenLayerActivations,
            double[] outputActivations,
            ActivationFunction activationFunction
    ) {
        //TODO
    }

    /**
     * Applies the derivative of the chosen activation function to a single value (needed for backprop).
     *
     * @param activatedValue The neuron's post-activation output (if using SIGMOID or TANH).
     *                       For LINEAR, the input and output are the same, so you can decide how to handle it.
     * @param function       The ActivationFunction used.
     * @return The derivative of the activation function at that value.
     *
     * <p><strong>Example Guidelines:</strong>
     * <ul>
     *   <li>LINEAR: derivative is always <code>1</code>.</li>
     *   <li>SIGMOID: derivative = <code>sigmoid(x) * (1 - sigmoid(x))</code>, 
     *       so if <code>activatedValue</code> is <code>sigmoid(x)</code>, 
     *       derivative = <code>activatedValue * (1 - activatedValue)</code>.</li>
     *   <li>TANH: derivative = <code>1 - (tanh(x))^2</code>. 
     *       If <code>activatedValue</code> = <code>tanh(x)</code>, 
     *       derivative = <code>1.0 - activatedValue * activatedValue</code>.</li>
     * </ul>
     */
    public static double activateDerivative(double activatedValue, ActivationFunction function) {
        //TODO Add any special logic (like if you pass raw sum or the activated sum).
        switch (function) {
            case LINEAR:
                return 1.0;
            case SIGMOID:
                return activatedValue * (1.0 - activatedValue);
            case TANH:
                return 1.0 - (activatedValue * activatedValue);
            default:
                return 1.0; // fallback
        }
    }
}
