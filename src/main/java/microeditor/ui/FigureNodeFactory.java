package microeditor.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;
import microeditor.model.Selection;

import java.util.Objects;

final class FigureNodeFactory {
    private final Selection selection;
    private final FigureNodeHandler figureNodeHandler;

    FigureNodeFactory(Selection selection, FigureNodeHandler figureNodeHandler) {
        this.selection = Objects.requireNonNull(selection);
        this.figureNodeHandler = Objects.requireNonNull(figureNodeHandler);
    }

    Node createFigureNode(Figure figure) {
        return createFigureNode(figure, true, false);
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
            figureNodeHandler.install(groupNode, groupFigure);
        }
        return groupNode;
    }

    private Rectangle createRectangleNode(RectangleFigure figure, boolean interactive, boolean highlighted) {
        Rectangle rectangleNode = new Rectangle(figure.getX(), figure.getY(), figure.getWidth(), figure.getHeight());
        rectangleNode.setArcWidth(figure.getCornerRadius());
        rectangleNode.setArcHeight(figure.getCornerRadius());
        styleShape(rectangleNode, figure, highlighted);
        if (interactive) {
            figureNodeHandler.install(rectangleNode, figure);
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
            figureNodeHandler.install(polygonNode, figure);
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

    @FunctionalInterface
    interface FigureNodeHandler {
        void install(Node node, Figure figure);
    }
}
