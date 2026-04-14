package microeditor.command;

import microeditor.model.Figure;
import microeditor.model.Toolbar;

import java.util.Objects;

/**
 * Command that removes a template from the toolbar and can restore it.
 */
public final class RemoveToolbarFigureCommand implements Command {
    private final Toolbar toolbar;
    private final int index;
    private Figure removedTemplate;

    /**
     * Creates a toolbar removal command for a template index.
     *
     * @param toolbar toolbar containing the template
     * @param index index of the template to remove
     */
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
