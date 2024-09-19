package com.Tak.GUI;

import com.Tak.Logic.Board;
import com.Tak.Logic.Piece;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private int boardSize = 5;  // Define your board size (e.g., 5x5 grid)
    private Board board;
    private SpriteBatch batch;

    @Override
    public void show() {
        // Initialize camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600); // Set your game viewport size (e.g., 800x600)

        // Initialize shapeRenderer and batch
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        // Initialize the board
        board = new Board(boardSize);
    }

    @Override
    public void render(float delta) {
        // Clear the screen
        Gdx.gl.glClearColor(0, 0, 0, 1);  // Set the background color (black)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update the camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw the board and pieces
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);  // Start drawing shapes
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                // Draw the grid cells
                drawCell(x, y);

                // Draw pieces if present
                Piece piece = board.getPiece(x, y);
                if (piece != null) {
                    drawPiece(x, y, piece);
                }
            }
        }
        shapeRenderer.end();  // End drawing shapes
    }

    private void drawCell(int x, int y) {
        // Example: Draw a simple rectangle as the grid cell
        int cellSize = 100;  // Define cell size
        int margin = 10;     // Margin between cells
        shapeRenderer.setColor(Color.GRAY);  // Set the color for the cell
        shapeRenderer.rect(x * (cellSize + margin), y * (cellSize + margin), cellSize, cellSize);
    }

    private void drawPiece(int x, int y, Piece piece) {
        // Example: Draw a circle representing a piece
        int cellSize = 100;
        shapeRenderer.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);  // Set color based on piece
        shapeRenderer.circle(x * (cellSize + 10) + cellSize / 2, y * (cellSize + 10) + cellSize / 2, 40);  // Draw piece in the center of the cell
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Dispose resources
        shapeRenderer.dispose();
        batch.dispose();
    }
}
