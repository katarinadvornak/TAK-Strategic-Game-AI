package com.Tak.AI.experiments;

import com.Tak.AI.evaluation.EvaluationFunction;
import com.Tak.AI.players.MinimaxAgent;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.Player.Color;

import java.util.List;

public class Main {
    private final Player GREENPlayer;
    private final Player BLUEPlayer;

    public Main() {
        this.GREENPlayer = new MinimaxAgent(Color.GREEN, 21, 1, 1, 3); // Example piece counts
        this.BLUEPlayer = new MinimaxAgent(Color.BLUE, 21, 1, 1, 3);
    }

    public static void main(String[] args) {
        // Initialize the board, players, and evaluation function.
        Board board = new Board(5, 3); // Example 5x5 board.
        Main mainInstance = new Main();
        Player GREENPlayer = mainInstance.GREENPlayer;
        Player BLUEPlayer = mainInstance.BLUEPlayer;

        EvaluationFunction evaluationFunction = new EvaluationFunction();

        // Create and run the depth experiment.
        DepthExperiment experiment = new DepthExperiment(board, evaluationFunction, GREENPlayer, BLUEPlayer, 5, 0);
        List<DepthExperiment.DepthExperimentResult> results = experiment.runExperiment();

        // Print the results or pass them to a graphing library.
        System.out.println("Depth Experiment Results:");
        for (DepthExperiment.DepthExperimentResult result : results) {
            System.out.println(result);
        }

        // Use the results to create graphs, e.g., time taken vs depth.
    }
}
