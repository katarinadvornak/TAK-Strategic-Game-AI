package com.Tak.GUI;

import com.Tak.Logic.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import java.util.List;

/**
 * The GameInputHandler class handles user input for placing and moving pieces on the board.
 */
public class GameInputHandler extends InputAdapter {
    private PerspectiveCamera camera;
    private TakGame takGame;
    private GameRenderer renderer;
    private UIManager uiManager;
    private GameScreen gameScreen;

    private enum SelectionState {
        SELECTING_SOURCE,
        SELECTING_DIRECTION
    }

    private SelectionState selectionState = SelectionState.SELECTING_SOURCE;
    private int sourceX, sourceY;

    public GameInputHandler(PerspectiveCamera camera, TakGame takGame, GameRenderer renderer, UIManager uiManager, GameScreen gameScreen) {
        this.camera = camera;
        this.takGame = takGame;
        this.renderer = renderer;
        this.uiManager = uiManager;
        this.gameScreen = gameScreen;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) return false;

        // Convert screen coordinates to world coordinates
        Ray pickRay = camera.getPickRay(screenX, screenY);
        Vector3 intersection = new Vector3();

        // Define the plane of the board
        Plane boardPlane = new Plane(new Vector3(0, 1, 0), new Vector3(0, 0, 0));

