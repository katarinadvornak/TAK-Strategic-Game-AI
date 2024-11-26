// File: core/src/main/java/com/Tak/GUI/GameScreen.java
package com.Tak.GUI;

import com.Tak.AI.players.MinimaxAgent;
import com.Tak.AI.players.RandomAIPlayer;
import com.Tak.GUI.UIManager.DropCountsCallback;
import com.Tak.Logic.exceptions.GameOverException;
import com.Tak.Logic.exceptions.InvalidMoveException;
import com.Tak.Logic.models.Direction;
import com.Tak.Logic.models.Piece;
import com.Tak.Logic.models.Piece.PieceType;
import com.Tak.Logic.models.Player;
import com.Tak.Logic.models.TakGame;
import com.Tak.Logic.models.GameMode;
import com.Tak.Logic.utils.Logger;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameScreen class handles the main game screen, coordinating between the renderer, UI, and input handler.
 */

public class GameScreen implements Screen, GameInputHandler.UICallback {
    private TakGameMain game;
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
    private GameMode gameMode; // Added game mode

    private Player aiPlayer; // **Added AI player reference**

    /**
     * Constructor to initialize the GameScreen.
     *
     * @param game      The main game instance.
     * @param gameMode  The selected game mode (Human vs AI or Human vs Human).
     */
    public GameScreen(TakGameMain game, GameMode gameMode) {
        this.game = game;
        this.gameMode = gameMode;
        this.useAI = (gameMode == GameMode.HUMAN_VS_AI);
        create();
    }   

    /**
     * Initializes the game components, camera, renderer, UI, and input handler.
     */
    public void create() {
        // Initialize the game logic based on game mode
        Player humanPlayer = new com.Tak.Logic.players.HumanPlayer(Player.Color.GREEN, 21, 1, 1); // Adjust piece counts as needed
        aiPlayer = null; // **Initialize aiPlayer**

        if (useAI) {
            aiPlayer = new MinimaxAgent(Player.Color.BLUE, 21, 1, 1, 3); // Default depth 3
        }

        if (aiPlayer != null) {
            // Set opponents
            humanPlayer.setOpponent(aiPlayer);
            aiPlayer.setOpponent(humanPlayer);
        }

        List<Player> players = new ArrayList<>();
        players.add(humanPlayer);
        if (aiPlayer != null) {
            players.add(aiPlayer);
        }

        takGame = new TakGame(boardSize, players);

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

        // Initialize ShapeRenderer
        shapeRenderer = new ShapeRenderer();

        // Initialize UI Manager
        uiManager = new UIManager(takGame, this);
        uiManager.getStage().setViewport(new ScreenViewport());

        // Initialize Input Handler with UICallback implementation
        inputHandler = new GameInputHandler(camera, takGame, renderer, uiManager, this);

        // Set input processors
        if (useAI) {
            Gdx.input.setInputProcessor(new InputMultiplexer(uiManager.getStage(), inputHandler, camController));
        } else {
            Gdx.input.setInputProcessor(new InputMultiplexer(uiManager.getStage(), inputHandler, camController));
        }

        addRulesButton();
        addBackButton();
        if (useAI) {
            addAISelectionDropdown();
        }

        // After setting up UI elements
        uiManager.updateHotbarColors();
    }

    // [Other methods remain unchanged]

    /**
     * Changes the AI behavior based on the selected AI type and depth.
     *
     * @param aiType The selected AI type ("RandomAI" or "MinimaxAgent").
     * @param depth  The selected depth for MinimaxAgent. Ignored for RandomAI.
     */
    private void changeAIBehavior(String aiType, int depth) {
        if (!(aiPlayer instanceof MinimaxAgent || aiPlayer instanceof RandomAIPlayer)) {
            return; // Skip if the current player isn't an AI
        }

        Player newAI;
        if ("RandomAI".equals(aiType)) {
            newAI = new RandomAIPlayer(Player.Color.BLUE,
                                    aiPlayer.getRemainingPieces(Piece.PieceType.FLAT_STONE),
                                    aiPlayer.getRemainingPieces(Piece.PieceType.STANDING_STONE),
                                    aiPlayer.getRemainingPieces(Piece.PieceType.CAPSTONE));
        } else { // MinimaxAgent
            newAI = new MinimaxAgent(Player.Color.BLUE,
                                    aiPlayer.getRemainingPieces(Piece.PieceType.FLAT_STONE),
                                    aiPlayer.getRemainingPieces(Piece.PieceType.STANDING_STONE),
                                    aiPlayer.getRemainingPieces(Piece.PieceType.CAPSTONE),
                                    depth); // Use selected depth
        }

        // Replace the AI player using TakGame's replacePlayer method
        takGame.replacePlayer(aiPlayer, newAI);

        // Update the aiPlayer reference to point to the new AI
        aiPlayer = newAI;

        // Update UI or other components if necessary
        if ("RandomAI".equals(aiType)) {
            Gdx.app.log("GameScreen", "AI changed to RandomAI");
        } else {
            Gdx.app.log("GameScreen", "AI changed to MinimaxAgent with depth " + depth);
        }
    }



