package com.Tak.GUI;

import com.Tak.Logic.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The GameScreen class handles the rendering and UI of the Tak game.
 * It manages the 3D board, pieces, and user interface elements.
 */
public class GameScreen implements Screen {
    private TakGameMain game; // Reference to the main game class
    private PerspectiveCamera camera; // 3D camera
    private ModelBatch modelBatch; // Renders 3D models
    private Environment environment; // Lighting and environment settings
    private ModelInstance boardInstance; // Instance of the game board
    private Model boardModel; // 3D model of the board
    private Model flatStoneModel; // 3D model for Flat Stone
    private Model standingStoneModel; // 3D model for Standing Stone
    private Model capstoneModel; // 3D model for Capstone
    private Array<ModelInstance> pieceInstances; // Instances of placed pieces
    private ModelInstance hoverOutlineInstance; // Instance for hover outline

    private TakGame takGame; // Game logic handler
    private CameraInputController camController; // Camera controls

    private int boardSize = 5; // Board size (can be adjusted)

    // UI elements
    private Stage stage;
    private Skin skin;
    private TextButton newGameButton;
    private TextButton exitButton;
    private Image playerBlackImage;
    private Image playerWhiteImage;
    private Label playerBlackScoreLabel;
    private Label playerWhiteScoreLabel;
    private List<String> movesArray;
    private com.badlogic.gdx.scenes.scene2d.ui.List<String> movesList;
    private Label currentPlayerLabel;

    // Hotbar elements
    private Image normalStoneImage;
    private Image standingStoneImage;
    private Image capstoneImage;

    // Selection variables
    private Piece.PieceType selectedPieceType = null; // Currently selected piece type

    public GameScreen(TakGameMain game) {
        this.game = game;
        create();
    }

