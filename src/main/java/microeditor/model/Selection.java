package microeditor.model;

import microeditor.observer.ModelChangeType;
import microeditor.observer.ModelObserver;
import microeditor.observer.ObservableModel;
import microeditor.observer.ObservableSupport;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Current user selection.
 * It is separate from the document because selection is a UI state, not a figure.
 */
public final class Selection implements Serializable, ObservableModel {
    private transient ObservableSupport observableSupport = new ObservableSupport(this);
    private final Set<Figure> selectedFigures = new LinkedHashSet<>();

    /**
     * Adds a figure to the current selection.
     *
     * @param figure figure to select
     */
    public void select(Figure figure) {
        if (selectedFigures.add(figure)) {
            notifySelectionChanged(figure);
        }
    }

    /**
     * Removes a figure from the current selection.
     *
     * @param figure figure to unselect
     */
    public void unselect(Figure figure) {
        if (selectedFigures.remove(figure)) {
            notifySelectionChanged(figure);
        }
    }

    /**
     * Clears the current selection.
     */
    public void clear() {
        if (!selectedFigures.isEmpty()) {
            selectedFigures.clear();
            notifySelectionChanged(this);
        }
    }

    /**
     * Checks whether a figure is selected.
     *
     * @param figure tested figure
     * @return {@code true} when the figure is selected
     */
    public boolean isSelected(Figure figure) {
        return selectedFigures.contains(figure);
    }

    /**
     * Returns selected figures in selection order.
     *
     * @return unmodifiable set of selected figures
     */
    public Set<Figure> getSelectedFigures() {
        return Collections.unmodifiableSet(selectedFigures);
    }

    @Override
    public void addObserver(ModelObserver observer) {
        getObservableSupport().addObserver(observer);
    }

    @Override
    public void removeObserver(ModelObserver observer) {
        getObservableSupport().removeObserver(observer);
    }

    private void notifySelectionChanged(Object changedObject) {
        getObservableSupport().notifyObservers(ModelChangeType.SELECTION_CHANGED, changedObject);
    }

    private ObservableSupport getObservableSupport() {
        if (observableSupport == null) {
            observableSupport = new ObservableSupport(this);
        }
        return observableSupport;
    }
}
