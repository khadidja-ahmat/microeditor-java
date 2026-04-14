package microeditor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
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

/**
 * JavaFX view showing reusable figure templates and the trash zone.
 */
public final class ToolPaletteView {
    public static final String TOOL_PREFIX = "toolbar:";
    private static final double ICON_BOX_SIZE = 34.0;
    private static final double ICON_MAX_SIZE = 26.0;

    private final VBox palettePane;
    private final VBox toolsPane = new VBox(12);
    private final Pane trashPane;
    private final ToolPaletteDragDrop dragDrop;
    private Toolbar toolbar;

    /**
     * Creates a tool palette view.
     *
     * @param toolbar toolbar model rendered by the palette
     * @param deleteSelectionAction action triggered by the trash zone
     * @param removeToolAction action triggered when a template is removed
     */
    public ToolPaletteView(Toolbar toolbar, Runnable deleteSelectionAction, IntConsumer removeToolAction) {
        this.toolbar = Objects.requireNonNull(toolbar);
        Objects.requireNonNull(deleteSelectionAction);
        this.dragDrop = new ToolPaletteDragDrop(removeToolAction);
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

    /**
     * Returns the JavaFX container used by this view.
     *
     * @return palette pane
     */
    public VBox getPane() {
        return palettePane;
    }

    /**
     * Replaces the toolbar model rendered by this view.
     *
     * @param toolbar new toolbar model
     */
    public void setToolbar(Toolbar toolbar) {
        this.toolbar = Objects.requireNonNull(toolbar);
        render();
    }

    /**
     * Checks whether a screen point is inside the palette.
     *
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return {@code true} when the point is inside the palette
     */
    public boolean containsScreenPoint(double screenX, double screenY) {
        Bounds screenBounds = palettePane.localToScreen(palettePane.getBoundsInLocal());
        return screenBounds != null && screenBounds.contains(screenX, screenY);
    }

    /**
     * Checks whether a screen point is inside the trash zone.
     *
     * @param screenX screen X coordinate
     * @param screenY screen Y coordinate
     * @return {@code true} when the point is inside the trash zone
     */
    public boolean trashContainsScreenPoint(double screenX, double screenY) {
        Bounds screenBounds = trashPane.localToScreen(trashPane.getBoundsInLocal());
        return screenBounds != null && screenBounds.contains(screenX, screenY);
    }

    /**
     * Rebuilds the palette icons from the toolbar model.
     */
    public void render() {
        toolsPane.getChildren().clear();
        for (int index = 0; index < toolbar.getFigures().size(); index++) {
            int toolIndex = index;
            Node icon = createFigureIcon(toolbar.getFigures().get(index));
            dragDrop.installToolHandlers(icon, toolIndex);
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
        FigureBounds bounds = calculateGroupBounds(groupFigure);
        if (bounds == null || bounds.width() == 0.0 || bounds.height() == 0.0) {
            return createDefaultIcon(groupFigure);
        }

        double scale = ICON_MAX_SIZE / Math.max(bounds.width(), bounds.height());
        for (Figure child : groupFigure.getChildren()) {
            groupIcon.getChildren().add(createScaledFigureIcon(child, bounds.minX(), bounds.minY(), scale));
        }
        return groupIcon;
    }

    private Node createScaledFigureIcon(Figure figure, double minX, double minY, double scale) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            Rectangle rectangleIcon = new Rectangle(
                    (rectangleFigure.getX() + rectangleFigure.getTranslateX() - minX) * scale,
                    (rectangleFigure.getY() + rectangleFigure.getTranslateY() - minY) * scale,
                    rectangleFigure.getWidth() * scale,
                    rectangleFigure.getHeight() * scale);
            rectangleIcon.setArcWidth(rectangleFigure.getCornerRadius() * scale);
            rectangleIcon.setArcHeight(rectangleFigure.getCornerRadius() * scale);
            rectangleIcon.setFill(Color.web(rectangleFigure.getColor()));
            rectangleIcon.setStroke(Color.web("#34689f"));
            return rectangleIcon;
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            Polygon polygonIcon = new Polygon();
            double centerX = polygonFigure.getX() + polygonFigure.getTranslateX();
            double centerY = polygonFigure.getY() + polygonFigure.getTranslateY();
            double radius = polygonFigure.getSideLength();
            for (int sideIndex = 0; sideIndex < polygonFigure.getSideCount(); sideIndex++) {
                double angle = -Math.PI / 2.0 + (2.0 * Math.PI * sideIndex / polygonFigure.getSideCount());
                polygonIcon.getPoints().add((centerX + Math.cos(angle) * radius - minX) * scale);
                polygonIcon.getPoints().add((centerY + Math.sin(angle) * radius - minY) * scale);
            }
            polygonIcon.setFill(Color.web(polygonFigure.getColor()));
            polygonIcon.setStroke(Color.web("#34689f"));
            return polygonIcon;
        }
        if (figure instanceof GroupFigure nestedGroupFigure) {
            Group nestedGroupIcon = new Group();
            for (Figure child : nestedGroupFigure.getChildren()) {
                nestedGroupIcon.getChildren().add(createScaledFigureIcon(child, minX, minY, scale));
            }
            return nestedGroupIcon;
        }
        return createDefaultIcon(figure);
    }