        if (Intersector.intersectRayPlane(pickRay, boardPlane, intersection)) {
            // Adjust for board's position
            Vector3 boardPosition = new Vector3();
            renderer.boardInstance.transform.getTranslation(boardPosition);

            float localX = intersection.x - (boardPosition.x - gameScreen.boardSize / 2f);
            float localY = intersection.z - (boardPosition.z - gameScreen.boardSize / 2f);

            int x = (int) Math.floor(localX);
            int y = (int) Math.floor(localY);

            Gdx.app.log("GameInputHandler", "Clicked position: (" + x + ", " + y + ")");

            if (x >= 0 && x < gameScreen.boardSize && y >= 0 && y < gameScreen.boardSize) {
                if (gameScreen.selectedPieceType != null) {
                    // Handle placing a new piece
                    try {
                        // Place the selected piece
                        takGame.placePiece(x, y, gameScreen.selectedPieceType);

                        // Update the rendering
                        renderer.updatePieceInstances();

                        // Update the moves list
                        String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " placed " + gameScreen.selectedPieceType + " at (" + x + ", " + y + ")";
                        uiManager.addMoveToList(moveDescription);

                        // Update player scores
                        uiManager.updatePlayerScores();

                        // Deselect the piece
                        gameScreen.selectedPieceType = null;
                        renderer.removeHoverOutline();

                        // Update current player label
                        uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
                        uiManager.updateHotbarColors();

                        // Check for game end
                        if (takGame.isGameEnded()) {
                            gameScreen.showGameOverDialog(takGame.getWinner());
                        }
                    } catch (InvalidMoveException e) {
                        // Handle invalid move
                        gameScreen.showErrorDialog("Invalid Move: " + e.getMessage());
                    } catch (GameOverException e) {
                        // Handle game over
                        gameScreen.showGameOverDialog(takGame.getWinner());
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
                            gameScreen.showErrorDialog("Select a stack where your piece is on top.");
                        }
                    } else if (selectionState == SelectionState.SELECTING_DIRECTION) {
                        // Calculate the difference between source and destination
                        int dx = x - sourceX;
                        int dy = y - sourceY;

                        Direction direction = null;
                        if (dx == 0 && dy > 0) {
                            direction = Direction.UP;
                        } else if (dx == 0 && dy < 0) {
                            direction = Direction.DOWN;
                        } else if (dy == 0 && dx > 0) {
                            direction = Direction.RIGHT;
                        } else if (dy == 0 && dx < 0) {
                            direction = Direction.LEFT;
                        } else {
                            gameScreen.showErrorDialog("Invalid direction. Moves must be in a straight line.");
                            return true;
                        }

                        // **New Part: Check stack size and decide whether to prompt or move directly**
                        List<Piece> stack = takGame.getBoard().getBoardPosition(sourceX, sourceY);
                        if (stack.size() == 1) {
                            // Move directly without prompting
                            try {
                                int[] dropCounts = {1};
                                takGame.moveStack(sourceX, sourceY, direction, dropCounts);

                                // Update the rendering
                                renderer.updatePieceInstances();

                                // Update the moves list
                                String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " moved 1 piece from (" + sourceX + ", " + sourceY + ") to direction " + direction;
                                uiManager.addMoveToList(moveDescription);

                                // Update UI elements
                                uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
                                uiManager.updateHotbarColors();

                                // Check for game end
                                if (takGame.isGameEnded()) {
                                    gameScreen.showGameOverDialog(takGame.getWinner());
                                }
                            } catch (InvalidMoveException e) {
                                // Handle invalid move
                                gameScreen.showErrorDialog("Invalid Move: " + e.getMessage());
                            } catch (GameOverException e) {
                                // Handle game over
                                gameScreen.showGameOverDialog(takGame.getWinner());
                            }

                            // Reset selection state
                            selectionState = SelectionState.SELECTING_SOURCE;
                            renderer.removeHighlight();
                        } else {
                            // Prompt for drop counts
                            promptForDropCounts(sourceX, sourceY, direction);
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
        if (gameScreen.selectedPieceType == null) {
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
            renderer.boardInstance.transform.getTranslation(boardPosition);

            float localX = intersection.x - (boardPosition.x - gameScreen.boardSize / 2f);
            float localY = intersection.z - (boardPosition.z - gameScreen.boardSize / 2f);

            int x = (int) Math.floor(localX);
            int y = (int) Math.floor(localY);

            Gdx.app.log("GameInputHandler", "Hovering over: (" + x + ", " + y + ")");

            if (x >= 0 && x < gameScreen.boardSize && y >= 0 && y < gameScreen.boardSize) {
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

    /**
     * Prompts the user to enter drop counts for moving a stack of pieces.
     * Allows the user to leave the input empty to move a single piece by default.
     *
     * @param sourceX   The x-coordinate of the source position.
     * @param sourceY   The y-coordinate of the source position.
     * @param direction The direction in which to move the stack.
     */
    private void promptForDropCounts(int sourceX, int sourceY, Direction direction) {
        TextField dropCountsField = new TextField("", uiManager.getSkin());
        Dialog dialog = new Dialog("Enter Drop Counts", uiManager.getSkin()) {
            @Override
            protected void result(Object object) {
                if (object.equals("ok")) {
                    String input = dropCountsField.getText().trim();
                    try {
                        int[] dropCounts;

                        if (input.isEmpty()) {
                            // **Handle Empty Input: Default to moving one piece**
                            dropCounts = new int[]{1};
                            Gdx.app.log("GameInputHandler", "Empty input detected. Defaulting to move 1 piece.");
                        } else {
                            // **Handle Multiple Piece Moves**
                            String[] tokens = input.split(",");
                            dropCounts = new int[tokens.length];
                            for (int i = 0; i < tokens.length; i++) {
                                if (tokens[i].trim().isEmpty()) {
                                    throw new NumberFormatException("Empty move count detected between commas.");
                                }
                                dropCounts[i] = Integer.parseInt(tokens[i].trim());
                                if (dropCounts[i] <= 0) {
                                    throw new NumberFormatException("Move counts must be positive integers.");
                                }
                            }
                            Gdx.app.log("GameInputHandler", "Parsed drop counts: " + java.util.Arrays.toString(dropCounts));
                        }

                        // Attempt to move the stack with the parsed drop counts
                        takGame.moveStack(sourceX, sourceY, direction, dropCounts);

                        // Update the rendering
                        renderer.updatePieceInstances();

                        // Update the moves list
                        String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " moved stack from (" + sourceX + ", " + sourceY + ") in direction " + direction;
                        uiManager.addMoveToList(moveDescription);

                        // Update UI elements
                        uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
                        uiManager.updateHotbarColors();

                        // Check for game end
                        if (takGame.isGameEnded()) {
                            gameScreen.showGameOverDialog(takGame.getWinner());
                        }
                    } catch (InvalidMoveException e) {
                        // Handle invalid move
                        gameScreen.showErrorDialog("Invalid Move: " + e.getMessage());
                    } catch (GameOverException e) {
                        // Handle game over
                        gameScreen.showGameOverDialog(takGame.getWinner());
                    } catch (NumberFormatException e) {
                        // Handle invalid number format
                        gameScreen.showErrorDialog("Invalid input for drop counts. Please enter positive integers separated by commas.");
                    } catch (Exception e) {
                        // Handle any unexpected exceptions
                        gameScreen.showErrorDialog("An unexpected error occurred: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        // Reset selection state
                        selectionState = SelectionState.SELECTING_SOURCE;
                        renderer.removeHighlight();
                    }
                } else {
                    // Reset selection state if the dialog is canceled
                    selectionState = SelectionState.SELECTING_SOURCE;
                    renderer.removeHighlight();
                }
            }
        };
        dialog.getContentTable().add(new Label("Enter drop counts separated by commas (leave blank to move 1 piece):", uiManager.getSkin()));
        dialog.getContentTable().row();
        dialog.getContentTable().add(dropCountsField).width(200);
        dialog.button("OK", "ok");
        dialog.button("Cancel", "cancel");
        dialog.show(uiManager.getStage());
    }

}
