// File: core/src/main/java/com/Tak/GUI/GameScreen.java
package com.Tak.GUI;

import com.Tak.AI.*;
import com.Tak.Logic.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * The GameScreen class handles the main game screen, coordinating between the renderer, UI, and input handler.
 */
public class GameScreen implements Screen {
    public TakGameMain game;
    public int boardSize = 5;
    public TakGame takGame;

    public PerspectiveCamera camera;
    public CameraInputController camController;
    private GameRenderer renderer;
    private UIManager uiManager;
    private GameInputHandler inputHandler;

    public Piece.PieceType selectedPieceType;
    private boolean isAIMoving = false;
    private TextButton rulesButton;
    private ShapeRenderer shapeRenderer;
    public int moveCount = 0;
    private boolean useAI;

    // Remove AIPlayer instance field if it's redundant
    // private AIPlayer aiPlayer;

    public GameScreen(TakGameMain game, boolean useAI) {
        this.game = game;
        this.useAI = useAI;
        create();
    }

    public void create() {
        // Initialize the game logic
        int aiPlayersCount = useAI ? 1 : 0;
        takGame = new TakGame(boardSize, useAI, aiPlayersCount);


        // Set up the camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(boardSize * 2f, boardSize * 2f, boardSize * 2f); // Position the camera
        camera.lookAt(boardSize / 2f, 0, boardSize / 2f); // Look at the center of the board
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        // Initialize the camera controller
        camController = new CameraInputController(camera);
        camController.rotateButton = Input.Buttons.RIGHT; // Use right mouse button for rotation
        camController.translateButton = Input.Buttons.MIDDLE; // For panning
        camController.scrollFactor = -0.1f; // Zoom in/out with mouse wheel

        // Initialize Renderer
        renderer = new GameRenderer(camera, boardSize, takGame);
        renderer.updatePieceInstances();

        // Initialize UI Manager
        uiManager = new UIManager(takGame, this);
        uiManager.getStage().setViewport(new ScreenViewport());

        // Initialize Input Handler
        inputHandler = new GameInputHandler(camera, takGame, renderer, uiManager, this);

        // **Remove the redundant AIPlayer initialization and addition**
        /*
        // Initialize AIPlayer if AI is enabled
        if (useAI) {
            aiPlayer = new AIPlayer(Player.Color.WHITE, 15, 6, 1, 3); // Example parameters
            takGame.addPlayer(aiPlayer);
        }
        */

        // Set input processors
        Gdx.input.setInputProcessor(new InputMultiplexer(uiManager.getStage(), inputHandler, camController));

        addRulesButton();        

        // After setting up UI elements
        uiManager.updateHotbarColors();

        // Initialize ShapeRenderer for the selection arrow
        shapeRenderer = new ShapeRenderer();
    }
    private void addRulesButton() {
        rulesButton = new TextButton("Rules", uiManager.getSkin());

        Table table = new Table();
        table.top().left();
        table.setFillParent(true);

        table.add(rulesButton).pad(10);

        uiManager.getStage().addActor(table);

        rulesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showRulesDialog();
            }
        });
    }

    private void showRulesDialog() {
        Dialog dialog = new Dialog("Rules", uiManager.getSkin(), "dialog");
        dialog.text(getRulesText());
        dialog.button("OK");
        dialog.show(uiManager.getStage());
    }

    private String getRulesText() {
        return "1. Players take turns placing tiles on the board. No standing tiles or cap (hat) tiles can be placed during the first round.\n"
             + "2. Players can stack flat tiles or standing tiles on top of flat tiles. Cap tiles cannot be stacked on top of other tiles.\n"
             + "3. Cap tiles can flatten standing tiles, turning them into flat tiles.\n"
             + "4. Movement is orthogonal (along rows or columns). Only the top tile of a stack can be moved. Multiple tiles can be moved, but at least one must be placed on each square along the path.\n"
             + "5. Victory Conditions:\n"
             + "   - Road Victory: Create a continuous orthogonal line of flat or cap tiles connecting one side of the board to the opposite side.\n"
             + "   - Flat Victory: If the board is full and no road victory is achieved, the player with the most visible flat tiles wins.\n"
             + "6. Each turn, players must either place a tile or move a tile/stack. Only cap tiles can flatten standing tiles and become part of a road.\n"
             + "7. The game ends when one of the victory conditions is met.";
    }

    @Override
    public void render(float delta) {
        // Update camera controller
        camController.update();

        // Clear the screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Update the rendering instances in case the AI made a move
        renderer.updatePieceInstances();

        // Render the game
        renderer.render();

        // Render the selection arrow if a piece is selected
        if (selectedPieceType != null) {
            renderSelectionArrow();
        }

        // Update and draw the UI
        uiManager.getStage().act(delta);
        uiManager.getStage().draw();

        // Handle AI move if it's AI's turn
        if (useAI && takGame.getCurrentPlayer() instanceof AIPlayer && !takGame.isGameEnded()) {
            Gdx.app.log("GameScreen", "AI's turn detected. Initiating AI move.");
            handleAIMove();
        }
    }

    private void handleAIMove() {
        if (isAIMoving) {
            return; // Prevent multiple AI moves
        }
        isAIMoving = true;
    
        // Disable input during AI move
        Gdx.input.setInputProcessor(null);
    
        // Execute AI move asynchronously
        Timer.schedule(new Timer.Task(){
            @Override
            public void run() {
                try {
                    AIPlayer ai = (AIPlayer) takGame.getCurrentPlayer();
                    Gdx.app.log("GameScreen", ai.getColor() + " AI is making a move.");
                    ai.makeMove(takGame);
                    Gdx.app.log("GameScreen", ai.getColor() + " AI has made a move.");
                    
                    renderer.updatePieceInstances();
                    uiManager.addMoveToList(ai.toString());
                    uiManager.updatePlayerScores();
                    uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
                    uiManager.updateHotbarColors();
    
                    // Check for game end
                    if (takGame.isGameEnded()) {
                        showGameOverDialog(takGame.getWinner());
                    }
    
                } catch (InvalidMoveException | GameOverException e) {
                    showErrorDialog("AI Move Error: " + e.getMessage());
                    Gdx.app.error("GameScreen", "AI Move Error", e);
                } catch (Exception e) {
                    showErrorDialog("Unexpected AI Error: " + e.getMessage());
                    Gdx.app.error("GameScreen", "Unexpected AI Error", e);
                } finally {
                    // Re-enable input
                    Gdx.input.setInputProcessor(new InputMultiplexer(uiManager.getStage(), inputHandler, camController));
                    isAIMoving = false;
                }
            }
        }, 0.5f); // 0.5-second delay before AI makes a move
    }

    private void renderSelectionArrow() {
        shapeRenderer.setProjectionMatrix(uiManager.getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        float x = 0, y = 0;
        switch (selectedPieceType) {
            case FLAT_STONE:
                x = 50;
                y = 330;
                break;
            case STANDING_STONE:
                x = 50;
                y = 210;
                break;
            case CAPSTONE:
                x = 50;
                y = 85;
                break;
        }

        // Draw a simple arrow pointing from left to right
        shapeRenderer.triangle(x, y - 10, x, y + 10, x + 30, y);
        shapeRenderer.triangle(x + 30, y - 10, x + 30, y + 10, x + 60, y);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        // Update camera viewport
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Update the stage viewport
        uiManager.getStage().getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        uiManager.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    /**
     * Toggles the selection of a piece type. Selecting a piece type if not selected,
     * or deselecting if already selected.
     *
     * @param pieceType The piece type to toggle.
     */
    public void toggleSelection(Piece.PieceType pieceType) {
        // During first two moves, only flat stones can be selected
        if (takGame.getMoveCount() < 2 && pieceType != Piece.PieceType.FLAT_STONE) {
            showErrorDialog("Only flat stones can be placed in the first two moves.");
            return;
        }

        if (selectedPieceType == pieceType) {
            // Deselect if already selected
            selectedPieceType = null;
            renderer.removeHoverOutline();
            Gdx.app.log("GameScreen", "Deselected " + pieceType);
        } else {
            // Select the new piece type
            selectedPieceType = pieceType;
            renderer.removeHoverOutline();
            Gdx.app.log("GameScreen", "Selected " + pieceType);
        }
    }

    /**
     * Updates the UI elements after resetting the game.
     */
    public void updateAfterGameReset() {
        renderer.updatePieceInstances();
        uiManager.movesArray.clear();
        uiManager.movesList.setItems(uiManager.movesArray.toArray(new String[0]));
        uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
        uiManager.updatePlayerScores();
        selectedPieceType = null;
        renderer.removeHoverOutline();
        uiManager.updateHotbarColors();
    }

    /**
     * Shows a game over dialog with the final scores and winner information.
     *
     * @param winner The player who won the game.
     */
    public void showGameOverDialog(Player winner) {
        String message;
        if (winner != null) {
            message = winner.getColor() + " wins!\n" +
                "Final Scores:\n" +
                "Black: " + takGame.getPlayer1().getScore() + "\n" +
                "White: " + takGame.getPlayer2().getScore();
        } else {
            message = "It's a tie!\n" +
                "Final Scores:\n" +
                "Black: " + takGame.getPlayer1().getScore() + "\n" +
                "White: " + takGame.getPlayer2().getScore();
        }

        Dialog dialog = new Dialog("Game Over", uiManager.getSkin(), "default") {
            @Override
            protected void result(Object object) {
                if (object.equals("newGame")) {
                    takGame.resetGame(true); // Reset both board and scores
                    if (useAI) {
                        // If AI uses learning, reset its state
                        Player ai = takGame.getPlayer2();
                        if (ai instanceof AIPlayer) {
                            ((AIPlayer) ai).resetAI();
                        }
                    }
                    updateAfterGameReset();
                } else if (object.equals("continue")) {
                    takGame.resetGame(false); // Reset board but keep scores
                    if (useAI) {
                        // If AI uses learning, reset its state
                        Player ai = takGame.getPlayer2();
                        if (ai instanceof AIPlayer) {
                            ((AIPlayer) ai).resetAI();
                        }
                    }
                    updateAfterGameReset();
                } else if (object.equals("exit")) {
                    Gdx.app.exit();
                }
            }
        };
        dialog.text(message);
        dialog.button("New Game", "newGame");
        dialog.button("Continue", "continue");
        dialog.button("Exit", "exit");
        dialog.show(uiManager.getStage());
    }

    /**
     * Shows an error dialog with the specified message.
     *
     * @param errorMessage The error message to display.
     */
    public void showErrorDialog(String errorMessage) {
        Dialog dialog = new Dialog("Error", uiManager.getSkin(), "default") {
            @Override
            protected void result(Object object) {
                // Optional: Handle dialog result if needed
            }
        };
        dialog.text(errorMessage);
        dialog.button("OK");
        dialog.show(uiManager.getStage());
    }

    public GameRenderer getRenderer() {
        return renderer;
    }
}
