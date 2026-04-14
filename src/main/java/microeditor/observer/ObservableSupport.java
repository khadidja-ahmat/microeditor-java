package microeditor.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reusable implementation of observer registration and notification.
 */
public final class ObservableSupport {
    private final Object source;
    private final List<ModelObserver> observers = new ArrayList<>();

    /**
     * Creates support for a given source object.
     *
     * @param source object reported as event source
     */
    public ObservableSupport(Object source) {
        this.source = Objects.requireNonNull(source);
    }

    /**
     * Registers an observer if it is not already registered.
     *
     * @param observer observer to add
     */
    public void addObserver(ModelObserver observer) {
        Objects.requireNonNull(observer);
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes an observer.
     *
     * @param observer observer to remove
     */
    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies every observer of a model change.
     *
     * @param type kind of model change
     * @param target object directly affected by the change
     */
    public void notifyObservers(ModelChangeType type, Object target) {
        ModelEvent event = new ModelEvent(source, type, target);
        for (ModelObserver observer : List.copyOf(observers)) {
            observer.update(event);
        }
    }
}
