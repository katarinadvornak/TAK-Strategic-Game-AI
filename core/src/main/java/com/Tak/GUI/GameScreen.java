package com.Tak.GUI;

import com.Tak.Logic.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

import java.util.List;

public class GameScreen implements Screen {
    private TakGameMain game;
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ModelInstance boardInstance;
    private Model boardModel;
    private Model flatStoneModel;
    private Model standingStoneModel;
    private Model capstoneModel;
    private Array<ModelInstance> pieceInstances;
    private TakGame takGame;
    private CameraInputController camController;

    private int boardSize = 5; // You can change this to other sizes (e.g., 3, 4, 5, 6, 8)

    public GameScreen(TakGameMain game) {
        this.game = game;
        create();
    }

    public void create() {
        // Initialize the game logic
        takGame = new TakGame(boardSize);

        // Set up the camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(boardSize * 1.5f, boardSize * 1.5f, boardSize * 1.5f);
        camera.lookAt(boardSize / 2f, 0, boardSize / 2f);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        // Camera controller for input
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(new InputMultiplexer(camController, new InputHandler()));

        // Initialize ModelBatch and Environment
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // Create the game board model
        ModelBuilder modelBuilder = new ModelBuilder();
        boardModel = modelBuilder.createBox(boardSize, 0.2f, boardSize,
            new Material(ColorAttribute.createDiffuse(Color.BROWN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        boardInstance = new ModelInstance(boardModel);
        boardInstance.transform.setToTranslation(boardSize / 2f, -0.1f, boardSize / 2f);

        // Create models for the pieces
        flatStoneModel = modelBuilder.createCylinder(0.8f, 0.2f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        standingStoneModel = modelBuilder.createCylinder(0.8f, 1f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        capstoneModel = modelBuilder.createCone(0.8f, 0.8f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        pieceInstances = new Array<>();

        // Initial rendering of the board and pieces
        updatePieceInstances();
    }

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
    }

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
    }

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

    @Override
    public void resize(int width, int height) {
        // Update camera viewport
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        boardModel.dispose();
        flatStoneModel.dispose();
        standingStoneModel.dispose();
        capstoneModel.dispose();
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    // Input handling for placing pieces and moving stacks
    private class InputHandler extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return false;

            // Convert screen coordinates to world coordinates
            Ray pickRay = camera.getPickRay(screenX, screenY);
            Vector3 intersection = new Vector3();

            if (Intersector.intersectRayPlane(pickRay, new Plane(new Vector3(0, 1, 0), 0), intersection)) {
                int x = Math.floorDiv((int) intersection.x, 1);
                int y = Math.floorDiv((int) intersection.z, 1);

                if (x >= 0 && x < boardSize && y >= 0 && y < boardSize) {
                    try {
                        // For simplicity, place a flat stone of the current player
                        Piece.PieceType pieceType = Piece.PieceType.FLAT_STONE;

                        // Place the piece
                        takGame.placePiece(x, y, pieceType);

                        // Update the rendering
                        updatePieceInstances();

                        // Check for game end
                        if (takGame.isGameEnded()) {
                            // Handle game end (e.g., display a message)
                            System.out.println("Game Over!");
                        }
                    } catch (InvalidMoveException e) {
                        // Handle invalid move (e.g., show a message)
                        System.out.println("Invalid Move: " + e.getMessage());
                    } catch (GameOverException e) {
                        // Handle game over
                        System.out.println("Game Over: " + e.getMessage());
                    }
                }
            }
            return true;
        }
    }
}
