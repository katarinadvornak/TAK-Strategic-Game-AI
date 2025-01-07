package com.Tak.AI.neuralnet;

/**
 * Responsible for initializing the weights of the neural network.
 * 
 * <p><strong>SRP:</strong> This class only deals with generating initial weight values
 * for the input-to-hidden and hidden-to-output layers (or further layers if extended).
 *
 * <p><strong>Common Enhancements:</strong>
 * <ul>
 *   <li>Xavier/Glorot initialization for SIGMOID/TANH networks.</li>
 *   <li>Kaiming/He initialization if you add ReLU-like functions.</li>
 *   <li>Optionally an interface like <code>WeightInitializationStrategy</code>
 *       to handle multiple strategies seamlessly.</li>
 * </ul>
 */
public class WeightInitializer {

    /**
     * Initializes a weight matrix with random values in range (-0.5, 0.5).
     *
     * @param rows The number of rows (e.g., input neurons).
     * @param cols The number of columns (e.g., hidden or output neurons).
     * @return A 2D array of shape [rows][cols] with random values.
     *
     * <p><strong>TODO Variation:</strong> 
     * <ul>
     *   <li>Use <em>Xavier initialization</em> if needed: random range ~ <code>sqrt(6.0 / (fanIn + fanOut))</code>.</li>
     *   <li>Use <em>Zero init</em> for debugging (though can lead to symmetry problems in training).</li>
     * </ul>
     */
    public static double[][] initializeRandom(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                //TODO Possibly do something other than (Math.random() - 0.5)
                matrix[i][j] = Math.random() - 0.5;
            }
        }
        return matrix;
    }
}
