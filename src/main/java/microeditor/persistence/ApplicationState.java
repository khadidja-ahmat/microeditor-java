package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.Serializable;
import java.util.Objects;

/**
 * Serializable application state saved in project files.
 */
public final class ApplicationState implements Serializable {
    private final DrawingDocument document;
    private final Toolbar toolbar;

    /**
     * Creates an application state.
     *
     * @param document current drawing document
     * @param toolbar current toolbar templates
     */
    public ApplicationState(DrawingDocument document, Toolbar toolbar) {
        this.document = Objects.requireNonNull(document);
        this.toolbar = Objects.requireNonNull(toolbar);
    }

    /**
     * Returns the saved drawing document.
     *
     * @return drawing document
     */
    public DrawingDocument getDocument() {
        return document;
    }

    /**
     * Returns the saved toolbar.
     *
     * @return toolbar templates
     */
    public Toolbar getToolbar() {
        return toolbar;
    }
}
