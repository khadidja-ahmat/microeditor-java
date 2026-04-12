package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.FigureState;
import microeditor.model.GroupFigure;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;
import microeditor.model.Toolbar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandManagerTest {
    @Test
    void addFigureCanBeUndoneAndRedone() {
        DrawingDocument document = new DrawingDocument();
        Figure rectangle = new RectangleFigure("rectangle-1", 10, 20, 100, 50, "#000000");
        CommandManager manager = new CommandManager();

        manager.executeCommand(new AddFigureCommand(document, rectangle));

        assertEquals(1, document.getFigures().size());
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());

        manager.undo();

        assertTrue(document.getFigures().isEmpty());
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());

        manager.redo();

        assertEquals(1, document.getFigures().size());
        assertSame(rectangle, document.getFigures().get(0));
    }

    @Test
    void removeFigureCanBeUndoneAtSameIndex() {
        DrawingDocument document = new DrawingDocument();
        Figure first = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        Figure second = new PolygonFigure("polygon-1", 40, 40, 5, 30, "#111111");
        CommandManager manager = new CommandManager();
        document.addFigure(first);
        document.addFigure(second);

        manager.executeCommand(new RemoveFigureCommand(document, first));

        assertEquals(List.of(second), document.getFigures());

        manager.undo();

        assertEquals(List.of(first, second), document.getFigures());
    }

    @Test
    void moveFigureCanBeUndoneAndRedone() {
        Figure rectangle = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        CommandManager manager = new CommandManager();

        manager.executeCommand(new MoveFigureCommand(rectangle, 20, 15));

        assertEquals(20, rectangle.getTranslateX());
        assertEquals(15, rectangle.getTranslateY());

        manager.undo();

        assertEquals(0, rectangle.getTranslateX());
        assertEquals(0, rectangle.getTranslateY());

        manager.redo();

        assertEquals(20, rectangle.getTranslateX());
        assertEquals(15, rectangle.getTranslateY());
    }

    @Test
    void editFigureCanBeUndoneAndRedone() {
        RectangleFigure rectangle = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        FigureState oldState = rectangle.saveState();
        rectangle.setWidth(200);
        rectangle.setHeight(80);
        rectangle.setColor("#FF0000");
        FigureState newState = rectangle.saveState();
        rectangle.restoreState(oldState);

        CommandManager manager = new CommandManager();
        manager.executeCommand(new EditFigureCommand(rectangle, oldState, newState));

        assertEquals(200, rectangle.getWidth());
        assertEquals(80, rectangle.getHeight());
        assertEquals("#FF0000", rectangle.getColor());

        manager.undo();

        assertEquals(100, rectangle.getWidth());
        assertEquals(50, rectangle.getHeight());
        assertEquals("#000000", rectangle.getColor());
    }

    @Test
    void groupFiguresCanBeUndone() {
        DrawingDocument document = new DrawingDocument();
        Figure rectangle = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        Figure polygon = new PolygonFigure("polygon-1", 40, 40, 5, 30, "#111111");
        GroupFigure group = new GroupFigure("group-1");
        CommandManager manager = new CommandManager();
        document.addFigure(rectangle);
        document.addFigure(polygon);

        manager.executeCommand(new GroupFiguresCommand(document, group, List.of(rectangle, polygon)));

        assertEquals(1, document.getFigures().size());
        assertSame(group, document.getFigures().get(0));
        assertEquals(List.of(rectangle, polygon), group.getChildren());

        manager.undo();

        assertEquals(List.of(rectangle, polygon), document.getFigures());
        assertTrue(group.getChildren().isEmpty());
    }

    @Test
    void ungroupFigureCanBeUndone() {
        DrawingDocument document = new DrawingDocument();
        Figure rectangle = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        Figure polygon = new PolygonFigure("polygon-1", 40, 40, 5, 30, "#111111");
        GroupFigure group = new GroupFigure("group-1");
        CommandManager manager = new CommandManager();
        group.addFigure(rectangle);
        group.addFigure(polygon);
        document.addFigure(group);

        manager.executeCommand(new UngroupFigureCommand(document, group));

        assertEquals(List.of(rectangle, polygon), document.getFigures());
        assertTrue(group.getChildren().isEmpty());

        manager.undo();

        assertEquals(List.of(group), document.getFigures());
        assertEquals(List.of(rectangle, polygon), group.getChildren());
    }

    @Test
    void addToolbarFigureCanBeUndoneAndRedone() {
        Toolbar toolbar = new Toolbar();
        Figure rectangle = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        CommandManager manager = new CommandManager();

        manager.executeCommand(new AddToolbarFigureCommand(toolbar, rectangle));
        rectangle.move(20, 15);

        assertEquals(1, toolbar.getFigures().size());
        assertEquals(0, toolbar.getFigures().get(0).getTranslateX());
        assertEquals(0, toolbar.getFigures().get(0).getTranslateY());

        manager.undo();

        assertTrue(toolbar.getFigures().isEmpty());

        manager.redo();

        assertEquals(1, toolbar.getFigures().size());
    }

    @Test
    void removeToolbarFigureCanBeUndoneAtSameIndex() {
        Toolbar toolbar = new Toolbar();
        Figure first = new RectangleFigure("rectangle-1", 0, 0, 100, 50, "#000000");
        Figure second = new PolygonFigure("polygon-1", 40, 40, 5, 30, "#111111");
        CommandManager manager = new CommandManager();
        toolbar.addFigure(first);
        toolbar.addFigure(second);

        manager.executeCommand(new RemoveToolbarFigureCommand(toolbar, 0));

        assertEquals(1, toolbar.getFigures().size());
        assertEquals("polygon-1", toolbar.getFigures().get(0).getId());

        manager.undo();

        assertEquals(2, toolbar.getFigures().size());
        assertEquals("rectangle-1", toolbar.getFigures().get(0).getId());
        assertEquals("polygon-1", toolbar.getFigures().get(1).getId());
    }
}
