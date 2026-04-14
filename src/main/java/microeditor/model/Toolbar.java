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
 * Stores reusable figure templates.
 * When the user drops a template on the whiteboard, the application creates a copy.
 */
public final class Toolbar implements Serializable, ObservableModel {
    private transient ObservableSupport observableSupport = new ObservableSupport(this);
    private final List<Figure> figures = new ArrayList<>();

    /**
     * Adds a copy of a figure at the end of the toolbar.
     *
     * @param figure source figure copied as a template
     * @return template stored in the toolbar
     */
    public Figure addFigure(Figure figure) {
        return addFigure(figures.size(), figure);
    }

    /**
     * Adds a copy of a figure at the requested toolbar index.
     *
     * @param index desired insertion index
     * @param figure source figure copied as a template
     * @return template stored in the toolbar
     */
    public Figure addFigure(int index, Figure figure) {
        Figure template = figure.clone();
        int insertionIndex = Math.max(0, Math.min(index, figures.size()));
        figures.add(insertionIndex, template);
        notifyObservers(ModelChangeType.TEMPLATE_ADDED, template);
        return template;
    }

    /**
     * Removes a template from the toolbar.
     *
     * @param figure template to remove
     */
    public void removeFigure(Figure figure) {
        if (figures.remove(figure)) {
            notifyObservers(ModelChangeType.TEMPLATE_REMOVED, figure);
        }
    }

    /**
     * Removes a template by index.
     *
     * @param index template index
     * @return removed template
     */
    public Figure removeFigure(int index) {
        Figure removedFigure = figures.remove(index);
        notifyObservers(ModelChangeType.TEMPLATE_REMOVED, removedFigure);
        return removedFigure;
    }

    /**
     * Creates a drawable figure from a toolbar template.
     *
     * @param index template index
     * @return cloned figure ready to be placed on the document
     */
    public Figure createFigure(int index) {
        return figures.get(index).clone();
    }

    /**
     * Returns the stored toolbar templates.
     *
     * @return unmodifiable list of templates
     */
    public List<Figure> getFigures() {
        return Collections.unmodifiableList(figures);
    }

    /**
     * Returns the index of a template.
     *
     * @param figure searched template
     * @return index of the template, or {@code -1} when absent
     */
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
