package microeditor.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;
import microeditor.model.Selection;

import java.util.Objects;

public final class WhiteboardView {
    public static final double WIDTH = 680.0;
    public static final double HEIGHT = 460.0;

    private final Pane whiteboardPane = new Pane();
    private final Selection selection;

    private DrawingDocument document;
    private ToolDropHandler toolDropHandler = (toolName, x, y) -> { };
    private MoveHandler moveHandler = (deltaX, deltaY) -> { };
    private ToolbarDropChecker toolbarDropChecker = (screenX, screenY) -> false;
    private ToolbarDropChecker trashDropChecker = (screenX, screenY) -> false;
    private Runnable addSelectionToToolbarAction = () -> { };
    private Runnable groupAction = () -> { };
    private Runnable ungroupAction = () -> { };
    private Runnable editAction = () -> { };
    private Runnable deleteAction = () -> { };
    private double mousePressedSceneX;
    private double mousePressedSceneY;

    public WhiteboardView(DrawingDocument document, Selection selection) {
        this.document = Objects.requireNonNull(document);
        this.selection = Objects.requireNonNull(selection);
        configureWhiteboardPane();
    }

    public Pane getPane() {
        return whiteboardPane;
    }

    public void setDocument(DrawingDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    public void setToolDropHandler(ToolDropHandler toolDropHandler) {
        this.toolDropHandler = Objects.requireNonNull(toolDropHandler);
    }

    public void setMoveHandler(MoveHandler moveHandler) {
        this.moveHandler = Objects.requireNonNull(moveHandler);
    }

    public void setToolbarDropChecker(ToolbarDropChecker toolbarDropChecker) {
        this.toolbarDropChecker = Objects.requireNonNull(toolbarDropChecker);
    }

    public void setTrashDropChecker(ToolbarDropChecker trashDropChecker) {
        this.trashDropChecker = Objects.requireNonNull(trashDropChecker);
    }

    public void setAddSelectionToToolbarAction(Runnable addSelectionToToolbarAction) {
        this.addSelectionToToolbarAction = Objects.requireNonNull(addSelectionToToolbarAction);
    }

    public void setGroupAction(Runnable groupAction) {
        this.groupAction = Objects.requireNonNull(groupAction);
    }

    public void setUngroupAction(Runnable ungroupAction) {
        this.ungroupAction = Objects.requireNonNull(ungroupAction);
    }

    public void setEditAction(Runnable editAction) {
        this.editAction = Objects.requireNonNull(editAction);
    }

    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = Objects.requireNonNull(deleteAction);
    }

    public void render() {
        whiteboardPane.getChildren().clear();
        for (Figure figure : document.getFigures()) {
            whiteboardPane.getChildren().add(createFigureNode(figure, true, false));
        }
    }

