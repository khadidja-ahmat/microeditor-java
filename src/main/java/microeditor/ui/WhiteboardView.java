package microeditor.ui;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;
import microeditor.model.Selection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JavaFX view responsible for rendering the drawing document on the whiteboard.
 */
public final class WhiteboardView {
    public static final double WIDTH = 680.0;
    public static final double HEIGHT = 460.0;

    private final Pane whiteboardPane = new Pane();
    private final Selection selection;
    private final FigureNodeFactory figureNodeFactory;
    private final WhiteboardDragDrop dragDrop;
    private final WhiteboardSelection whiteboardSelection;
    private final Map<Figure, Node> figureNodes = new HashMap<>();

    private DrawingDocument document;
    private Runnable groupAction = () -> { };
    private Runnable ungroupAction = () -> { };
    private Runnable editAction = () -> { };
    private Runnable deleteAction = () -> { };
    private double mousePressedSceneX;
    private double mousePressedSceneY;

    /**
     * Creates a whiteboard view bound to a document and a selection model.
     *
     * @param document drawing document to render
     * @param selection shared selection state
     */
    public WhiteboardView(DrawingDocument document, Selection selection) {
        this.document = Objects.requireNonNull(document);
        this.selection = Objects.requireNonNull(selection);
        this.figureNodeFactory = new FigureNodeFactory(selection, this::installFigureMouseHandlers);
        this.dragDrop = new WhiteboardDragDrop(selection, this::render);
        this.whiteboardSelection = new WhiteboardSelection(selection, figureNodes, this::render);
        configureWhiteboardPane();
    }

    /**
     * Returns the JavaFX pane used by this view.
     *
     * @return whiteboard pane
     */
    public Pane getPane() {
        return whiteboardPane;
    }

    /**
     * Replaces the document rendered by this view.
     *
     * @param document new drawing document
     */
    public void setDocument(DrawingDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    /**
     * Registers the action called when a toolbar item is dropped on the board.
     *
     * @param toolDropHandler drop handler
     */
    public void setToolDropHandler(ToolDropHandler toolDropHandler) {
        dragDrop.setToolDropHandler(toolDropHandler);
    }

    /**
     * Registers the action called after selected figures are moved.
     *
     * @param moveHandler move handler
     */
    public void setMoveHandler(MoveHandler moveHandler) {
        dragDrop.setMoveHandler(moveHandler);
    }

    /**
     * Registers the hit-test used to detect drops over the toolbar.
     *
     * @param toolbarDropChecker toolbar hit-test
     */
    public void setToolbarDropChecker(ToolbarDropChecker toolbarDropChecker) {
        dragDrop.setToolbarDropChecker(toolbarDropChecker);
    }

    /**
     * Registers the hit-test used to detect drops over the trash zone.
     *
     * @param trashDropChecker trash hit-test
     */
    public void setTrashDropChecker(ToolbarDropChecker trashDropChecker) {
        dragDrop.setTrashDropChecker(trashDropChecker);
    }

    /**
     * Registers the action that stores the current selection in the toolbar.
     *
     * @param addSelectionToToolbarAction action to run
     */
    public void setAddSelectionToToolbarAction(Runnable addSelectionToToolbarAction) {
        dragDrop.setAddSelectionToToolbarAction(addSelectionToToolbarAction);
    }

    /**
     * Registers the action that groups selected figures.
     *
     * @param groupAction action to run
     */
    public void setGroupAction(Runnable groupAction) {
        this.groupAction = Objects.requireNonNull(groupAction);
    }

    /**
     * Registers the action that ungroups the selected group.
     *
     * @param ungroupAction action to run
     */
    public void setUngroupAction(Runnable ungroupAction) {
        this.ungroupAction = Objects.requireNonNull(ungroupAction);
    }

    /**
     * Registers the action that edits the selected figure.
     *
     * @param editAction action to run
     */
    public void setEditAction(Runnable editAction) {
        this.editAction = Objects.requireNonNull(editAction);
    }

    /**
     * Registers the action that deletes selected figures.
     *
     * @param deleteAction action to run
     */
    public void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = Objects.requireNonNull(deleteAction);
        dragDrop.setDeleteAction(deleteAction);
    }

    /**
     * Rebuilds the visual nodes from the current document state.
     */
    public void render() {
        whiteboardPane.getChildren().clear();
        figureNodes.clear();
        for (Figure figure : document.getFigures()) {
            Node figureNode = figureNodeFactory.createFigureNode(figure);
            figureNodes.put(figure, figureNode);
            whiteboardPane.getChildren().add(figureNode);
        }
    }

    private void configureWhiteboardPane() {
        whiteboardPane.setPrefSize(WIDTH, HEIGHT);
        whiteboardPane.setMinSize(WIDTH, HEIGHT);
        whiteboardPane.setStyle("-fx-background-color: white; -fx-border-color: #bbbbbb;");

        whiteboardSelection.installOn(whiteboardPane);
        dragDrop.installOn(whiteboardPane);
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
            if (event.getButton() == MouseButton.PRIMARY) {
                dragDrop.finishFigureDrag(figure, event.getScreenX(), event.getScreenY(),
                        event.getSceneX(), event.getSceneY(), mousePressedSceneX, mousePressedSceneY);
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

    /**
     * Handles toolbar drops on the whiteboard.
     */
    @FunctionalInterface
    public interface ToolDropHandler {
        /**
         * Called when a toolbar item is dropped.
         *
         * @param toolName encoded toolbar item name
         * @param x board X coordinate
         * @param y board Y coordinate
         */
        void onToolDropped(String toolName, double x, double y);
    }

    /**
     * Handles movement of selected figures.
     */
    @FunctionalInterface
    public interface MoveHandler {
        /**
         * Moves the current selection by a delta.
         *
         * @param deltaX horizontal displacement
         * @param deltaY vertical displacement
         */
        void moveSelectedFigures(double deltaX, double deltaY);
    }

    /**
     * Tests whether a screen point is over a target zone.
     */
    @FunctionalInterface
    public interface ToolbarDropChecker {
        /**
         * Checks a screen point.
         *
         * @param screenX screen X coordinate
         * @param screenY screen Y coordinate
         * @return {@code true} when the point is over the zone
         */
        boolean isOverToolbar(double screenX, double screenY);
    }
}
