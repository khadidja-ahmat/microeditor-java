package microeditor.model;

import microeditor.observer.ModelChangeType;
import microeditor.observer.ModelObserver;
import microeditor.observer.ObservableModel;
import microeditor.observer.ObservableSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logical content of the whiteboard.
 * It contains the figures of the current drawing, not the JavaFX view.
 */
public final class DrawingDocument implements Serializable, ObservableModel {
    private transient ObservableSupport observableSupport = new ObservableSupport(this);
    private final List<Figure> figures = new ArrayList<>();

    public void addFigure(Figure figure) {
        if (!figures.contains(figure)) {
            figures.add(figure);
            notifyObservers(ModelChangeType.FIGURE_ADDED, figure);
        }
    }

    public void addFigure(int index, Figure figure) {
        if (!figures.contains(figure)) {
            int insertionIndex = Math.max(0, Math.min(index, figures.size()));
            figures.add(insertionIndex, figure);
            notifyObservers(ModelChangeType.FIGURE_ADDED, figure);
        }
    }

    public void removeFigure(Figure figure) {
        if (figures.remove(figure)) {
            notifyObservers(ModelChangeType.FIGURE_REMOVED, figure);
        }
    }

    public void clear() {
        if (!figures.isEmpty()) {
            figures.clear();
            notifyObservers(ModelChangeType.FIGURES_CLEARED, this);
        }
    }

    public List<Figure> getFigures() {
        return Collections.unmodifiableList(figures);
    }

    public int indexOf(Figure figure) {
        return figures.indexOf(figure);
    }

    @Override
    public void addObserver(ModelObserver observer) {
        getObservableSupport().addObserver(observer);
    }

    @Override
    public void removeObserver(ModelObserver observer) {
        getObservableSupport().removeObserver(observer);
    }

    private void notifyObservers(ModelChangeType type, Object changedObject) {
        getObservableSupport().notifyObservers(type, changedObject);
    }

    private ObservableSupport getObservableSupport() {
        if (observableSupport == null) {
            observableSupport = new ObservableSupport(this);
        }
        return observableSupport;
    }
}
