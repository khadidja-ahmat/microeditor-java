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

    public WhiteboardView(DrawingDocument document, Selection selection) {
        this.document = Objects.requireNonNull(document);
        this.selection = Objects.requireNonNull(selection);
        this.figureNodeFactory = new FigureNodeFactory(selection, this::installFigureMouseHandlers);
        this.dragDrop = new WhiteboardDragDrop(selection, this::render);
        this.whiteboardSelection = new WhiteboardSelection(selection, figureNodes, this::render);
        configureWhiteboardPane();
    }

    public Pane getPane() {
        return whiteboardPane;
    }

    public void setDocument(DrawingDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    public void setToolDropHandler(ToolDropHandler toolDropHandler) {
        dragDrop.setToolDropHandler(toolDropHandler);
    }

    public void setMoveHandler(MoveHandler moveHandler) {
        dragDrop.setMoveHandler(moveHandler);
    }

    public void setToolbarDropChecker(ToolbarDropChecker toolbarDropChecker) {
        dragDrop.setToolbarDropChecker(toolbarDropChecker);
    }

    public void setTrashDropChecker(ToolbarDropChecker trashDropChecker) {
        dragDrop.setTrashDropChecker(trashDropChecker);
    }

    public void setAddSelectionToToolbarAction(Runnable addSelectionToToolbarAction) {
        dragDrop.setAddSelectionToToolbarAction(addSelectionToToolbarAction);
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
        dragDrop.setDeleteAction(deleteAction);
    }

    public void render() {
        whiteboardPane.getChildren().clear();
        figureNodes.clear();
        for (Figure figure : document.getFigures()) {
            Node figureNode = figureNodeFactory.createFigureNode(figure);
            figureNodes.put(figure, figureNode);
            whiteboardPane.getChildren().add(figureNode);
        }
        whiteboardSelection.addMultiSelectionOutline();
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
