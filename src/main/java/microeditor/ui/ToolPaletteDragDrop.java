package microeditor.ui;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.function.IntConsumer;

final class ToolPaletteDragDrop {
    private final IntConsumer removeToolAction;

    ToolPaletteDragDrop(IntConsumer removeToolAction) {
        this.removeToolAction = Objects.requireNonNull(removeToolAction);
    }

    void installToolHandlers(Node toolIcon, int toolIndex) {
        installDrag(toolIcon, ToolPaletteView.TOOL_PREFIX + toolIndex);
        installRemovalMenu(toolIcon, toolIndex);
    }

    void installTrashDrop(Pane trashPane) {
        trashPane.setOnDragOver(this::acceptToolbarToolDrop);
        trashPane.setOnDragDropped(this::dropToolbarToolOnTrash);
    }

    private void installDrag(Node toolIcon, String toolName) {
        toolIcon.setOnDragDetected(event -> {
            Dragboard dragboard = toolIcon.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(toolName);
            dragboard.setContent(content);
            event.consume();
        });
    }

    private void installRemovalMenu(Node toolIcon, int toolIndex) {
        toolIcon.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                MenuItem removeItem = new MenuItem("Remove from toolbar");
                removeItem.setOnAction(actionEvent -> removeToolAction.accept(toolIndex));
                new ContextMenu(removeItem).show(toolIcon, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });
    }

    private void acceptToolbarToolDrop(DragEvent event) {
        if (isToolbarToolDrag(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    private void dropToolbarToolOnTrash(DragEvent event) {
        boolean dropCompleted = false;
        Dragboard dragboard = event.getDragboard();
        if (isToolbarToolDrag(dragboard)) {
            removeToolAction.accept(parseToolIndex(dragboard.getString()));
            dropCompleted = true;
        }
        event.setDropCompleted(dropCompleted);
        event.consume();
    }

    private boolean isToolbarToolDrag(Dragboard dragboard) {
        return dragboard.hasString() && dragboard.getString().startsWith(ToolPaletteView.TOOL_PREFIX);
    }

    private int parseToolIndex(String toolName) {
        return Integer.parseInt(toolName.substring(ToolPaletteView.TOOL_PREFIX.length()));
    }
}
