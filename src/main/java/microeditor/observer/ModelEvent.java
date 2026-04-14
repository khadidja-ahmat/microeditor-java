package microeditor.observer;

/**
 * Event sent when an observable model object changes.
 */
public final class ModelEvent {
    private final Object source;
    private final ModelChangeType type;
    private final Object target;

    /**
     * Creates a model event.
     *
     * @param source object that emitted the event
     * @param type kind of model change
     * @param target object directly affected by the change
     */
    public ModelEvent(Object source, ModelChangeType type, Object target) {
        this.source = source;
        this.type = type;
        this.target = target;
    }

    /**
     * Returns the event source.
     *
     * @return object that emitted the event
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the change type.
     *
     * @return model change type
     */
    public ModelChangeType getType() {
        return type;
    }

    /**
     * Returns the object affected by the change.
     *
     * @return changed object
     */
    public Object getTarget() {
        return target;
    }
}
