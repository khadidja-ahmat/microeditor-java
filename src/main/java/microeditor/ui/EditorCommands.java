package microeditor.ui;

import javafx.scene.paint.Color;
import microeditor.command.AddFigureCommand;
import microeditor.command.AddToolbarFigureCommand;
import microeditor.command.ChangeColorCommand;
import microeditor.command.Command;
import microeditor.command.CommandManager;
import microeditor.command.CompositeCommand;
import microeditor.command.EditFigureCommand;
import microeditor.command.GroupFiguresCommand;
import microeditor.command.MoveFigureCommand;
import microeditor.command.RemoveFigureCommand;
import microeditor.command.RemoveToolbarFigureCommand;
import microeditor.command.UngroupFigureCommand;
import microeditor.factory.FigureFactory;
import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.FigureState;
import microeditor.model.GroupFigure;
import microeditor.model.Selection;
import microeditor.model.Toolbar;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Application service that translates UI actions into undoable model commands.
 */
final class EditorCommands {
    private final FigureFactory figureFactory;
    private final CommandManager commandManager;
    private final Selection selection;

    private DrawingDocument document;
    private Toolbar toolbar;
    private WhiteboardView whiteboardView;
    private ToolPaletteView toolPaletteView;
    private Supplier<Color> selectedColorSupplier = () -> Color.web("#4A90E2");

    /**
     * Creates the editor command facade.
     */
    EditorCommands(
            FigureFactory figureFactory,
            CommandManager commandManager,
            Selection selection,
            DrawingDocument document,
            Toolbar toolbar) {
        this.figureFactory = Objects.requireNonNull(figureFactory);
        this.commandManager = Objects.requireNonNull(commandManager);
        this.selection = Objects.requireNonNull(selection);
        this.document = Objects.requireNonNull(document);
        this.toolbar = Objects.requireNonNull(toolbar);
    }

    DrawingDocument getDocument() {
        return document;
    }

    Toolbar getToolbar() {
        return toolbar;
    }

    void setDocument(DrawingDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    void setToolbar(Toolbar toolbar) {
        this.toolbar = Objects.requireNonNull(toolbar);
    }

    void setViews(WhiteboardView whiteboardView, ToolPaletteView toolPaletteView) {
        this.whiteboardView = Objects.requireNonNull(whiteboardView);
        this.toolPaletteView = Objects.requireNonNull(toolPaletteView);
    }

    void setSelectedColorSupplier(Supplier<Color> selectedColorSupplier) {
        this.selectedColorSupplier = Objects.requireNonNull(selectedColorSupplier);
    }

    void clearCommandHistory() {
        commandManager.clear();
    }

    void addFigureFromToolbar(String toolName, double x, double y) {
        Figure figure = createFigureFromTool(toolName, x, y);
        if (figure == null) {
            return;
        }

        commandManager.executeCommand(new AddFigureCommand(document, figure));
        selection.clear();
        selection.select(figure);
        whiteboardView.render();
    }

    void addSelectionToToolbar() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        if (selectedFigures.isEmpty()) {
            return;
        }

        Figure template = selectedFigures.size() == 1
                ? selectedFigures.get(0)
                : createToolbarGroup(selectedFigures);
        commandManager.executeCommand(new AddToolbarFigureCommand(toolbar, template));
        toolPaletteView.render();
    }

    void removeToolbarFigure(int index) {
        if (index < 0 || index >= toolbar.getFigures().size()) {
            return;
        }

        commandManager.executeCommand(new RemoveToolbarFigureCommand(toolbar, index));
        toolPaletteView.render();
    }

    void moveSelectedFigures(double deltaX, double deltaY) {
        executeAsOneUserAction(selection.getSelectedFigures().stream()
                .map(selectedFigure -> new MoveFigureCommand(selectedFigure, deltaX, deltaY))
                .toList());
    }

    void groupSelectedFigures() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        if (selectedFigures.size() < 2) {
            return;
        }

        GroupFigure group = new GroupFigure("group-" + UUID.randomUUID());
        commandManager.executeCommand(new GroupFiguresCommand(document, group, selectedFigures));
        selection.clear();
        selection.select(group);
        whiteboardView.render();
    }

    void ungroupSelectedGroup() {
        Figure selectedFigure = getSingleSelectedFigure();
        if (!(selectedFigure instanceof GroupFigure group)) {
            return;
        }

        commandManager.executeCommand(new UngroupFigureCommand(document, group));
        selection.clear();
        whiteboardView.render();
    }

    void editSelectedFigure() {
        Figure selectedFigure = getSingleSelectedFigure();
        if (selectedFigure == null) {
            return;
        }

        FigureState oldState = selectedFigure.saveState();
        Optional<FigureState> newState = FigureEditDialog.show(selectedFigure, whiteboardView::render);
        if (newState.isPresent()) {
            selectedFigure.restoreState(oldState);
            commandManager.executeCommand(new EditFigureCommand(selectedFigure, oldState, newState.get()));
        } else {
            selectedFigure.restoreState(oldState);
        }
        whiteboardView.render();
    }

    void changeSelectedColor() {
        String selectedColor = toHex(selectedColorSupplier.get());
        executeAsOneUserAction(selection.getSelectedFigures().stream()
                .map(selectedFigure -> new ChangeColorCommand(selectedFigure, selectedColor))
                .toList());
        whiteboardView.render();
    }

    void deleteSelectedFigures() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        executeAsOneUserAction(selectedFigures.stream()
                .map(selectedFigure -> new RemoveFigureCommand(document, selectedFigure))
                .toList());
        selection.clear();
        whiteboardView.render();
    }

    void undo() {
        commandManager.undo();
        whiteboardView.render();
        toolPaletteView.render();
    }

    void redo() {
        commandManager.redo();
        whiteboardView.render();
        toolPaletteView.render();
    }

    void seedToolbarIfEmpty() {
        if (toolbar.getFigures().isEmpty()) {
            toolbar.addFigure(figureFactory.createRectangle(0, 0, 120, 70));
            toolbar.addFigure(figureFactory.createPolygon(0, 0, 5, 45));
        }
    }

    private Figure createFigureFromTool(String toolName, double x, double y) {
        if (toolName.startsWith(ToolPaletteView.TOOL_PREFIX)) {
            int index = Integer.parseInt(toolName.substring(ToolPaletteView.TOOL_PREFIX.length()));
            Figure figure = toolbar.createFigure(index);
            figure.setPosition(x, y);
            return figure;
        }
        return null;
    }

    private GroupFigure createToolbarGroup(List<Figure> selectedFigures) {
        GroupFigure group = new GroupFigure("toolbar-group-" + UUID.randomUUID());
        for (Figure selectedFigure : selectedFigures) {
            group.addFigure(selectedFigure);
        }
        return group;
    }

    private Figure getSingleSelectedFigure() {
        if (selection.getSelectedFigures().size() != 1) {
            return null;
        }
        return selection.getSelectedFigures().iterator().next();
    }

    private void executeAsOneUserAction(List<? extends Command> commands) {
        if (commands.isEmpty()) {
            return;
        }
        if (commands.size() == 1) {
            commandManager.executeCommand(commands.get(0));
            return;
        }
        commandManager.executeCommand(new CompositeCommand(List.copyOf(commands)));
    }

    private String toHex(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