    /**
     * Initializes the game screen, including the 3D board, pieces, and UI elements.
     */
    public void create() {
        // Open the game in full screen
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        // Initialize the game logic
        takGame = new TakGame(boardSize);

        // Set up the camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(boardSize * 2f, boardSize * 2f, boardSize * 2f); // Position the camera
        camera.lookAt(boardSize / 2f, 0, boardSize / 2f); // Look at the center of the board
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        // Camera controller for input
        camController = new CameraInputController(camera);

        // Initialize ModelBatch and Environment
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // Load the board texture with grid
        Texture boardTexture;
        try {
            boardTexture = new Texture(Gdx.files.internal("board_texture.png")); // Ensure board_texture.png is in assets
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load board_texture.png", e);
            throw new GdxRuntimeException("Missing asset: board_texture.png");
        }
        Material boardMaterial = new Material(TextureAttribute.createDiffuse(boardTexture));

        // Create the game board model with texture
        ModelBuilder modelBuilder = new ModelBuilder();
        boardModel = modelBuilder.createBox(boardSize, 0.2f, boardSize,
            boardMaterial,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        boardInstance = new ModelInstance(boardModel);
        boardInstance.transform.setToTranslation(boardSize / 2f, -0.1f, boardSize / 2f);

        // Create models for the pieces (placeholders)
        flatStoneModel = modelBuilder.createSphere(0.8f, 0.2f, 0.8f, 32, 32,
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        standingStoneModel = modelBuilder.createBox(0.8f, 1f, 0.8f,
            new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        capstoneModel = modelBuilder.createCone(0.8f, 0.8f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        pieceInstances = new Array<>();

        // Initialize UI elements
        stage = new Stage();
        skin = createBasicSkin();

        newGameButton = new TextButton("New Game", skin);
        exitButton = new TextButton("Exit", skin);

        // Create small black and white circles for player icons
        Texture playerBlackTexture = createCircleTexture(Color.BLACK);
        Texture playerWhiteTexture = createCircleTexture(Color.WHITE);
        playerBlackImage = new Image(new TextureRegionDrawable(new TextureRegion(playerBlackTexture)));
        playerWhiteImage = new Image(new TextureRegionDrawable(new TextureRegion(playerWhiteTexture)));

        // Create labels for player scores
        playerBlackScoreLabel = new Label("Score: 0", skin);
        playerWhiteScoreLabel = new Label("Score: 0", skin);

        // Initialize move list
        movesArray = new ArrayList<>();
        movesList = new com.badlogic.gdx.scenes.scene2d.ui.List<>(skin);
        movesList.setItems(movesArray.toArray(new String[0]));

        currentPlayerLabel = new Label("Current Player: " + takGame.getCurrentPlayer().getColor(), skin);

        // Create left-side panel
        Table leftPanel = new Table();
        leftPanel.defaults().pad(5); // Default padding for elements

        // New Game button
        leftPanel.add(newGameButton).width(120).height(40).row();

        // Player Black info
        leftPanel.add(playerBlackImage).size(64, 64).row();
        leftPanel.add(playerBlackScoreLabel).row();

        // Player White info
        leftPanel.add(playerWhiteImage).size(64, 64).row();
        leftPanel.add(playerWhiteScoreLabel).row();

        // Current Player Label
        leftPanel.add(currentPlayerLabel).padTop(10).row();

        // Move List
        ScrollPane scrollPane = new ScrollPane(movesList, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);
        scrollPane.setForceScroll(false, true); // Disable horizontal scrolling

        leftPanel.add(scrollPane).height(300).width(300).expandY().fillY().row(); // Increased height and width

        // Create hotbar panel
        Table hotbarPanel = new Table();
        hotbarPanel.defaults().pad(5); // Padding between hotbar items

        // Create placeholder shapes for pieces
        normalStoneImage = new Image(new TextureRegionDrawable(createNormalStonePlaceholder()));
        standingStoneImage = new Image(new TextureRegionDrawable(createStandingStonePlaceholder()));
        capstoneImage = new Image(new TextureRegionDrawable(createCapstonePlaceholder()));

        // Add shapes to hotbar
        hotbarPanel.add(normalStoneImage).size(50, 50).row();
        hotbarPanel.add(standingStoneImage).size(50, 50).row();
        hotbarPanel.add(capstoneImage).size(50, 50).row();

        // Add hotbar to left panel
        leftPanel.add(new Label("Hotbar:", skin)).padTop(20).row();
        leftPanel.add(hotbarPanel).padBottom(10).row();

        // Create top-right panel for the exit button
        Table topRightPanel = new Table();
        topRightPanel.add(exitButton).width(100).height(40).pad(10);

        // Create UI table
        Table uiTable = new Table();
        uiTable.setFillParent(true);

        // Add top-right panel first
        uiTable.top().right();
        uiTable.add(topRightPanel).expandX().fillX().row();

        // Add left panel aligned to top left
        uiTable.top().left();
        uiTable.add(leftPanel).expandY().fillY().pad(10).left();

        stage.addActor(uiTable);

        // Add listeners to buttons
        addButtonListeners();

        // Add listeners to hotbar images
        addHotbarListeners();

        // Set input processors
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, camController, new InputHandler()));

        // Initial rendering of the board and pieces
        updatePieceInstances();
        updatePlayerScores();
        updateHotbarColors();
    }

    /**
     * Adds listeners to the New Game and Exit buttons.
     */
    private void addButtonListeners() {
        // Listener for New Game button
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                takGame.resetGame();
                updatePieceInstances();
                movesArray.clear();
                movesList.setItems(movesArray.toArray(new String[0]));
                currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());
                updatePlayerScores();
                updateHotbarColors();
                selectedPieceType = null;
                removeHoverOutline();
            }
        });

        // Listener for Exit button
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    /**
     * Adds listeners to the hotbar images for selecting/deselecting pieces.
     */
    private void addHotbarListeners() {
        // Normal Stone
        normalStoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleSelection(Piece.PieceType.FLAT_STONE);
            }
        });

        // Standing Stone
        standingStoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleSelection(Piece.PieceType.STANDING_STONE);
            }
        });

        // Capstone
        capstoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleSelection(Piece.PieceType.CAPSTONE);
            }
        });
    }

    /**
     * Toggles the selection of a piece type. Selecting a piece type if not selected,
     * or deselecting if already selected.
     *
     * @param pieceType The piece type to toggle.
     */
    private void toggleSelection(Piece.PieceType pieceType) {
        if (selectedPieceType == pieceType) {
            // Deselect if already selected
            selectedPieceType = null;
            removeHoverOutline();
            Gdx.app.log("GameScreen", "Deselected " + pieceType);
        } else {
            // Select the new piece type
            selectedPieceType = pieceType;
            removeHoverOutline();
            Gdx.app.log("GameScreen", "Selected " + pieceType);
        }
    }

    /**
     * Renders the game screen, including the 3D board, pieces, and UI elements.
     *
     * @param delta Time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        // Update camera controller
        camController.update();

        // Clear the screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render the board and pieces
        modelBatch.begin(camera);
        modelBatch.render(boardInstance, environment);
        for (ModelInstance instance : pieceInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        // Update and draw the UI
        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the rendering instances of all pieces on the board.
     */
    private void updatePieceInstances() {
        pieceInstances.clear();
        Board board = takGame.getBoard();
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                List<Piece> stack = board.getBoardPosition(x, y);
                if (!stack.isEmpty()) {
                    float heightOffset = 0f;
                    for (int i = 0; i < stack.size(); i++) {
                        Piece piece = stack.get(i);
                        Model model = getModelForPiece(piece);
                        ModelInstance pieceInstance = new ModelInstance(model);
                        pieceInstance.transform.setToTranslation(x + 0.5f, heightOffset, y + 0.5f);

                        // Apply color based on the owner
                        Color pieceColor = piece.getOwner().getColor() == Player.Color.WHITE ? Color.WHITE : Color.BLACK;
                        ((ColorAttribute) pieceInstance.materials.get(0).get(ColorAttribute.Diffuse)).color.set(pieceColor);

                        pieceInstances.add(pieceInstance);
                        heightOffset += getHeightForPiece(piece);
                    }
                }
            }
        }

        // Re-add the board to ensure it's rendered beneath all pieces
        pieceInstances.add(boardInstance);
    }

    /**
     * Returns the appropriate model for a given piece type.
     *
     * @param piece The piece whose model is needed.
     * @return The corresponding model.
     */
    private Model getModelForPiece(Piece piece) {
        switch (piece.getPieceType()) {
            case FLAT_STONE:
                return flatStoneModel;
            case STANDING_STONE:
                return standingStoneModel;
            case CAPSTONE:
                return capstoneModel;
            default:
                return flatStoneModel;
        }
    }

    /**
     * Returns the height offset for a given piece type.
     *
     * @param piece The piece whose height is needed.
     * @return The height offset.
     */
    private float getHeightForPiece(Piece piece) {
        switch (piece.getPieceType()) {
            case FLAT_STONE:
                return 0.2f;
            case STANDING_STONE:
                return 1f;
            case CAPSTONE:
                return 0.8f;
            default:
                return 0.2f;
        }
    }

    /**
     * Resizes the viewport when the window size changes.
     *
     * @param width  The new width.
     * @param height The new height.
     */
    @Override
    public void resize(int width, int height) {
        // Update camera viewport
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Update the stage viewport
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes of all resources to prevent memory leaks.
     */
    @Override
    public void dispose() {
        modelBatch.dispose();
        boardModel.dispose();
        flatStoneModel.dispose();
        standingStoneModel.dispose();
        capstoneModel.dispose();
        stage.dispose();
        skin.dispose();

        // Dispose of any remaining hover outline model to prevent memory leaks
        if (hoverOutlineInstance != null && hoverOutlineInstance.model != null) {
            hoverOutlineInstance.model.dispose();
        }
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
     * Handles user input for placing pieces on the board.
     */
    private class InputHandler extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;

            // If no piece is selected, do nothing
            if (selectedPieceType == null) return false;

            // Convert screen coordinates to world coordinates
            Ray pickRay = camera.getPickRay(screenX, screenY);
            Vector3 intersection = new Vector3();

            // Define the plane of the board
            Plane boardPlane = new Plane(new Vector3(0, 1, 0), new Vector3(boardSize / 2f, 0, boardSize / 2f));

            if (Intersector.intersectRayPlane(pickRay, boardPlane, intersection)) {
                int x = (int) Math.floor(intersection.x);
                int y = (int) Math.floor(intersection.z);

                Gdx.app.log("GameScreen", "Clicked position: (" + x + ", " + y + ")");

                if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                    try {
                        // Place the selected piece
                        takGame.placePiece(x, y, selectedPieceType);

                        // Update the rendering
                        updatePieceInstances();

                        // Update the moves list and current player label
                        String moveDescription = "Player " + takGame.getCurrentPlayer().getColor() + " placed " + selectedPieceType + " at (" + x + ", " + y + ")";
                        movesArray.add(moveDescription);
                        movesList.setItems(movesArray.toArray(new String[0]));
                        currentPlayerLabel.setText("Current Player: " + takGame.getCurrentPlayer().getColor());

                        // Update player scores
                        updatePlayerScores();

                        // Switch player and update hotbar colors
                        takGame.switchPlayer();
                        updateHotbarColors();

                        // Deselect the piece
                        selectedPieceType = null;
                        removeHoverOutline();

                        // Check for game end
                        if (takGame.isGameEnded()) {
                            showGameOverDialog();
                        }
                    } catch (InvalidMoveException e) {
                        // Handle invalid move (e.g., show a message)
                        Gdx.app.error("GameScreen", "Invalid Move: " + e.getMessage(), e);
                        showErrorDialog("Invalid Move: " + e.getMessage());
                    } catch (GameOverException e) {
                        // Handle game over
                        Gdx.app.log("GameScreen", "Game Over: " + e.getMessage());
                        showGameOverDialog();
                    }
                } else {
                    Gdx.app.log("GameScreen", "Clicked out of bounds: (" + x + ", " + y + ")");
                }
            } else {
                Gdx.app.log("GameScreen", "No intersection with board plane.");
            }
            return true;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            if (selectedPieceType == null) {
                removeHoverOutline();
                return false;
            }

            // Convert screen coordinates to world coordinates
            Ray pickRay = camera.getPickRay(screenX, screenY);
            Vector3 intersection = new Vector3();

            // Define the plane of the board
            Plane boardPlane = new Plane(new Vector3(0, 1, 0), new Vector3(boardSize / 2f, 0, boardSize / 2f));

            if (Intersector.intersectRayPlane(pickRay, boardPlane, intersection)) {
                int x = (int) Math.floor(intersection.x);
                int y = (int) Math.floor(intersection.z);

                Gdx.app.log("GameScreen", "Hovering over: (" + x + ", " + y + ")");

                if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                    // Remove existing hover outline before creating a new one
                    removeHoverOutline();

                    // Create the hover outline
                    hoverOutlineInstance = createHoverOutline(x, y);
                    pieceInstances.add(hoverOutlineInstance);
                    Gdx.app.log("GameScreen", "Created hover outline at: (" + x + ", " + y + ")");
                } else {
                    removeHoverOutline();
                }
            } else {
                removeHoverOutline();
            }

            return false;
        }

        /**
         * Creates a blue outline ModelInstance for the hovered square.
         *
         * @param x The x-coordinate of the square.
         * @param y The y-coordinate of the square.
         * @return The hover outline ModelInstance.
         */
        private ModelInstance createHoverOutline(int x, int y) {
            ModelBuilder modelBuilder = new ModelBuilder();
            // Removed modelBuilder.begin(); to prevent "Call end() first" exception

            // Create a box slightly above the board to serve as an outline
            Material outlineMaterial = new Material(ColorAttribute.createDiffuse(new Color(0, 0, 1, 0.3f)));
            Model outlineModel = modelBuilder.createBox(1.0f, 0.05f, 1.0f,
                outlineMaterial,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

            ModelInstance outlineInstance = new ModelInstance(outlineModel);
            outlineInstance.transform.setToTranslation(x + 0.5f, 0.1f, y + 0.5f);

            return outlineInstance;
        }
    }

    /**
     * Removes the hover outline from the board.
     */
    private void removeHoverOutline() {
        if (hoverOutlineInstance != null) {
            pieceInstances.removeValue(hoverOutlineInstance, true);
            hoverOutlineInstance = null;
            Gdx.app.log("GameScreen", "Removed hover outline.");
        }
    }

    /**
     * Creates a placeholder texture for a normal stone (circle).
     *
     * @return The generated texture.
     */
    private Texture createNormalStonePlaceholder() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 2); // White circle with padding

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a placeholder texture for a standing stone (rectangle).
     *
     * @return The generated texture.
     */
    private Texture createStandingStonePlaceholder() {
        int width = 64;
        int height = 64;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(8, 16, width - 16, height - 32); // White rectangle with padding

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a placeholder texture for a capstone (cone).
     *
     * @return The generated texture.
     */
    private Texture createCapstonePlaceholder() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        // Draw a simple cone shape using triangles
        pixmap.fillTriangle(size / 2, size - 2, 2, 2, size - 2, 2); // Base triangle

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a small circle texture of a given color for player icons.
     *
     * @param color The color of the circle.
     * @return The generated texture.
     */
    private Texture createCircleTexture(Color color) {
        int size = 64; // Size of the circle
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(color);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 2); // Circle with padding

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a colored placeholder texture based on the type of piece and the specified color.
     *
     * @param type  The type of piece ("normal", "standing", "capstone").
     * @param color The color to apply.
     * @return The generated texture.
     */
    private Texture createColoredPlaceholder(String type, Color color) {
        switch (type) {
            case "normal":
                return createNormalStonePlaceholderWithColor(color);
            case "standing":
                return createStandingStonePlaceholderWithColor(color);
            case "capstone":
                return createCapstonePlaceholderWithColor(color);
            default:
                return createNormalStonePlaceholderWithColor(color);
        }
    }

    /**
     * Creates a colored placeholder texture for a normal stone (circle).
     *
     * @param color The color to apply.
     * @return The generated texture.
     */
    private Texture createNormalStonePlaceholderWithColor(Color color) {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(color);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 2); // Circle with padding

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a colored placeholder texture for a standing stone (rectangle).
     *
     * @param color The color to apply.
     * @return The generated texture.
     */
    private Texture createStandingStonePlaceholderWithColor(Color color) {
        int width = 64;
        int height = 64;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(color);
        pixmap.fillRectangle(8, 16, width - 16, height - 32); // Rectangle with padding

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a colored placeholder texture for a capstone (cone).
     *
     * @param color The color to apply.
     * @return The generated texture.
     */
    private Texture createCapstonePlaceholderWithColor(Color color) {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0); // Transparent background
        pixmap.fill();

        pixmap.setColor(color);
        // Draw a simple cone shape using triangles
        pixmap.fillTriangle(size / 2, size - 2, 2, 2, size - 2, 2); // Base triangle

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a basic skin programmatically for UI elements.
     *
     * @return The created skin.
     */
    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Create a default font
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        // Create button textures
        Pixmap pixmapUp = new Pixmap(150, 60, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(Color.GRAY);
        pixmapUp.fill();
        skin.add("button-up", new Texture(pixmapUp));

        Pixmap pixmapDown = new Pixmap(150, 60, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(Color.DARK_GRAY);
        pixmapDown.fill();
        skin.add("button-down", new Texture(pixmapDown));

        pixmapUp.dispose();
        pixmapDown.dispose();

        // Create a TextButton style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button-up");
        textButtonStyle.down = skin.newDrawable("button-down");
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        // Create a Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        skin.add("default", labelStyle);

        // Create a List style
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
        listStyle.font = skin.getFont("default");
        listStyle.selection = skin.newDrawable("button-down", Color.DARK_GRAY);
        listStyle.background = skin.newDrawable("button-up", Color.LIGHT_GRAY);
        skin.add("default", listStyle);

        // Create a ScrollPane style
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollPaneStyle);

        // Create an ImageButton style (if needed in the future)
        skin.add("default", new ImageButton.ImageButtonStyle());

        return skin;
    }

    /**
     * Updates the player scores displayed on the UI.
     */
    private void updatePlayerScores() {
        // Retrieve scores from players
        int playerBlackScore = takGame.getPlayer1().getScore();
        int playerWhiteScore = takGame.getPlayer2().getScore();

        // Update score labels
        playerBlackScoreLabel.setText("Score: " + playerBlackScore);
        playerWhiteScoreLabel.setText("Score: " + playerWhiteScore);
    }

    /**
     * Updates the colors of the hotbar pieces based on the current player's turn.
     */
    private void updateHotbarColors() {
        Color currentColor = takGame.getCurrentPlayer().getColor() == Player.Color.WHITE ? Color.WHITE : Color.BLACK;

        // Update normal stone color
        if (normalStoneImage.getDrawable() != null) {
            Texture oldTexture = ((TextureRegionDrawable) normalStoneImage.getDrawable()).getRegion().getTexture();
            oldTexture.dispose();
        }
        normalStoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("normal", currentColor)));

        // Update standing stone color
        if (standingStoneImage.getDrawable() != null) {
            Texture oldTexture = ((TextureRegionDrawable) standingStoneImage.getDrawable()).getRegion().getTexture();
            oldTexture.dispose();
        }
        standingStoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("standing", currentColor)));

        // Update capstone color
        if (capstoneImage.getDrawable() != null) {
            Texture oldTexture = ((TextureRegionDrawable) capstoneImage.getDrawable()).getRegion().getTexture();
            oldTexture.dispose();
        }
        capstoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("capstone", currentColor)));
    }

    /**
     * Shows a game over dialog with the final scores and winner information.
     */
    private void showGameOverDialog() {
        Player winner = null;
        if (takGame.getPlayer1().getScore() > takGame.getPlayer2().getScore()) {
            winner = takGame.getPlayer1();
        } else if (takGame.getPlayer2().getScore() > takGame.getPlayer1().getScore()) {
            winner = takGame.getPlayer2();
        }

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

        Dialog dialog = new Dialog("Game Over", skin) {
            @Override
            protected void result(Object object) {
                // Optional: Handle dialog result if needed
            }
        };
        dialog.text(message);
        dialog.button("OK");
        dialog.show(stage);
    }

    /**
     * Shows an error dialog with the specified message.
     *
     * @param errorMessage The error message to display.
     */
    private void showErrorDialog(String errorMessage) {
        Dialog dialog = new Dialog("Error", skin) {
            @Override
            protected void result(Object object) {
                // Optional: Handle dialog result if needed
            }
        };
        dialog.text(errorMessage);
        dialog.button("OK");
        dialog.show(stage);
    }
}
