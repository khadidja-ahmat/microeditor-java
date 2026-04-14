package microeditor.observer;

/**
 * Listener notified when an observable model changes.
 */
@FunctionalInterface
public interface ModelObserver {
    /**
     * Handles a model event.
     *
     * @param event event describing the change
     */
    void update(ModelEvent event);
}
