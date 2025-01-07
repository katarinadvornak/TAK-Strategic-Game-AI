package com.Tak.AI.neuralnet;

import java.io.Serializable;

/**
 * A "facade" class that ties together configuration, forward, and backprop
 * to present a single interface for training and evaluating the network.
 * 
 * <p><strong>Recommended Usage:</strong>
 * <ul>
 *   <li>Construct this with a {@link NeuralNetworkConfig} indicating layer sizes & activation.</li>
 *   <li>Call {@code forward(...)} to get output predictions from given input vectors.</li>
 *   <li>Call {@code backpropagate(...)} to adjust weights after receiving a target label.</li>
 * </ul>
 *
 * <p>You can store the <em>intermediate</em> hidden/output activations by using
 * {@link #forwardWithIntermediate(double[])} if you want them for subsequent backprop calls.
 */
public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private NeuralNetworkConfig config;
    private double[][] weightsInputHidden;
    private double[][] weightsHiddenOutput;

    /**
     * Constructs a NeuralNetwork with the specified config,
     * and initializes the weights randomly by default.
     *
     * @param config A NeuralNetworkConfig object containing structure & activation details.
     */
    public NeuralNetwork(NeuralNetworkConfig config) {
        this.config = config;
        initializeWeights();
    }

    /**
     * Initializes the weight matrices using WeightInitializer.
     */
    private void initializeWeights() {
        this.weightsInputHidden = WeightInitializer.initializeRandom(
                config.getInputSize(), config.getHiddenSize()
        );
        this.weightsHiddenOutput = WeightInitializer.initializeRandom(
                config.getHiddenSize(), config.getOutputSize()
        );
    }

    /**
     * Performs a forward pass through the neural network (no intermediate details).
     *
     * @param inputs An array containing input values.
     * @return The output array from the network (length = outputSize).
     *
     * <p><strong>Implementation Note:</strong>
     * If you are going to backprop right after this, consider using 
     * {@link #forwardWithIntermediate(double[])} to avoid computing the sums/activations twice.
     */
    public double[] forward(double[] inputs) {
        //TODO Possibly store hiddenActivations if you want to do backprop in same call
        return ForwardPropagator.forward(
                inputs,
                weightsInputHidden,
                weightsHiddenOutput,
                config.getActivationFunction()
        );
    }

    /**
     * Performs backpropagation and updates the network weights.
     *
     * @param inputs       The input array.
     * @param targets      The target output array (desired).
     * @param learningRate The learning rate for weight updates.
     *
     * <p><strong>Implementation Outline (TODO):</strong>
     * <ol>
     *   <li>Obtain hidden & output activations (via {@link #forwardWithIntermediate(double[])}) to avoid recomputation.</li>
     *   <li>Call {@link BackPropagator#backpropagate(...)} with the saved activations.</li>
     *   <li>Use the updated weights afterward for subsequent forward passes.</li>
     * </ol>
     */
    public void backpropagate(double[] inputs, double[] targets, double learningRate) {
        //TODO
    }

    /**
     * Example method returning both hidden and output activations for backprop.
     * The user/trainer can store them, then pass to a specialized backprop method.
     *
     * @param inputs The input array to feed forward.
     * @return An instance of {@link ForwardResults} containing arrays for hidden & output.
     */
    public ForwardResults forwardWithIntermediate(double[] inputs) {
        //TODO Actually compute hidden-layer sums/activations and output-layer sums/activations.
        // Return them in ForwardResults, so backprop can skip re-computing forward pass.
        return new ForwardResults(
            new double[ config.getHiddenSize() ], 
            new double[ config.getOutputSize() ]
        );
    }

    // --------------------- Getters / Setters / Accessors ---------------------

    public NeuralNetworkConfig getConfig() {
        return config;
    }

    public double[][] getWeightsInputHidden() {
        return weightsInputHidden;
    }

    public double[][] getWeightsHiddenOutput() {
        return weightsHiddenOutput;
    }

    public void setWeightsInputHidden(double[][] newWeights) {
        //TODO Optionally validate shape
        this.weightsInputHidden = newWeights;
    }

    public void setWeightsHiddenOutput(double[][] newWeights) {
        //TODO Optionally validate shape
        this.weightsHiddenOutput = newWeights;
    }

    /**
     * Holds hidden + output activations from a forward pass,
     * so you can pass them directly to a backprop method.
     */
    public static class ForwardResults {
        private double[] hiddenActivations;
        private double[] outputActivations;

        public ForwardResults(double[] hiddenActivations, double[] outputActivations) {
            this.hiddenActivations = hiddenActivations;
            this.outputActivations = outputActivations;
        }

        public double[] getHiddenActivations() { return hiddenActivations; }
        public double[] getOutputActivations() { return outputActivations; }
    }
}
