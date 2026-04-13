package microeditor.ui;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import microeditor.model.Figure;
import microeditor.model.Selection;

import java.util.Map;
import java.util.Objects;

final class WhiteboardSelection {
    private final Selection selection;
    private final Map<Figure, Node> figureNodes;
    private final Runnable renderAction;

    private Pane whiteboardPane;
    private double selectionStartX;
    private double selectionStartY;
    private Rectangle selectionRectangle;
    private boolean selectionRectangleFinished;

    WhiteboardSelection(Selection selection, Map<Figure, Node> figureNodes, Runnable renderAction) {
        this.selection = Objects.requireNonNull(selection);
        this.figureNodes = Objects.requireNonNull(figureNodes);
        this.renderAction = Objects.requireNonNull(renderAction);
    }

    void installOn(Pane whiteboardPane) {
        this.whiteboardPane = Objects.requireNonNull(whiteboardPane);
        whiteboardPane.setOnMouseClicked(event -> {
            if (selectionRectangleFinished) {
                selectionRectangleFinished = false;
                event.consume();
                return;
            }
            if (event.getButton() == MouseButton.PRIMARY) {
                selection.clear();
                renderAction.run();
            }
        });
        whiteboardPane.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getTarget() == whiteboardPane) {
                startSelectionRectangle(event.getX(), event.getY(), event.isControlDown());
                event.consume();
            }
        });
        whiteboardPane.setOnMouseDragged(event -> {
            if (selectionRectangle != null) {
                updateSelectionRectangle(event.getX(), event.getY());
                event.consume();
            }
        });
        whiteboardPane.setOnMouseReleased(event -> {
            if (selectionRectangle != null) {
                selectFiguresInsideRectangle();
                selectionRectangle = null;
                selectionRectangleFinished = true;
                renderAction.run();
                event.consume();
            }
        });
    }

    void addMultiSelectionOutline() {
        if (selection.getSelectedFigures().size() < 2) {
            return;
        }

        Bounds selectedBounds = null;
        for (Figure figure : selection.getSelectedFigures()) {
            Node figureNode = figureNodes.get(figure);
            if (figureNode == null) {
                continue;
            }
            Bounds bounds = figureNode.getBoundsInParent();
            selectedBounds = selectedBounds == null ? bounds : union(selectedBounds, bounds);
        }

        if (selectedBounds != null) {
            whiteboardPane.getChildren().add(createSelectionOutline(selectedBounds));
        }
    }

    private void startSelectionRectangle(double x, double y, boolean keepCurrentSelection) {
        selectionStartX = x;
        selectionStartY = y;
        if (!keepCurrentSelection) {
            selection.clear();
        }

        selectionRectangle = createSelectionRectangle(x, y, 0, 0);
        whiteboardPane.getChildren().add(selectionRectangle);
    }

    private void updateSelectionRectangle(double x, double y) {
        selectionRectangle.setX(Math.min(selectionStartX, x));
        selectionRectangle.setY(Math.min(selectionStartY, y));
        selectionRectangle.setWidth(Math.abs(x - selectionStartX));
        selectionRectangle.setHeight(Math.abs(y - selectionStartY));
    }

    private void selectFiguresInsideRectangle() {
        Bounds selectionBounds = selectionRectangle.getBoundsInParent();
        for (Map.Entry<Figure, Node> entry : figureNodes.entrySet()) {
            if (entry.getValue().getBoundsInParent().intersects(selectionBounds)) {
                selection.select(entry.getKey());
            }
        }
    }

    private Rectangle createSelectionOutline(Bounds selectedBounds) {
        return createSelectionRectangle(
                selectedBounds.getMinX() - 6.0,
                selectedBounds.getMinY() - 6.0,
                selectedBounds.getWidth() + 12.0,
                selectedBounds.getHeight() + 12.0);
    }

    private Rectangle createSelectionRectangle(double x, double y, double width, double height) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setFill(Color.web("#4A90E21A"));
        rectangle.setStroke(Color.web("#4A90E2"));
        rectangle.setStrokeWidth(1.5);
        rectangle.getStrokeDashArray().addAll(7.0, 5.0);
        rectangle.setMouseTransparent(true);
        return rectangle;
    }

    private Bounds union(Bounds firstBounds, Bounds secondBounds) {
        double minX = Math.min(firstBounds.getMinX(), secondBounds.getMinX());
        double minY = Math.min(firstBounds.getMinY(), secondBounds.getMinY());
        double maxX = Math.max(firstBounds.getMaxX(), secondBounds.getMaxX());
        double maxY = Math.max(firstBounds.getMaxY(), secondBounds.getMaxY());
        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
}
