package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.Toolbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class PersistenceService {
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

    public void save(DrawingDocument document, Toolbar toolbar, Path path) throws IOException {
        save(new ApplicationState(document, toolbar), path);
    }
}
