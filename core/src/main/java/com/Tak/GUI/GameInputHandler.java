// File: core/src/main/java/com/Tak/GUI/GameInputHandler.java
package com.Tak.GUI;

import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.TakGame;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.Gdx;
import com.Tak.Logic.models.Board;
import java.util.List;


/**
 * The GameInputHandler class handles user input for placing and moving pieces on the board.
 */
public class GameInputHandler extends InputAdapter {
    private PerspectiveCamera camera;
    private TakGame takGame;
    private GameRenderer renderer;
    private UIManager uiManager;
    private UICallback uiCallback; // Interface for callbacks to UIManager or GameScreen

    private enum SelectionState {
        SELECTING_SOURCE,
        SELECTING_DIRECTION
    }

    private SelectionState selectionState = SelectionState.SELECTING_SOURCE;
    private int sourceX, sourceY;

    /**
     * Interface to handle UI callbacks, promoting loose coupling.
     */
    public interface UICallback {
        void showErrorDialog(String message);
        void showGameOverDialog(Player winner);
        void addMoveToList(String moveDescription);
        void updatePlayerScores();
        void updateCurrentPlayerLabel();
        void updateHotbarColors();
        void showDropCountsPrompt(int sourceX, int sourceY, Direction direction, UIManager.DropCountsCallback callback);
        Piece.PieceType getSelectedPieceType(); // Added method
        void deselectPiece(); // Added method
        boolean isPieceSelected(); // Added method
    }

    /**
     * Constructor to initialize the GameInputHandler.
     *
     * @param camera      The PerspectiveCamera used for ray picking.
     * @param takGame     The current TakGame instance.
     * @param renderer    The GameRenderer instance.
     * @param uiManager   The UIManager instance.
     * @param uiCallback  The UICallback implementation for UI interactions.
     */
    public GameInputHandler(PerspectiveCamera camera, TakGame takGame, GameRenderer renderer, UIManager uiManager, UICallback uiCallback) {
        this.camera = camera;
        this.takGame = takGame;
        this.renderer = renderer;
        this.uiManager = uiManager;
        this.uiCallback = uiCallback;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) return false;

        // Convert screen coordinates to world coordinates
        Ray pickRay = camera.getPickRay(screenX, screenY);
        Vector3 intersection = new Vector3();

        // Define the plane of the board (Y-up)
        Plane boardPlane = new Plane(new Vector3(0, 1, 0), new Vector3(0, 0, 0));