    private FigureBounds calculateGroupBounds(GroupFigure groupFigure) {
        FigureBounds bounds = null;
        for (Figure child : groupFigure.getChildren()) {
            FigureBounds childBounds = calculateFigureBounds(child);
            bounds = bounds == null ? childBounds : bounds.union(childBounds);
        }
        return bounds;
    }

    private FigureBounds calculateFigureBounds(Figure figure) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            double minX = rectangleFigure.getX() + rectangleFigure.getTranslateX();
            double minY = rectangleFigure.getY() + rectangleFigure.getTranslateY();
            return new FigureBounds(minX, minY, minX + rectangleFigure.getWidth(), minY + rectangleFigure.getHeight());
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            double centerX = polygonFigure.getX() + polygonFigure.getTranslateX();
            double centerY = polygonFigure.getY() + polygonFigure.getTranslateY();
            double radius = polygonFigure.getSideLength();

            for (int sideIndex = 0; sideIndex < polygonFigure.getSideCount(); sideIndex++) {
                double angle = -Math.PI / 2.0 + (2.0 * Math.PI * sideIndex / polygonFigure.getSideCount());
                double x = centerX + Math.cos(angle) * radius;
                double y = centerY + Math.sin(angle) * radius;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
            return new FigureBounds(minX, minY, maxX, maxY);
        }
        if (figure instanceof GroupFigure nestedGroupFigure) {
            return calculateGroupBounds(nestedGroupFigure);
        }
        return new FigureBounds(
                figure.getX() + figure.getTranslateX(),
                figure.getY() + figure.getTranslateY(),
                figure.getX() + figure.getTranslateX(),
                figure.getY() + figure.getTranslateY());
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
        dragDrop.installTrashDrop(trashPane);

        Node trashIcon = ToolbarIcons.trashIcon();
        trashIcon.setScaleX(1.15);
        trashIcon.setScaleY(1.15);
        trashIcon.setLayoutX(8);
        trashIcon.setLayoutY(8);
        trashPane.getChildren().add(trashIcon);
        return trashPane;
    }

    private record FigureBounds(double minX, double minY, double maxX, double maxY) {
        double width() {
            return maxX - minX;
        }

        double height() {
            return maxY - minY;
        }

        FigureBounds union(FigureBounds other) {
            if (other == null) {
                return this;
            }
            return new FigureBounds(
                    Math.min(minX, other.minX),
                    Math.min(minY, other.minY),
                    Math.max(maxX, other.maxX),
                    Math.max(maxY, other.maxY));
        }
    }
}
