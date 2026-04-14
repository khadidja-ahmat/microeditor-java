package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Saves and loads editor projects using Java serialization.
 */
public final class PersistenceService {
    /**
     * Saves a complete application state to disk.
     *
     * @param state application state to save
     * @param path destination file
     * @throws IOException when the file cannot be written
     */
    public void save(ApplicationState state, Path path) throws IOException {
        Objects.requireNonNull(state);
        Objects.requireNonNull(path);

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
            output.writeObject(state);
        }
    }

    /**
     * Loads an application state from disk.
     *
     * @param path source file
     * @return loaded application state
     * @throws IOException when the file cannot be read or has invalid content
     * @throws ClassNotFoundException when a serialized class cannot be resolved
     */
    public ApplicationState load(Path path) throws IOException, ClassNotFoundException {
        Objects.requireNonNull(path);

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
            Object object = input.readObject();
            if (object instanceof ApplicationState state) {
                return state;
            }
            throw new IOException("The selected file does not contain a microeditor project.");
        }
    }

    /**
     * Saves a document and toolbar as one application state.
     *
     * @param document drawing document to save
     * @param toolbar toolbar templates to save
     * @param path destination file
     * @throws IOException when the file cannot be written
     */
    public void save(DrawingDocument document, Toolbar toolbar, Path path) throws IOException {
        save(new ApplicationState(document, toolbar), path);
    }
}
