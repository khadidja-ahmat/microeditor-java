package microeditor.ui;

import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import microeditor.model.Figure;
import microeditor.model.Selection;

import java.util.Objects;

final class WhiteboardDragDrop {
    private final Selection selection;
    private final Runnable renderAction;

    private WhiteboardView.ToolDropHandler toolDropHandler = (toolName, x, y) -> { };
    private WhiteboardView.MoveHandler moveHandler = (deltaX, deltaY) -> { };
    private WhiteboardView.ToolbarDropChecker toolbarDropChecker = (screenX, screenY) -> false;
    private WhiteboardView.ToolbarDropChecker trashDropChecker = (screenX, screenY) -> false;
    private Runnable addSelectionToToolbarAction = () -> { };
    private Runnable deleteAction = () -> { };

    WhiteboardDragDrop(Selection selection, Runnable renderAction) {
        this.selection = Objects.requireNonNull(selection);
        this.renderAction = Objects.requireNonNull(renderAction);
    }

    void installOn(Pane whiteboardPane) {
        whiteboardPane.setOnDragOver(this::acceptToolbarDrag);
        whiteboardPane.setOnDragDropped(this::dropToolOnWhiteboard);
    }

    void setToolDropHandler(WhiteboardView.ToolDropHandler toolDropHandler) {
        this.toolDropHandler = Objects.requireNonNull(toolDropHandler);
    }

    void setMoveHandler(WhiteboardView.MoveHandler moveHandler) {
        this.moveHandler = Objects.requireNonNull(moveHandler);
    }

    void setToolbarDropChecker(WhiteboardView.ToolbarDropChecker toolbarDropChecker) {
        this.toolbarDropChecker = Objects.requireNonNull(toolbarDropChecker);
    }

    void setTrashDropChecker(WhiteboardView.ToolbarDropChecker trashDropChecker) {
        this.trashDropChecker = Objects.requireNonNull(trashDropChecker);
    }

    void setAddSelectionToToolbarAction(Runnable addSelectionToToolbarAction) {
        this.addSelectionToToolbarAction = Objects.requireNonNull(addSelectionToToolbarAction);
    }

    void setDeleteAction(Runnable deleteAction) {
        this.deleteAction = Objects.requireNonNull(deleteAction);
    }

    void finishFigureDrag(
            Figure figure,
            double screenX,
            double screenY,
            double sceneX,
            double sceneY,
            double startSceneX,
            double startSceneY) {
        if (!selection.isSelected(figure)) {
            return;
        }
        if (trashDropChecker.isOverToolbar(screenX, screenY)) {
            deleteAction.run();
            return;
        }
        if (toolbarDropChecker.isOverToolbar(screenX, screenY)) {
            addSelectionToToolbarAction.run();
            renderAction.run();
            return;
        }

        double deltaX = sceneX - startSceneX;
        double deltaY = sceneY - startSceneY;
        if (deltaX != 0.0 || deltaY != 0.0) {
            moveHandler.moveSelectedFigures(deltaX, deltaY);
        }
        renderAction.run();
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
}
