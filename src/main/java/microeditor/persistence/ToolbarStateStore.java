package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists the toolbar separately from project files.
 */
public final class ToolbarStateStore {
    private static final Path TOOLBAR_STATE_PATH = Path.of(
            System.getProperty("user.home"), ".microeditor-toolbar.microeditor");

    private final PersistenceService persistenceService;

    /**
     * Creates a toolbar store.
     *
     * @param persistenceService service used for serialization
     */
    public ToolbarStateStore(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    /**
     * Loads the toolbar state or returns a default toolbar when no state exists.
     *
     * @param defaultToolbar fallback toolbar
     * @return loaded or default toolbar
     * @throws IOException when the state file cannot be read
     * @throws ClassNotFoundException when a serialized class cannot be resolved
     */
    public Toolbar loadOrDefault(Toolbar defaultToolbar) throws IOException, ClassNotFoundException {
        if (!Files.exists(TOOLBAR_STATE_PATH)) {
            return defaultToolbar;
        }
        return persistenceService.load(TOOLBAR_STATE_PATH).getToolbar();
    }

    /**
     * Saves the toolbar state.
     *
     * @param toolbar toolbar to save
     * @throws IOException when the state file cannot be written
     */
    public void save(Toolbar toolbar) throws IOException {
        persistenceService.save(new DrawingDocument(), toolbar, TOOLBAR_STATE_PATH);
    }
}