    /**
     * Adds the Rules button to the UI.
     */
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

    /**
     * Adds a Back button to return to the main menu.
     */
    private void addBackButton() {
        TextButton backButton = new TextButton("Back to Main Menu", uiManager.getSkin());
        Table backButtonTable = new Table();
        backButtonTable.bottom().left();
        backButtonTable.setFillParent(true);

        backButtonTable.add(backButton).pad(10).width(150).height(40);
        uiManager.getStage().addActor(backButtonTable);

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game)); // Navigate back to the main menu
            }
        });
    }

    /**
     * Adds a dropdown for AI selection to dynamically change the AI type.
     * Also adds a dropdown for MinimaxAgent depth selection.
     */
    private void addAISelectionDropdown() {
        SelectBox<String> aiSelectBox = new SelectBox<>(uiManager.getSkin());
        aiSelectBox.setItems("RandomAI", "MinimaxAgent");
        aiSelectBox.setSelected("MinimaxAgent"); // Default selection

        SelectBox<Integer> depthSelectBox = new SelectBox<>(uiManager.getSkin());
        depthSelectBox.setItems(1, 2, 3, 4);
        depthSelectBox.setVisible(true); // Initially visible since default is MinimaxAgent
        depthSelectBox.setSelected(3); // Default depth

        Table dropdownTable = new Table();
        dropdownTable.bottom().right();
        dropdownTable.setFillParent(true);

        dropdownTable.add(new Label("AI Type: ", uiManager.getSkin())).pad(10);
        dropdownTable.add(aiSelectBox).width(150).height(40).pad(10);
        dropdownTable.row();
        dropdownTable.add(new Label("Minimax Depth: ", uiManager.getSkin())).pad(10);
        dropdownTable.add(depthSelectBox).width(150).height(40).pad(10);

        uiManager.getStage().addActor(dropdownTable);

        aiSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedAI = aiSelectBox.getSelected();
                if ("MinimaxAgent".equals(selectedAI)) {
                    depthSelectBox.setVisible(true);
                } else {
                    depthSelectBox.setVisible(false);
                }
                changeAIBehavior(selectedAI, depthSelectBox.getSelected());
            }
        });

        depthSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedAI = aiSelectBox.getSelected();
                if ("MinimaxAgent".equals(selectedAI)) {
                    int selectedDepth = depthSelectBox.getSelected();
                    changeAIBehavior(selectedAI, selectedDepth);
                }
            }
        });
    }


    /**
     * Displays the game rules in a dialog.
     */
    private void showRulesDialog() {
        Dialog dialog = new Dialog("Rules", uiManager.getSkin(), "dialog");
        dialog.text(getRulesText());
        dialog.button("OK");
        dialog.show(uiManager.getStage());
    }

    /**
     * Retrieves the game rules text.
     *
     * @return The game rules.
     */
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

        // Update and draw the UI
        uiManager.getStage().act(delta);
        uiManager.getStage().draw();

        // Handle AI move if it's AI's turn and AI is not already moving
        if (useAI && !takGame.isGameEnded()) {
            Player currentAI = takGame.getCurrentPlayer();
            if (currentAI instanceof MinimaxAgent || currentAI instanceof RandomAIPlayer) {
                handleAIMove();
            }
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
                    Player currentAI = takGame.getCurrentPlayer();
                    if (currentAI instanceof MinimaxAgent) {
                        ((MinimaxAgent) currentAI).makeMove(takGame);
                    } else if (currentAI instanceof RandomAIPlayer) {
                        ((RandomAIPlayer) currentAI).makeMove(takGame);
                    }

                    // Update the rendering
                    renderer.updatePieceInstances();

                    // Update UI elements
                    uiManager.addMoveToList("AI moved");
                    uiManager.updatePlayerScores();
                    updateCurrentPlayerLabel();
                    updateHotbarColors();

                    // Check for game end
                    if (takGame.isGameEnded()) {
                        showGameOverDialog(takGame.getWinner());
                    }
                } catch (InvalidMoveException | GameOverException e) {
                    // Handle exceptions
                    Logger.log("GameScreen", "AI encountered an error: " + e.getMessage());
                } finally {
                    // Re-enable input if it's a human player's turn
                    if (!(takGame.getCurrentPlayer() instanceof MinimaxAgent || takGame.getCurrentPlayer() instanceof RandomAIPlayer)) {
                        Gdx.input.setInputProcessor(new InputMultiplexer(uiManager.getStage(), inputHandler, camController));
                    }
                    isAIMoving = false;
                }
            }
        }, 0.5f); // 0.5-second delay before AI makes a move
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
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void pause() {
        // Called when the application is paused
    }

    @Override
    public void resume() {
        // Called when the application is resumed
    }

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

        // Check if the player has remaining stones of the selected type
        int remainingPieces = takGame.getCurrentPlayer().getRemainingPieces(pieceType);
        if (remainingPieces <= 0) {
            showErrorDialog("No remaining " + pieceType.toString().replace("_", " ").toLowerCase() + "s left.");
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
        uiManager.clearMovesList();
        uiManager.updatePlayerScores();
        updateCurrentPlayerLabel();
        updateHotbarColors();
        selectedPieceType = null;
        renderer.removeHoverOutline();
    }

    /**
     * Updates the current player label on the UI.
     */
    @Override
    public void updateCurrentPlayerLabel() {
        uiManager.currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
    }

    /**
     * Updates the hotbar colors based on the current game state.
     */
    @Override
    public void updateHotbarColors() {
        uiManager.updateHotbarColors();
    }

    /**
     * Shows a game over dialog with the final scores and winner information.
     *
     * @param winner The player who won the game.
     */
    @Override
    public void showGameOverDialog(Player winner) {
        String message;
        if (winner != null) {
            message = winner.getColor() + " wins!\n"
                + "Final Scores:\n"
                + "BLUE: " + takGame.getPlayer1().getScore() + "\n"
                + "GREEN: " + takGame.getPlayer2().getScore();
        } else {
            message = "It's a tie!\n"
                + "Final Scores:\n"
                + "BLUE: " + takGame.getPlayer1().getScore() + "\n"
                + "GREEN: " + takGame.getPlayer2().getScore();
        }

        Dialog dialog = new Dialog("Game Over", uiManager.getSkin(), "dialog") {
            @Override
            protected void result(Object object) {
                if (object.equals("newGame")) {
                    takGame.resetGame(true); // Reset both board and scores
                    updateAfterGameReset();
                } else if (object.equals("continue")) {
                    takGame.resetGame(false); // Reset board but keep scores
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
    @Override
    public void showErrorDialog(String errorMessage) {
        Dialog dialog = new Dialog("Error", uiManager.getSkin(), "dialog");
        dialog.text(errorMessage);
        dialog.button("OK");
        dialog.show(uiManager.getStage());
    }

    @Override
    public void addMoveToList(String moveDescription) {
        uiManager.addMoveToList(moveDescription);
    }

    @Override
    public void updatePlayerScores() {
        uiManager.updatePlayerScores();
    }

    @Override
    public void showDropCountsPrompt(int sourceX, int sourceY, Direction direction, DropCountsCallback callback) {
        uiManager.promptForDropCounts(sourceX, sourceY, direction, callback);
    }

    @Override
    public PieceType getSelectedPieceType() {
        return selectedPieceType;
    }

    @Override
    public void deselectPiece() {
        selectedPieceType = null;
        renderer.removeHoverOutline();
        Gdx.app.log("GameScreen", "Piece deselected");
    }

    @Override
    public boolean isPieceSelected() {
        return selectedPieceType != null;
    }
}