        if (Intersector.intersectRayPlane(pickRay, boardPlane, intersection)) {
            // Adjust for board's position
            Vector3 boardPosition = new Vector3();
            renderer.getBoardInstance().transform.getTranslation(boardPosition);

            float localX = intersection.x - (boardPosition.x - (takGame.getBoardSize() * renderer.getTileSize()) / 2f);
            float localY = intersection.z - (boardPosition.z - (takGame.getBoardSize() * renderer.getTileSize()) / 2f);

            int x = (int) Math.floor(localX);
            int y = (int) Math.floor(localY);

            Gdx.app.log("GameInputHandler", "Clicked position: (" + x + ", " + y + ")");

            if (x >= 0 && x < takGame.getBoardSize() && y >= 0 && y < takGame.getBoardSize()) {
                if (uiCallback.isPieceSelected()) {
                    // Handle placing a new piece
                    try {
                        Piece.PieceType selectedPiece = uiCallback.getSelectedPieceType();
                        try {
                            takGame.placePiece(x, y, selectedPiece);
                        } catch (GameOverException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        // Update the rendering
                        renderer.updatePieceInstances();

                        // Update the moves list
                        String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " placed " + selectedPiece + " at (" + x + ", " + y + ")";
                        uiCallback.addMoveToList(moveDescription);

                        // Update player scores
                        uiCallback.updatePlayerScores();

                        // Deselect the piece
                        uiCallback.deselectPiece();
                        renderer.removeHoverOutline();

                        // Update current player label
                        uiCallback.updateCurrentPlayerLabel();
                        uiCallback.updateHotbarColors();

                        // Check for game end
                        if (takGame.isGameEnded()) {
                            uiCallback.showGameOverDialog(takGame.getWinner());
                        }
                    } catch (InvalidMoveException e) {
                        // Handle invalid move
                        uiCallback.showErrorDialog("Invalid Move: " + e.getMessage());
                    }
                } else {
                    // Handle moving stacks
                    if (selectionState == SelectionState.SELECTING_SOURCE) {
                        // Check if there's a stack and the top piece belongs to the current player
                        List<Piece> stack = takGame.getBoard().getBoardPosition(x, y);
                        if (!stack.isEmpty() && stack.get(stack.size() - 1).getOwner() == takGame.getCurrentPlayer()) {
                            sourceX = x;
                            sourceY = y;
                            selectionState = SelectionState.SELECTING_DIRECTION;
                            renderer.highlightSquare(x, y);
                        } else {
                            uiCallback.showErrorDialog("Select a stack where your piece is on top.");
                        }
                    } else if (selectionState == SelectionState.SELECTING_DIRECTION) {
                        // Calculate the difference between source and destination
                        int dx = x - sourceX;
                        int dy = y - sourceY;
                    
                        final Direction direction; // Declare as final
                        if (dx == 0 && dy > 0) {
                            direction = Direction.UP;
                        } else if (dx == 0 && dy < 0) {
                            direction = Direction.DOWN;
                        } else if (dy == 0 && dx > 0) {
                            direction = Direction.RIGHT;
                        } else if (dy == 0 && dx < 0) {
                            direction = Direction.LEFT;
                        } else {
                            uiCallback.showErrorDialog("Invalid direction. Moves must be in a straight line.");
                            return true;
                        }
                    
                        // Check stack size and decide whether to prompt or move directly
                        List<Piece> stackPieces = takGame.getBoard().getBoardPosition(sourceX, sourceY);
                        if (stackPieces.size() == 1) {
                            // Move directly without prompting
                            try {
                                int[] dropCounts = {1};
                                takGame.moveStack(sourceX, sourceY, direction, dropCounts);
                    
                                // Update the rendering
                                renderer.updatePieceInstances();
                    
                                // Update the moves list
                                String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " moved 1 piece from (" + sourceX + ", " + sourceY + ") to direction " + direction;
                                uiCallback.addMoveToList(moveDescription);
                    
                                // Update UI elements
                                uiCallback.updateCurrentPlayerLabel();
                                uiCallback.updateHotbarColors();
                    
                                // Check for game end
                                if (takGame.isGameEnded()) {
                                    uiCallback.showGameOverDialog(takGame.getWinner());
                                }
                            } catch (InvalidMoveException e) {
                                // Handle invalid move
                                uiCallback.showErrorDialog("Invalid Move: " + e.getMessage());
                            } catch (GameOverException e) {
                                // Handle game over
                                uiCallback.showGameOverDialog(takGame.getWinner());
                            }
                    
                            // Reset selection state
                            selectionState = SelectionState.SELECTING_SOURCE;
                            renderer.removeHighlight();
                        } else {
                            // Prompt for drop counts via UIManager
                            uiCallback.showDropCountsPrompt(sourceX, sourceY, direction, (dropCounts) -> {
                                try {
                                    takGame.moveStack(sourceX, sourceY, direction, dropCounts);
                    
                                    // Update the rendering
                                    renderer.updatePieceInstances();
                    
                                    // Update the moves list
                                    String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " moved stack from (" + sourceX + ", " + sourceY + ") in direction " + direction;
                                    uiCallback.addMoveToList(moveDescription);
                    
                                    // Update UI elements
                                    uiCallback.updateCurrentPlayerLabel();
                                    uiCallback.updateHotbarColors();
                    
                                    // Check for game end
                                    if (takGame.isGameEnded()) {
                                        uiCallback.showGameOverDialog(takGame.getWinner());
                                    }
                                } catch (InvalidMoveException e) {
                                    // Handle invalid move
                                    uiCallback.showErrorDialog("Invalid Move: " + e.getMessage());
                                } catch (GameOverException e) {
                                    // Handle game over
                                    uiCallback.showGameOverDialog(takGame.getWinner());
                                } finally {
                                    // Reset selection state
                                    selectionState = SelectionState.SELECTING_SOURCE;
                                    renderer.removeHighlight();
                                }
                            });
                        }
                    }
                }
            } else {
                Gdx.app.log("GameInputHandler", "Clicked out of bounds: (" + x + ", " + y + ")");
            }
        } else {
            Gdx.app.log("GameInputHandler", "No intersection with board plane.");
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!uiCallback.isPieceSelected()) {
            renderer.removeHoverOutline();
            return false;
        }

        // Convert screen coordinates to world coordinates
        Ray pickRay = camera.getPickRay(screenX, screenY);
        Vector3 intersection = new Vector3();

        // Define the plane of the board
        Plane boardPlane = new Plane(new Vector3(0, 1, 0), new Vector3(0, 0, 0));

        if (Intersector.intersectRayPlane(pickRay, boardPlane, intersection)) {
            // Adjust for board's position
            Vector3 boardPosition = new Vector3();
            renderer.getBoardInstance().transform.getTranslation(boardPosition);

            float localX = intersection.x - (boardPosition.x - (takGame.getBoardSize() * renderer.getTileSize()) / 2f);
            float localY = intersection.z - (boardPosition.z - (takGame.getBoardSize() * renderer.getTileSize()) / 2f);

            int x = (int) Math.floor(localX);
            int y = (int) Math.floor(localY);

            Gdx.app.log("GameInputHandler", "Hovering over: (" + x + ", " + y + ")");

            if (x >= 0 && x < takGame.getBoardSize() && y >= 0 && y < takGame.getBoardSize()) {
                // Remove existing hover outline before creating a new one
                renderer.removeHoverOutline();

                // Create the hover outline
                renderer.createHoverOutline(x, y);
                Gdx.app.log("GameInputHandler", "Created hover outline at: (" + x + ", " + y + ")");
            } else {
                renderer.removeHoverOutline();
            }
        }

        return false;
    }
}
