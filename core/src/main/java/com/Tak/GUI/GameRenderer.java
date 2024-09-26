package com.Tak.GUI;

import com.Tak.Logic.Board;
import com.Tak.Logic.Piece;
import com.Tak.Logic.Player;
import com.Tak.Logic.TakGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.List;

/**
 * The GameRenderer class is responsible for rendering the 3D board and pieces.
 */
public class GameRenderer {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    public ModelInstance boardInstance;
    private Model boardModel;

    private Model flatStoneModel, standingStoneModel, capstoneModel;
    private Array<ModelInstance> pieceInstances;

    private int boardSize;
    private TakGame takGame;

    public ModelInstance hoverOutlineInstance;
    public ModelInstance selectionHighlightInstance;

    public GameRenderer(PerspectiveCamera camera, int boardSize, TakGame takGame) {
        this.camera = camera;
        this.boardSize = boardSize;
        this.takGame = takGame;
        initializeRendering();
    }

    private void initializeRendering() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();

        // Load the board texture with grid
        Texture boardTexture;
        try {
            boardTexture = new Texture(Gdx.files.internal("board_texture.png")); // Ensure board_texture.png is in assets
        } catch (Exception e) {
            Gdx.app.error("GameRenderer", "Failed to load board_texture.png", e);
            throw new GdxRuntimeException("Missing asset: board_texture.png");
        }
        Material boardMaterial = new Material(TextureAttribute.createDiffuse(boardTexture));

        // Create the game board model with texture
        boardModel = modelBuilder.createBox(boardSize, 0.2f, boardSize,
            boardMaterial,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        boardInstance = new ModelInstance(boardModel);
        boardInstance.transform.setToTranslation(boardSize / 2f, -0.1f, boardSize / 2f);

        // Create models for the pieces
        flatStoneModel = modelBuilder.createCylinder(0.8f, 0.2f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        standingStoneModel = modelBuilder.createBox(0.8f, 1f, 0.8f,
            new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        capstoneModel = modelBuilder.createCone(0.8f, 1f, 0.8f, 32,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        pieceInstances = new Array<>();
    }

    /**
     * Renders the board and pieces.
     */
    public void render() {
        modelBatch.begin(camera);
        modelBatch.render(boardInstance, environment);
        for (ModelInstance instance : pieceInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
    }

    /**
     * Updates the rendering instances of all pieces on the board.
     */
    public void updatePieceInstances() {
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

        // Re-add hover outline if it exists
        if (hoverOutlineInstance != null) {
            pieceInstances.add(hoverOutlineInstance);
        }

        // Re-add selection highlight if it exists
        if (selectionHighlightInstance != null) {
            pieceInstances.add(selectionHighlightInstance);
        }
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
                return 0.5f;
            case CAPSTONE:
                return 0.8f;
            default:
                return 0.2f;
        }
    }

    /**
     * Creates a hover outline at the specified board position.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void createHoverOutline(int x, int y) {
        removeHoverOutline();
        ModelBuilder modelBuilder = new ModelBuilder();
        Material outlineMaterial = new Material(ColorAttribute.createDiffuse(new Color(0, 0, 1, 0.1f)));
        outlineMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        Model outlineModel = modelBuilder.createBox(1.0f, 0.05f, 1.0f,
            outlineMaterial,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        hoverOutlineInstance = new ModelInstance(outlineModel);
        hoverOutlineInstance.transform.setToTranslation(x + 0.5f, 0.1f, y + 0.5f);
        pieceInstances.add(hoverOutlineInstance);
    }

    /**
     * Removes the hover outline.
     */
    public void removeHoverOutline() {
        if (hoverOutlineInstance != null) {
            pieceInstances.removeValue(hoverOutlineInstance, true);
            if (hoverOutlineInstance.model != null) {
                hoverOutlineInstance.model.dispose();
            }
            hoverOutlineInstance = null;
        }
    }

    /**
     * Highlights the selected square.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void highlightSquare(int x, int y) {
        removeHighlight();
        ModelBuilder modelBuilder = new ModelBuilder();
        Material highlightMaterial = new Material(ColorAttribute.createDiffuse(new Color(1, 1, 0, 0.5f)));
        highlightMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        Model highlightModel = modelBuilder.createBox(1.0f, 0.05f, 1.0f,
            highlightMaterial,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        selectionHighlightInstance = new ModelInstance(highlightModel);
        selectionHighlightInstance.transform.setToTranslation(x + 0.5f, 0.1f, y + 0.5f);
        pieceInstances.add(selectionHighlightInstance);
    }

    /**
     * Removes the selection highlight.
     */
    public void removeHighlight() {
        if (selectionHighlightInstance != null) {
            pieceInstances.removeValue(selectionHighlightInstance, true);
            if (selectionHighlightInstance.model != null) {
                selectionHighlightInstance.model.dispose();
            }
            selectionHighlightInstance = null;
        }
    }

    /**
     * Disposes of rendering resources.
     */
    public void dispose() {
        modelBatch.dispose();
        boardModel.dispose();
        flatStoneModel.dispose();
        standingStoneModel.dispose();
        capstoneModel.dispose();

        // Dispose hover outline model
        if (hoverOutlineInstance != null && hoverOutlineInstance.model != null) {
            hoverOutlineInstance.model.dispose();
        }

        // Dispose selection highlight model
        if (selectionHighlightInstance != null && selectionHighlightInstance.model != null) {
            selectionHighlightInstance.model.dispose();
        }
    }
}
