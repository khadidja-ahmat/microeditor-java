package microeditor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public final class ToolPaletteView {
    public static final String RECTANGLE_TOOL = "rectangle";
    public static final String POLYGON_TOOL = "polygon";

    private final VBox palettePane;

    public ToolPaletteView(Runnable deleteSelectionAction) {
        Objects.requireNonNull(deleteSelectionAction);
        VBox toolsPane = createToolsPane();
        Pane trashPane = createTrashPane(deleteSelectionAction);

        Region emptySpace = new Region();
        VBox.setVgrow(emptySpace, Priority.ALWAYS);

        palettePane = new VBox(8, toolsPane, emptySpace, trashPane);
        palettePane.setPadding(new Insets(8));
        palettePane.setPrefWidth(74);
        palettePane.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #cccccc;");
    }

    public VBox getPane() {
        return palettePane;
    }

    private VBox createToolsPane() {
        VBox toolsPane = new VBox(12, createRectangleIcon(), createPolygonIcon());
        toolsPane.setAlignment(Pos.TOP_CENTER);
        toolsPane.setPadding(new Insets(10));
        toolsPane.setStyle("-fx-background-color: #eef5ff; -fx-border-color: #b7c7df;");
        return toolsPane;
    }

    private Node createRectangleIcon() {
        Rectangle rectangleIcon = new Rectangle(22, 14);
        rectangleIcon.setFill(Color.web("#4A90E2"));
        rectangleIcon.setStroke(Color.web("#34689f"));
        installDrag(rectangleIcon, RECTANGLE_TOOL);
        return rectangleIcon;
    }

    private Node createPolygonIcon() {
        Polygon polygonIcon = new Polygon(
                11.0, 0.0,
                22.0, 8.0,
                18.0, 21.0,
                4.0, 21.0,
                0.0, 8.0);
        polygonIcon.setFill(Color.web("#4A90E2"));
        polygonIcon.setStroke(Color.web("#34689f"));
        installDrag(polygonIcon, POLYGON_TOOL);
        return polygonIcon;
    }

    private Pane createTrashPane(Runnable deleteSelectionAction) {
        Pane trashPane = new Pane();
        trashPane.setPrefSize(46, 46);
        trashPane.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #777777;");
        trashPane.setOnMouseClicked(event -> deleteSelectionAction.run());

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
            Dragboard dragboard = toolIcon.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(toolName);
            dragboard.setContent(content);
            event.consume();
        });
    }
}
