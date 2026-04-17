package microeditor.model;

import java.util.List;

/**
 * Common abstraction for objects that directly contain figures.
 */
public interface FigureContainer {
    /**
     * Adds a figure at the end of this container.
     *
     * @param figure figure to add
     */
    void addFigure(Figure figure);

    /**
     * Adds a figure at the requested index, clamped to the container bounds.
     *
     * @param index desired insertion index
     * @param figure figure to add
     */
    void addFigure(int index, Figure figure);

    /**
     * Removes a figure from this container.
     *
     * @param figure figure to remove
     */
    void removeFigure(Figure figure);

    /**
     * Returns the index of a directly contained figure.
     *
     * @param figure searched figure
     * @return index of the figure, or {@code -1} when absent
     */
    int indexOf(Figure figure);

    /**
     * Returns directly contained figures in drawing order.
     *
     * @return unmodifiable list of figures
     */
    List<Figure> getFigures();
}
