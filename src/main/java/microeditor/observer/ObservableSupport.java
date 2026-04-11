package microeditor.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ObservableSupport {
    private final Object source;
    private final List<ModelObserver> observers = new ArrayList<>();

    public ObservableSupport(Object source) {
        this.source = Objects.requireNonNull(source);
    }

    public void addObserver(ModelObserver observer) {
        Objects.requireNonNull(observer);
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ModelChangeType type, Object target) {
        ModelEvent event = new ModelEvent(source, type, target);
        for (ModelObserver observer : List.copyOf(observers)) {
            observer.update(event);
        }
    }
}
