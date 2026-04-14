package microeditor.model;

import java.io.Serializable;

/**
 * Memento object: it keeps a safe copy of a figure before a command changes it.
 */
public final class FigureState implements Serializable {
    private final Figure snapshot;

    /**
     * Captures a defensive copy of a figure.
     *
     * @param snapshot figure to snapshot
     */
    public FigureState(Figure snapshot) {
        this.snapshot = snapshot.clone();
    }

    /**
     * Returns a defensive copy of the stored snapshot.
     *
     * @return cloned snapshot
     */
    public Figure getSnapshot() {
        return snapshot.clone();
    }
}
