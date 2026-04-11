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

    public void select(Figure figure) {
        if (selectedFigures.add(figure)) {
            notifySelectionChanged(figure);
        }
    }

    public void unselect(Figure figure) {
        if (selectedFigures.remove(figure)) {
            notifySelectionChanged(figure);
        }
    }

    public void clear() {
        if (!selectedFigures.isEmpty()) {
            selectedFigures.clear();
            notifySelectionChanged(this);
        }
    }

    public boolean isSelected(Figure figure) {
        return selectedFigures.contains(figure);
    }

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
