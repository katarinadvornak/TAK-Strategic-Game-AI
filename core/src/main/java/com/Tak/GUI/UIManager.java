package com.Tak.GUI;

import com.Tak.Logic.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

/**
 * The UIManager class handles the creation and management of UI elements.
 * It now includes counters for the remaining pieces next to the hotbar.
 */
public class UIManager {
    private Stage stage;
    private Skin skin;

    // UI elements
    public TextButton newGameButton, exitButton;
    public Label currentPlayerLabel;
    public Label playerBlackScoreLabel, playerWhiteScoreLabel;
    public List<String> movesList;
    public Image normalStoneImage, standingStoneImage, capstoneImage;
    public Table hotbarTable;

    // **New: Labels for piece counts**
    private Label normalStoneCountLabel, standingStoneCountLabel, capstoneCountLabel;

    public ArrayList<String> movesArray;

    private TakGame takGame;
    private GameScreen gameScreen;

    /**
     * Constructor to initialize the UIManager with the game logic and screen reference.
     *
     * @param takGame    The current game logic instance.
     * @param gameScreen The game screen instance.
     */
    public UIManager(TakGame takGame, GameScreen gameScreen) {
        this.takGame = takGame;
        this.gameScreen = gameScreen;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Ensure the stage receives input events
        createSkin();
        createUIElements();
    }

    /**
     * Creates a basic skin programmatically for UI elements.
     */
    private void createSkin() {
        skin = new Skin();

        // Create a default font
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

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

        // Create Drawable for cursor and selection before using them
        Pixmap cursorPixmap = new Pixmap(1, 20, Pixmap.Format.RGBA8888);
        cursorPixmap.setColor(Color.WHITE);
        cursorPixmap.fill();
        skin.add("cursor", new Texture(cursorPixmap));
        cursorPixmap.dispose();

        Pixmap selectionPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        selectionPixmap.setColor(0.5f, 0.5f, 1, 0.5f);
        selectionPixmap.fill();
        skin.add("selection", new Texture(selectionPixmap));
        selectionPixmap.dispose();

        Pixmap textFieldBackgroundPixmap = new Pixmap(150, 30, Pixmap.Format.RGBA8888);
        textFieldBackgroundPixmap.setColor(Color.DARK_GRAY);
        textFieldBackgroundPixmap.fill();
        skin.add("textfield-background", new Texture(textFieldBackgroundPixmap));
        textFieldBackgroundPixmap.dispose();

        // Create a TextButton style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button-up");
        textButtonStyle.down = skin.newDrawable("button-down");
        textButtonStyle.font = skin.getFont("default-font");
        skin.add("default", textButtonStyle);

        // Create a Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        skin.add("default", labelStyle);

        // Create a List style
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
        listStyle.font = skin.getFont("default-font");
        listStyle.selection = skin.newDrawable("button-down", Color.DARK_GRAY);
        listStyle.background = skin.newDrawable("button-up", Color.LIGHT_GRAY);
        skin.add("default", listStyle);

        // Create a ScrollPane style
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollPaneStyle);

        // Create and add TextFieldStyle
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = skin.getFont("default-font");
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.cursor = skin.newDrawable("cursor");
        textFieldStyle.selection = skin.newDrawable("selection");
        textFieldStyle.background = skin.newDrawable("textfield-background");
        skin.add("default", textFieldStyle);

