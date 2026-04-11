package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.Serializable;
import java.util.Objects;

public final class ApplicationState implements Serializable {
    private final DrawingDocument document;
    private final Toolbar toolbar;

    public ApplicationState(DrawingDocument document, Toolbar toolbar) {
        this.document = Objects.requireNonNull(document);
        this.toolbar = Objects.requireNonNull(toolbar);
    }

    public DrawingDocument getDocument() {
        return document;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
}
