package microeditor.command;

import microeditor.model.Figure;
import microeditor.model.Toolbar;

import java.util.Objects;

/**
 * Command that adds a copy of a figure to the toolbar templates.
 */
public final class AddToolbarFigureCommand implements Command {
    private final Toolbar toolbar;
    private final Figure templateSnapshot;
    private Figure addedTemplate;
    private int addedIndex = -1;

    /**
     * Creates a toolbar insertion command from a source figure.
     *
     * @param toolbar toolbar receiving the template
     * @param sourceFigure figure copied as a reusable template
     */
    public AddToolbarFigureCommand(Toolbar toolbar, Figure sourceFigure) {
        this.toolbar = Objects.requireNonNull(toolbar);
        this.templateSnapshot = Objects.requireNonNull(sourceFigure).clone();
    }

    @Override
    public void execute() {
        if (addedIndex < 0) {
            addedIndex = toolbar.getFigures().size();
        }
        addedTemplate = toolbar.addFigure(addedIndex, templateSnapshot);
    }

    @Override
    public void undo() {
        if (addedTemplate != null) {
            toolbar.removeFigure(addedTemplate);
        }
    }
}
