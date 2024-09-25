package com.Tak.GUI;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The MainMenuScreen class represents the main menu of the game.
 * It allows the player to start a new game or exit the application.
 */
public class MainMenuScreen implements Screen {
    private TakGameMain game; // Reference to the main game class
    private Stage stage;
    private Skin skin;
    private TextButton startGameButton;
    private TextButton exitButton;

    public MainMenuScreen(TakGameMain game) {
        this.game = game;
        create();
    }

    /**
     * Initializes the main menu screen, including UI elements.
     */
    public void create() {
        // Set up the stage and skin
        stage = new Stage(new ScreenViewport());
        skin = createBasicSkin();

        // Create buttons
        startGameButton = new TextButton("Start Game", skin);
        exitButton = new TextButton("Exit", skin);

        // Set up the table layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Add buttons to the table
        table.add(startGameButton).width(200).height(60).pad(10);
        table.row();
        table.add(exitButton).width(200).height(60).pad(10);

        // Add the table to the stage
        stage.addActor(table);

        // Set input processor
        Gdx.input.setInputProcessor(stage);

        // Add listeners to buttons
        addButtonListeners();
    }

    /**
     * Adds listeners to the Start Game and Exit buttons.
     */
    private void addButtonListeners() {
        // Listener for Start Game button
        startGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showGameModeSelection();
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
     * Displays a dialog for selecting the game mode (Human vs. Human or Human vs. AI).
     */
    private void showGameModeSelection() {
        Dialog dialog = new Dialog("Select Game Mode", skin, "dialog") {
            @Override
            protected void result(Object object) {
                if (object.equals("human")) {
                    game.setScreen(new GameScreen(game)); // Start the GameScreen
                } else if (object.equals("ai")) {
                    // Currently not implemented
                    showNotImplementedDialog();
                }
            }
        };
        dialog.text("Choose an option:");
        dialog.button("Play against Human", "human");
        dialog.button("Play against AI", "ai");
        dialog.show(stage);
    }

    /**
     * Displays a dialog indicating that the selected feature is not yet implemented.
     */
    private void showNotImplementedDialog() {
        Dialog dialog = new Dialog("Not Implemented", skin, "dialog");
        dialog.text("This feature is not yet implemented.");
        dialog.button("OK");
        dialog.show(stage);
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
        skin.add("default-font", font);

        // Create button textures
        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GRAY);
        pixmap.fill();
        Texture buttonTexture = new Texture(pixmap);
        skin.add("button-up", buttonTexture);
        pixmap.dispose();

        // TextButton style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button-up");
        textButtonStyle.down = skin.newDrawable("button-up", Color.DARK_GRAY);
        textButtonStyle.font = skin.getFont("default-font");
        skin.add("default", textButtonStyle);

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        skin.add("default", labelStyle);

        // Window style (used for Dialog)
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("button-up", Color.DARK_GRAY);
        windowStyle.titleFont = skin.getFont("default-font");
        skin.add("dialog", windowStyle);

        return skin;
    }

    @Override
    public void show() {
        // Called when this screen becomes the current screen
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update and draw the stage
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Called when the application is paused
    }

    @Override
    public void resume() {
        // Called when the application is resumed
    }

    @Override
    public void hide() {
        // Called when this screen is no longer the current screen
    }

    @Override
    public void dispose() {
        // Dispose of assets
        stage.dispose();
        skin.dispose();
    }
}
