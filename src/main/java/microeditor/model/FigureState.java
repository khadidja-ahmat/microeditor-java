package microeditor.model;

import java.io.Serializable;

/**
 * Memento object: it keeps a safe copy of a figure before a command changes it.
 */
public final class FigureState implements Serializable {
    private final Figure snapshot;

    public FigureState(Figure snapshot) {
        this.snapshot = snapshot.clone();
    }

    public Figure getSnapshot() {
        return snapshot.clone();
    }
}
