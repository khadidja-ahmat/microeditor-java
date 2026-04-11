package microeditor.persistence;

import microeditor.model.DrawingDocument;
import microeditor.model.RectangleFigure;
import microeditor.model.Toolbar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void documentAndToolbarCanBeSavedAndLoaded() throws Exception {
        DrawingDocument document = new DrawingDocument();
        Toolbar toolbar = new Toolbar();
        RectangleFigure documentFigure = new RectangleFigure("rectangle-1", 10, 20, 100, 50, "#000000");
        RectangleFigure toolbarFigure = new RectangleFigure("rectangle-template", 0, 0, 80, 40, "#FF0000");
        PersistenceService persistenceService = new PersistenceService();
        Path saveFile = temporaryDirectory.resolve("drawing.microeditor");

        document.addFigure(documentFigure);
        toolbar.addFigure(toolbarFigure);

        persistenceService.save(document, toolbar, saveFile);
        ApplicationState loadedState = persistenceService.load(saveFile);

        assertEquals(1, loadedState.getDocument().getFigures().size());
        assertEquals(1, loadedState.getToolbar().getFigures().size());
        assertEquals("#000000", loadedState.getDocument().getFigures().get(0).getColor());
        assertEquals("#FF0000", loadedState.getToolbar().getFigures().get(0).getColor());
    }
}
