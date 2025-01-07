package com.Tak.AI.neuralnet;

import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.PieceStack;
import com.Tak.Logic.models.Player;

/**
 * Converts a Board state into a neural network input array for evaluation.
 *
 * <p><strong>SRP (Single Responsibility Principle):</strong>
 * Only handles extraction of relevant features from the Tak board
 * into a numeric array of consistent length.
 *
 * <p><strong>Potential Steps to Expand:</strong>
 * <ul>
 *   <li>Track <em>piece types</em> (e.g., -3 = opponent's capstone, +3 = player's capstone, etc.).</li>
 *   <li>Include <em>stack heights</em>, from 0 (empty) up to some max limit.</li>
 *   <li>Compute path potentials or connectivity metrics, if your ANN can handle that complexity.</li>
 *   <li>Normalize features so they lie in a suitable range (e.g., [0, 1] or [-1, 1]).</li>
 * </ul>
 */
public class BoardToInputsConverter {

    /**
     * Converts the board state into a normalized input array.
     *
     * @param board  The current Tak game board.
     * @param player The player from whose perspective we're evaluating.
     * @return A double[] representing the board state.
     *
     * <p><strong>TODO Implementation Outline:</strong>
     * <ol>
     *   <li>Decide on the exact dimension of your input layer (configurable or fixed?).</li>
     *   <li>Loop over each cell of the board and store features in the array (top piece type, stack height, ownership ratio, etc.).</li>
     *   <li>Add any additional global features (turn number, pieces left, etc.).</li>
     *   <li>Ensure consistent indexing, so each <code>(x,y)</code> cell is mapped to a stable index in the array.</li>
     * </ol>
     */
    public static double[] convert(Board board, Player player) {
        //TODO
        int placeholderInputSize = 10;
        double[] inputs = new double[placeholderInputSize];

        // Example trivial fill:
        for (int i = 0; i < placeholderInputSize; i++) {
            inputs[i] = 0.0;
        }

        return inputs;
    }

    /**
     * (Optional) Helper method to compute a specialized feature (e.g. ownership ratio).
     *
     * @param board  The game board.
     * @param player The player in question.
     * @return A computed feature value (example stub).
     *
     * <p><strong>Usage:</strong> Could be called by <code>convert(...)</code> to embed advanced heuristics
     * like "how many of my pieces are on top vs. the opponent's" or "capstone threats," etc.
     */
    private static double computeSomeFeature(Board board, Player player) {
        //TODO Implement feature extraction logic if needed.
        return 0.0;
    }
}
