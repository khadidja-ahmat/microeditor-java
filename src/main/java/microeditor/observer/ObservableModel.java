package microeditor.observer;

/**
 * Model object that accepts observers.
 */
public interface ObservableModel {
    /**
     * Registers an observer.
     *
     * @param observer observer to register
     */
    void addObserver(ModelObserver observer);

    /**
     * Removes a previously registered observer.
     *
     * @param observer observer to remove
     */
    void removeObserver(ModelObserver observer);
}
