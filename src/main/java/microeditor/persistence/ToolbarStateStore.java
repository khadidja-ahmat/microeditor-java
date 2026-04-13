package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ToolbarStateStore {
    private static final Path TOOLBAR_STATE_PATH = Path.of(
            System.getProperty("user.home"), ".microeditor-toolbar.microeditor");

    private final PersistenceService persistenceService;

    public ToolbarStateStore(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public Toolbar loadOrDefault(Toolbar defaultToolbar) throws IOException, ClassNotFoundException {
        if (!Files.exists(TOOLBAR_STATE_PATH)) {
            return defaultToolbar;
        }
        return persistenceService.load(TOOLBAR_STATE_PATH).getToolbar();
    }

    public void save(Toolbar toolbar) throws IOException {
        persistenceService.save(new DrawingDocument(), toolbar, TOOLBAR_STATE_PATH);
    }
}
