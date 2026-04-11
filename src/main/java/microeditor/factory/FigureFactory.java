package microeditor.factory;

import microeditor.model.Figure;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;

import java.util.UUID;

/**
 * Centralizes figure creation.
 * The UI should ask this factory for figures instead of calling constructors directly.
 */
public final class FigureFactory {
    private static final String DEFAULT_COLOR = "#4A90E2";
    private static final String RECTANGLE_ID_PREFIX = "rectangle";
    private static final String POLYGON_ID_PREFIX = "polygon";

    public RectangleFigure createRectangle(double x, double y, double width, double height) {
        return new RectangleFigure(newId(RECTANGLE_ID_PREFIX), x, y, width, height, DEFAULT_COLOR);
    }

    public PolygonFigure createPolygon(double x, double y, int sideCount, double sideLength) {
        return new PolygonFigure(newId(POLYGON_ID_PREFIX), x, y, sideCount, sideLength, DEFAULT_COLOR);
    }

    public Figure createFromPrototype(Figure prototype, double x, double y) {
        Figure clone = prototype.clone();
        clone.setPosition(x, y);
        return clone;
    }

    private String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
