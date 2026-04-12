package microeditor.command;

import microeditor.model.Figure;
import microeditor.model.Toolbar;

import java.util.Objects;

public final class RemoveToolbarFigureCommand implements Command {
    private final Toolbar toolbar;
    private final int index;
    private Figure removedTemplate;

    public RemoveToolbarFigureCommand(Toolbar toolbar, int index) {
        this.toolbar = Objects.requireNonNull(toolbar);
        this.index = index;
    }

    @Override
    public void execute() {
        removedTemplate = toolbar.removeFigure(index);
    }

    @Override
    public void undo() {
        if (removedTemplate != null) {
            removedTemplate = toolbar.addFigure(index, removedTemplate);
        }
    }
}
