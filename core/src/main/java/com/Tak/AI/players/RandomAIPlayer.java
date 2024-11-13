package com.Tak.AI.players;

import com.Tak.AI.actions.Action;
import com.Tak.AI.utils.ActionGenerator;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Board;
import com.Tak.Logic.models.Piece.PieceType;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.utils.Logger;

import java.util.List;
import java.util.Random;

/**
 * RandomAIPlayer selects moves randomly and serves as a baseline agent.
 */
public class RandomAIPlayer extends Player {

    private Random random;

    public RandomAIPlayer(Color color, int flatStones, int standingStones, int capstones) {
        super(color, flatStones, standingStones, capstones);
        this.random = new Random();
    }

    @Override
    public void makeMove(TakGame game)
            throws InvalidMoveException, GameOverException {
        Board board = game.getBoard();
        List<String> actions = ActionGenerator.generatePossibleActions(
                board, this, game.getMoveCount());

        if (!actions.isEmpty()) {
            String actionStr = actions.get(
                    random.nextInt(actions.size()));
            Action action = Action.fromString(actionStr, this.getColor());
            action.execute(board);
            game.incrementMoveCount();
            game.checkWinConditions();
            Logger.debug("RandomAIPlayer", this.getColor() + " executed action: " + actionStr);
        } else {
            throw new InvalidMoveException("No valid moves available.");
        }
    }

    @Override
    public Player copy() {
        return new RandomAIPlayer(this.getColor(),
                this.getRemainingPieces(PieceType.FLAT_STONE),
                this.getRemainingPieces(PieceType.STANDING_STONE),
                this.getRemainingPieces(PieceType.CAPSTONE));
    }

    @Override
    public int getTotalPiecesLeft() {
        int totalPieces = 0;
        for (PieceType pieceType : PieceType.values()) {
            totalPieces += getRemainingPieces(pieceType);
        }
        return totalPieces;
    }
}