        // Add WindowStyle for Dialogs
        Pixmap windowPixmap = new Pixmap(400, 200, Pixmap.Format.RGBA8888);
        windowPixmap.setColor(Color.LIGHT_GRAY);
        windowPixmap.fill();
        skin.add("window-background", new Texture(windowPixmap));
        windowPixmap.dispose();

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = skin.getFont("default-font");
        windowStyle.background = skin.newDrawable("window-background");
        skin.add("default", windowStyle);
        // **Add the "dialog" WindowStyle**
        Window.WindowStyle dialogStyle = new Window.WindowStyle();
        dialogStyle.titleFont = skin.getFont("default-font");
        dialogStyle.background = skin.newDrawable("window-background");
        skin.add("dialog", dialogStyle); // Register the "dialog" style
    }

    /**
     * Creates all UI elements, including the hotbar and piece counters.
     */
    private void createUIElements() {
        newGameButton = new TextButton("New Game", skin);
        exitButton = new TextButton("Exit", skin);

        // Create small black and white circles for player icons
        Texture playerBlackTexture = createCircleTexture(Color.BLACK);
        Texture playerWhiteTexture = createCircleTexture(Color.WHITE);
        Image playerBlackImage = new Image(new TextureRegionDrawable(new TextureRegion(playerBlackTexture)));
        Image playerWhiteImage = new Image(new TextureRegionDrawable(new TextureRegion(playerWhiteTexture)));

        // Create labels for player scores
        playerBlackScoreLabel = new Label("Score: 0", skin);
        playerWhiteScoreLabel = new Label("Score: 0", skin);

        // Initialize move list
        movesArray = new ArrayList<>();
        movesList = new List<>(skin);
        movesList.setItems(movesArray.toArray(new String[0]));
        ScrollPane movesScrollPane = new ScrollPane(movesList, skin);
        movesScrollPane.setFadeScrollBars(false);
        movesScrollPane.setScrollbarsVisible(true);
        movesScrollPane.setForceScroll(false, true); // Disable horizontal scrolling

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
        leftPanel.add(movesScrollPane).height(300).width(300).expandY().fillY().row(); // Increased height and width

        // **New: Create hotbar panel with piece counters**
        Table hotbarPanel = new Table();
        hotbarPanel.defaults().pad(5); // Padding between hotbar items

        // Create placeholder shapes for pieces
        normalStoneImage = new Image(new TextureRegionDrawable(createNormalStonePlaceholder()));
        standingStoneImage = new Image(new TextureRegionDrawable(createStandingStonePlaceholder()));
        capstoneImage = new Image(new TextureRegionDrawable(createCapstonePlaceholder()));

        // **Adjusting the sizes of Standing Stone and Capstone**
        // Here, we set different sizes to make them visually distinct
        normalStoneImage.setSize(50, 50);    // Flat Stone size
        standingStoneImage.setSize(60, 60);  // Standing Stone size (larger)
        capstoneImage.setSize(55, 55);        // Capstone size (medium)

        // Add labels to hotbar images
        Label normalStoneLabel = new Label("Flat Stone", skin);
        Label standingStoneLabel = new Label("Standing Stone", skin);
        Label capstoneLabel = new Label("Capstone", skin);

        // **Initialize new piece count labels without "Left:" prefix**
        normalStoneCountLabel = new Label("0", skin);
        standingStoneCountLabel = new Label("0", skin);
        capstoneCountLabel = new Label("0", skin);

        // **Create separate tables for each piece type to align counts beside images**
        Table flatStoneTable = new Table();
        flatStoneTable.add(normalStoneImage).size(50, 50).padRight(10); // Image with right padding
        flatStoneTable.add(normalStoneCountLabel).align(Align.left).row(); // Count label beside image

        Table standingStoneTable = new Table();
        standingStoneTable.add(standingStoneImage).size(60, 60).padRight(10);
        standingStoneTable.add(standingStoneCountLabel).align(Align.left).row();

        Table capstoneTable = new Table();
        capstoneTable.add(capstoneImage).size(55, 55).padRight(10);
        capstoneTable.add(capstoneCountLabel).align(Align.left).row();

        // **Add piece tables to hotbarPanel**
        hotbarPanel.add(flatStoneTable).expandX().fillX().row();
        hotbarPanel.add(new Label("Flat Stone", skin)).align(Align.left).row(); // Optional: You can remove this if labels are not needed
        hotbarPanel.add(standingStoneTable).expandX().fillX().row();
        hotbarPanel.add(new Label("Standing Stone", skin)).align(Align.left).row();
        hotbarPanel.add(capstoneTable).expandX().fillX().row();
        hotbarPanel.add(new Label("Capstone", skin)).align(Align.left).row();

        // **Alternative: If you prefer labels above or beside, adjust accordingly**
        /*
        hotbarPanel.add(flatStoneTable).align(Align.left).row();
        hotbarPanel.add(new Label("Flat Stone", skin)).align(Align.left).row();
        hotbarPanel.add(standingStoneTable).align(Align.left).row();
        hotbarPanel.add(new Label("Standing Stone", skin)).align(Align.left).row();
        hotbarPanel.add(capstoneTable).align(Align.left).row();
        hotbarPanel.add(new Label("Capstone", skin)).align(Align.left).row();
        */

        // **Add hotbar to left panel**
        leftPanel.add(new Label("Hotbar:", skin)).padTop(20).row();
        leftPanel.add(hotbarPanel).padBottom(10).row();

        // Create UI table
        Table uiTable = new Table();
        uiTable.setFillParent(true);

        // Create top-right panel for the exit button
        Table topRightPanel = new Table();
        topRightPanel.add(exitButton).width(100).height(40).pad(10);

        // Add top-right panel first
        uiTable.top().right();
        uiTable.add(topRightPanel).expandX().fillX().row();

        // Add left panel aligned to top left
        uiTable.top().left();
        uiTable.add(leftPanel).expandY().fillY().pad(10).left();

        stage.addActor(uiTable);

        // Add listeners to buttons and hotbar images
        addButtonListeners();
        addHotbarListeners();

        // **After setting up UI elements, update the hotbar colors and piece counts**
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
                takGame.resetGame(true); // Reset both board and scores
                gameScreen.updateAfterGameReset();
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
     * Adds listeners to the hotbar images for selecting piece types.
     */
    private void addHotbarListeners() {
        // Normal Stone
        normalStoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.toggleSelection(Piece.PieceType.FLAT_STONE);
            }
        });

        // Standing Stone
        standingStoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.toggleSelection(Piece.PieceType.STANDING_STONE);
            }
        });

        // Capstone
        capstoneImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.toggleSelection(Piece.PieceType.CAPSTONE);
            }
        });
    }

    /**
     * Updates the hotbar colors and piece count labels based on the current game state.
     */
    public void updateHotbarColors() {
        // Determine the current player's color
        Color currentColor = takGame.getCurrentPlayer().getColor() == Player.Color.WHITE ? Color.WHITE : Color.BLACK;

        Player targetPlayer;
        if (takGame.getMoveCount() < 2) {
            // **First two moves: players place opponent's pieces**
            targetPlayer = takGame.getOpponentPlayer();
            // Hide standing stone and capstone during first two moves
            standingStoneImage.setVisible(false);
            standingStoneCountLabel.setVisible(false);
            capstoneImage.setVisible(false);
            capstoneCountLabel.setVisible(false);
        } else {
            // **After first two moves: players place their own pieces**
            targetPlayer = takGame.getCurrentPlayer();
            // Show standing stone and capstone
            standingStoneImage.setVisible(true);
            standingStoneCountLabel.setVisible(true);
            capstoneImage.setVisible(true);
            capstoneCountLabel.setVisible(true);
        }

        // Update normal stone color
        normalStoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("normal", currentColor)));
        normalStoneImage.invalidate();

        // Update standing stone and capstone colors only if they are visible
        if (takGame.getMoveCount() >= 2) {
            standingStoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("standing", currentColor)));
            standingStoneImage.invalidate();

            capstoneImage.setDrawable(new TextureRegionDrawable(createColoredPlaceholder("capstone", currentColor)));
            capstoneImage.invalidate();
        }

        // **Update the piece count labels with only the number**
        normalStoneCountLabel.setText(String.valueOf(targetPlayer.getRemainingPieces(Piece.PieceType.FLAT_STONE)));
        standingStoneCountLabel.setText(String.valueOf(targetPlayer.getRemainingPieces(Piece.PieceType.STANDING_STONE)));
        capstoneCountLabel.setText(String.valueOf(targetPlayer.getRemainingPieces(Piece.PieceType.CAPSTONE)));
    }

    /**
     * Updates the player scores displayed on the UI.
     */
    public void updatePlayerScores() {
        // Retrieve scores from players
        int playerBlackScore = takGame.getPlayer1().getScore();
        int playerWhiteScore = takGame.getPlayer2().getScore();

        // Update score labels
        playerBlackScoreLabel.setText("Score: " + playerBlackScore);
        playerWhiteScoreLabel.setText("Score: " + playerWhiteScore);
    }

    /**
     * Updates the moves list displayed on the UI.
     *
     * @param moveDescription The description of the move to add.
     */
    public void addMoveToList(String moveDescription) {
        movesArray.add(moveDescription);
        movesList.setItems(movesArray.toArray(new String[0]));
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
     * Creates a placeholder texture for a normal stone (circle).
     *
     * @return The generated texture.
     */
    private Texture createNormalStonePlaceholder() {
        return createNormalStonePlaceholderWithColor(Color.WHITE);
    }

    /**
     * Creates a placeholder texture for a standing stone (rectangle).
     *
     * @return The generated texture.
     */
    private Texture createStandingStonePlaceholder() {
        return createStandingStonePlaceholderWithColor(Color.WHITE);
    }

    /**
     * Creates a placeholder texture for a capstone (triangle).
     *
     * @return The generated texture.
     */
    private Texture createCapstonePlaceholder() {
        return createCapstonePlaceholderWithColor(Color.WHITE);
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
     * Creates a colored placeholder texture for a capstone (triangle).
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
        // Draw a simple triangle to represent the capstone
        pixmap.fillTriangle(size / 2, 2, 2, size - 2, size - 2, size - 2);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Disposes of rendering resources.
     */
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    /**
     * Returns the current stage.
     *
     * @return The stage.
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Returns the current skin.
     *
     * @return The skin.
     */
    public Skin getSkin() {
        return skin;
    }
}
