package microeditor.observer;

public final class ModelEvent {
    private final Object source;
    private final ModelChangeType type;
    private final Object target;

    public ModelEvent(Object source, ModelChangeType type, Object target) {
        this.source = source;
        this.type = type;
        this.target = target;
    }

    public Object getSource() {
        return source;
    }

    public ModelChangeType getType() {
        return type;
    }

    public Object getTarget() {
        return target;
    }
}