    private void configureWhiteboardPane() {
        whiteboardPane.setPrefSize(WIDTH, HEIGHT);
        whiteboardPane.setMinSize(WIDTH, HEIGHT);
        whiteboardPane.setStyle("-fx-background-color: white; -fx-border-color: #bbbbbb;");

        whiteboardPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selection.clear();
                render();
            }
        });

        whiteboardPane.setOnDragOver(this::acceptToolbarDrag);
        whiteboardPane.setOnDragDropped(this::dropToolOnWhiteboard);
    }

    private void acceptToolbarDrag(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void dropToolOnWhiteboard(DragEvent event) {
        boolean dropCompleted = false;
        if (event.getDragboard().hasString()) {
            toolDropHandler.onToolDropped(event.getDragboard().getString(), event.getX(), event.getY());
            dropCompleted = true;
        }
        event.setDropCompleted(dropCompleted);
        event.consume();
    }

    private Node createFigureNode(Figure figure, boolean interactive, boolean highlighted) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            return createRectangleNode(rectangleFigure, interactive, highlighted);
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            return createPolygonNode(polygonFigure, interactive, highlighted);
        }
        if (figure instanceof GroupFigure groupFigure) {
            return createGroupNode(groupFigure, interactive, highlighted);
        }
        return new Group();
    }

    private Node createGroupNode(GroupFigure groupFigure, boolean interactive, boolean highlighted) {
        Group groupNode = new Group();
        boolean groupHighlighted = highlighted || selection.isSelected(groupFigure);
        for (Figure child : groupFigure.getChildren()) {
            groupNode.getChildren().add(createFigureNode(child, false, groupHighlighted));
        }
        if (interactive) {
            installFigureMouseHandlers(groupNode, groupFigure);
        }
        return groupNode;
    }

    private Rectangle createRectangleNode(RectangleFigure figure, boolean interactive, boolean highlighted) {
        Rectangle rectangleNode = new Rectangle(figure.getX(), figure.getY(), figure.getWidth(), figure.getHeight());
        rectangleNode.setArcWidth(figure.getCornerRadius());
        rectangleNode.setArcHeight(figure.getCornerRadius());
        styleShape(rectangleNode, figure, highlighted);
        if (interactive) {
            installFigureMouseHandlers(rectangleNode, figure);
        }
        return rectangleNode;
    }

    private Polygon createPolygonNode(PolygonFigure figure, boolean interactive, boolean highlighted) {
        Polygon polygonNode = new Polygon();
        double radius = figure.getSideLength();
        for (int sideIndex = 0; sideIndex < figure.getSideCount(); sideIndex++) {
            double angle = -Math.PI / 2.0 + (2.0 * Math.PI * sideIndex / figure.getSideCount());
            polygonNode.getPoints().add(figure.getX() + Math.cos(angle) * radius);
            polygonNode.getPoints().add(figure.getY() + Math.sin(angle) * radius);
        }
        styleShape(polygonNode, figure, highlighted);
        if (interactive) {
            installFigureMouseHandlers(polygonNode, figure);
        }
        return polygonNode;
    }

    private void styleShape(Shape shape, Figure figure, boolean highlighted) {
        boolean selected = highlighted || selection.isSelected(figure);
        shape.setFill(Color.web(figure.getColor()));
        shape.setTranslateX(figure.getTranslateX());
        shape.setTranslateY(figure.getTranslateY());
        shape.setRotate(figure.getRotation());
        shape.setStroke(selected ? Color.BLACK : Color.web("#555555"));
        shape.setStrokeWidth(selected ? 3.0 : 1.0);
    }

    private void installFigureMouseHandlers(Node figureNode, Figure figure) {
        figureNode.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                mousePressedSceneX = event.getSceneX();
                mousePressedSceneY = event.getSceneY();
                updateSelection(figure, event.isControlDown());
                event.consume();
            }
        });

        figureNode.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && selection.isSelected(figure)) {
                if (trashDropChecker.isOverToolbar(event.getScreenX(), event.getScreenY())) {
                    deleteAction.run();
                    event.consume();
                    return;
                }
                if (toolbarDropChecker.isOverToolbar(event.getScreenX(), event.getScreenY())) {
                    addSelectionToToolbarAction.run();
                    render();
                    event.consume();
                    return;
                }
                double deltaX = event.getSceneX() - mousePressedSceneX;
                double deltaY = event.getSceneY() - mousePressedSceneY;
                if (deltaX != 0.0 || deltaY != 0.0) {
                    moveHandler.moveSelectedFigures(deltaX, deltaY);
                }
                render();
                event.consume();
            }
        });

        figureNode.setOnContextMenuRequested(event -> {
            if (!selection.isSelected(figure)) {
                selection.clear();
                selection.select(figure);
                render();
            }
            createContextMenu().show(figureNode, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void updateSelection(Figure figure, boolean controlKeyPressed) {
        if (controlKeyPressed) {
            toggleSelection(figure);
        } else if (!selection.isSelected(figure)) {
            selection.clear();
            selection.select(figure);
        }
    }

    private void toggleSelection(Figure figure) {
        if (selection.isSelected(figure)) {
            selection.unselect(figure);
        } else {
            selection.select(figure);
        }
    }

    private ContextMenu createContextMenu() {
        MenuItem groupItem = new MenuItem("Group");
        groupItem.setOnAction(event -> groupAction.run());
        groupItem.setDisable(selection.getSelectedFigures().size() < 2);

        MenuItem ungroupItem = new MenuItem("De-group");
        ungroupItem.setOnAction(event -> ungroupAction.run());
        ungroupItem.setDisable(!singleSelectionIsGroup());

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(event -> editAction.run());
        editItem.setDisable(selection.getSelectedFigures().size() != 1);

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> deleteAction.run());
        deleteItem.setDisable(selection.getSelectedFigures().isEmpty());

        return new ContextMenu(groupItem, ungroupItem, editItem, deleteItem);
    }

    private boolean singleSelectionIsGroup() {
        if (selection.getSelectedFigures().size() != 1) {
            return false;
        }
        return selection.getSelectedFigures().iterator().next() instanceof GroupFigure;
    }

    @FunctionalInterface
    public interface ToolDropHandler {
        void onToolDropped(String toolName, double x, double y);
    }

    @FunctionalInterface
    public interface MoveHandler {
        void moveSelectedFigures(double deltaX, double deltaY);
    }

    @FunctionalInterface
    public interface ToolbarDropChecker {
        boolean isOverToolbar(double screenX, double screenY);
    }
}
