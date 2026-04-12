package microeditor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;
import microeditor.model.Toolbar;

import java.util.Objects;
import java.util.function.IntConsumer;

public final class ToolPaletteView {
    public static final String TOOL_PREFIX = "toolbar:";
    private static final double ICON_BOX_SIZE = 34.0;
    private static final double ICON_MAX_SIZE = 26.0;

    private final VBox palettePane;
    private final VBox toolsPane = new VBox(12);
    private final Pane trashPane;
    private final IntConsumer removeToolAction;
    private Toolbar toolbar;

    public ToolPaletteView(Toolbar toolbar, Runnable deleteSelectionAction, IntConsumer removeToolAction) {
        this.toolbar = Objects.requireNonNull(toolbar);
        Objects.requireNonNull(deleteSelectionAction);
        this.removeToolAction = Objects.requireNonNull(removeToolAction);
        trashPane = createTrashPane(deleteSelectionAction);

        Region emptySpace = new Region();
        VBox.setVgrow(emptySpace, Priority.ALWAYS);

        configureToolsPane();
        palettePane = new VBox(8, toolsPane, emptySpace, trashPane);
        palettePane.setPadding(new Insets(8));
        palettePane.setPrefWidth(74);
        palettePane.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #cccccc;");
        render();
    }

    public VBox getPane() {
        return palettePane;
    }

    public void setToolbar(Toolbar toolbar) {
        this.toolbar = Objects.requireNonNull(toolbar);
        render();
    }

    public boolean containsScreenPoint(double screenX, double screenY) {
        Bounds screenBounds = palettePane.localToScreen(palettePane.getBoundsInLocal());
        return screenBounds != null && screenBounds.contains(screenX, screenY);
    }

    public boolean trashContainsScreenPoint(double screenX, double screenY) {
        Bounds screenBounds = trashPane.localToScreen(trashPane.getBoundsInLocal());
        return screenBounds != null && screenBounds.contains(screenX, screenY);
    }

    public void render() {
        toolsPane.getChildren().clear();
        for (int index = 0; index < toolbar.getFigures().size(); index++) {
            int toolIndex = index;
            Node icon = createFigureIcon(toolbar.getFigures().get(index));
            installDrag(icon, TOOL_PREFIX + toolIndex);
            installRemovalMenu(icon, toolIndex);
            toolsPane.getChildren().add(icon);
        }
    }

    private void configureToolsPane() {
        toolsPane.setAlignment(Pos.TOP_CENTER);
        toolsPane.setPadding(new Insets(10));
        toolsPane.setStyle("-fx-background-color: #eef5ff; -fx-border-color: #b7c7df;");
    }

    private Node createFigureIcon(Figure figure) {
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(ICON_BOX_SIZE, ICON_BOX_SIZE);

        if (figure instanceof RectangleFigure) {
            iconBox.getChildren().add(createRectangleIcon((RectangleFigure) figure));
            return iconBox;
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            iconBox.getChildren().add(createPolygonIcon(polygonFigure));
            return iconBox;
        }
        if (figure instanceof GroupFigure groupFigure) {
            iconBox.getChildren().add(createGroupIcon(groupFigure));
            return iconBox;
        }
        iconBox.getChildren().add(createDefaultIcon(figure));
        return iconBox;
    }

    private Node createRectangleIcon(RectangleFigure figure) {
        double width = figure.getWidth();
        double height = figure.getHeight();
        double scale = ICON_MAX_SIZE / Math.max(width, height);
        Rectangle rectangleIcon = new Rectangle(width * scale, height * scale);
        rectangleIcon.setArcWidth(figure.getCornerRadius() * scale);
        rectangleIcon.setArcHeight(figure.getCornerRadius() * scale);
        rectangleIcon.setFill(Color.web(figure.getColor()));
        rectangleIcon.setStroke(Color.web("#34689f"));
        return rectangleIcon;
    }

    private Node createPolygonIcon(PolygonFigure figure) {
        Polygon polygonIcon = new Polygon();
        double centerX = ICON_BOX_SIZE / 2.0;
        double centerY = ICON_BOX_SIZE / 2.0;
        double radius = ICON_MAX_SIZE / 2.0;
        for (int sideIndex = 0; sideIndex < figure.getSideCount(); sideIndex++) {
            double angle = -Math.PI / 2.0 + (2.0 * Math.PI * sideIndex / figure.getSideCount());
            polygonIcon.getPoints().add(centerX + Math.cos(angle) * radius);
            polygonIcon.getPoints().add(centerY + Math.sin(angle) * radius);
        }
        polygonIcon.setFill(Color.web(figure.getColor()));
        polygonIcon.setStroke(Color.web("#34689f"));
        return polygonIcon;
    }

    private Node createGroupIcon(GroupFigure groupFigure) {
        Group groupIcon = new Group();
        int childIndex = 0;
        for (Figure child : groupFigure.getChildren()) {
            if (childIndex == 3) {
                break;
            }
            Node childIcon = createSmallChildIcon(child);
            childIcon.setTranslateX(childIndex * 5.0);
            childIcon.setTranslateY(childIndex * -4.0);
            groupIcon.getChildren().add(childIcon);
            childIndex++;
        }
        return groupIcon;
    }

    private Node createSmallChildIcon(Figure figure) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            Rectangle rectangleIcon = new Rectangle(16, 10);
            rectangleIcon.setFill(Color.web(rectangleFigure.getColor()));
            rectangleIcon.setStroke(Color.web("#34689f"));
            return rectangleIcon;
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            Polygon polygonIcon = new Polygon();
            double radius = 8.0;
            for (int sideIndex = 0; sideIndex < polygonFigure.getSideCount(); sideIndex++) {
                double angle = -Math.PI / 2.0 + (2.0 * Math.PI * sideIndex / polygonFigure.getSideCount());
                polygonIcon.getPoints().add(Math.cos(angle) * radius);
                polygonIcon.getPoints().add(Math.sin(angle) * radius);
            }
            polygonIcon.setFill(Color.web(polygonFigure.getColor()));
            polygonIcon.setStroke(Color.web("#34689f"));
            return polygonIcon;
        }
        return createDefaultIcon(figure);
    }

    private Node createDefaultIcon(Figure figure) {
        Rectangle rectangleIcon = new Rectangle(18, 12);
        rectangleIcon.setFill(Color.web(figure.getColor()));
        rectangleIcon.setStroke(Color.web("#34689f"));
        return rectangleIcon;
    }

    private Pane createTrashPane(Runnable deleteSelectionAction) {
        Pane trashPane = new Pane();
        trashPane.setPrefSize(46, 46);
        trashPane.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #777777;");
        trashPane.setOnMouseClicked(event -> deleteSelectionAction.run());
        trashPane.setOnDragOver(this::acceptToolbarToolDrop);
        trashPane.setOnDragDropped(this::dropToolbarToolOnTrash);

        Node trashIcon = ToolbarIcons.trashIcon();
        trashIcon.setScaleX(1.15);
        trashIcon.setScaleY(1.15);
        trashIcon.setLayoutX(8);
        trashIcon.setLayoutY(8);
        trashPane.getChildren().add(trashIcon);
        return trashPane;
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
        return dragboard.hasString() && dragboard.getString().startsWith(TOOL_PREFIX);
    }

    private int parseToolIndex(String toolName) {
        return Integer.parseInt(toolName.substring(TOOL_PREFIX.length()));
    }
}
