package microeditor.observer;

@FunctionalInterface
public interface ModelObserver {
    void update(ModelEvent event);
}
