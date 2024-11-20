package com.Tak.AI.experiments;

import com.Tak.AI.actions.Action;
import com.Tak.Logic.models.*;

import java.util.List;

public class Results {
    public static class GameResult {
        public final int depth;
        public final double executionTime;
        public final double positionScore;
        public final int nodesExplored;

        public GameResult(int depth, double executionTime, double positionScore, int nodesExplored) {
            this.depth = depth;
            this.executionTime = executionTime;
            this.positionScore = positionScore;
            this.nodesExplored = nodesExplored;
        }
    }

    public static class DepthExperimentResult {
        public final int depth;
        public final List<GameResult> results;

        public DepthExperimentResult(int depth, List<GameResult> results) {
            this.depth = depth;
            this.results = results;
        }
    }

    public static class OpeningExperimentResult {
        public final Board position;
        public final Action bestMove;
        public final double executionTime;
        public final double positionScore;

        public OpeningExperimentResult(Board position, Action bestMove,
                                       double executionTime, double positionScore) {
            this.position = position;
            this.bestMove = bestMove;
            this.executionTime = executionTime;
            this.positionScore = positionScore;
        }
    }

    public static class TimeExperimentResult {
        public final long timeConstraint;
        public final List<GameResult> results;

        public TimeExperimentResult(long timeConstraint, List<GameResult> results) {
            this.timeConstraint = timeConstraint;
            this.results = results;
        }
    }

    public static class BranchingExperimentResult {
        private final int branchingFactor;
        private final double executionTime;
        private final Board position;
        private final Action bestMove;

        public BranchingExperimentResult(int branchingFactor, double executionTime,
                                         Board position, Action bestMove) {
            this.branchingFactor = branchingFactor;
            this.executionTime = executionTime;
            this.position = position;
            this.bestMove = bestMove;
        }
    }
}

