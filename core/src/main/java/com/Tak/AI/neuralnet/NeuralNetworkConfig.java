package com.Tak.AI.neuralnet;

import java.io.Serializable;

/**
 * Holds configuration details for constructing and running the neural network.
 * 
 * <p><strong>SRP:</strong> This class only stores configuration properties
 * (layer sizes, activation function, etc.) without logic to manipulate them.
 *
 * <p><strong>Possible Future Extensions:</strong>
 * <ul>
 *   <li>Default learning rates or momentum values.</li>
 *   <li>Additional hidden layers: a list/array of layer sizes instead of a single hidden layer size.</li>
 *   <li>Advanced training hyperparameters (weight decay, dropout rates, etc.).</li>
 * </ul>
 */
public class NeuralNetworkConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;
    private final ActivationFunction activationFunction;

    /**
     * Constructs a NeuralNetworkConfig with the required parameters.
     *
     * @param inputSize         The number of input neurons.
     * @param hiddenSize        The number of hidden-layer neurons.
     * @param outputSize        The number of output neurons.
     * @param activationFunction The activation function to use (LINEAR, SIGMOID, TANH).
     */
    public NeuralNetworkConfig(int inputSize, int hiddenSize, int outputSize, ActivationFunction activationFunction) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.activationFunction = activationFunction;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getHiddenSize() {
        return hiddenSize;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public ActivationFunction getActivationFunction() {
        return activationFunction;
    }
}
