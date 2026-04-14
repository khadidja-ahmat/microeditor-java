package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;

import java.util.Objects;

/**
 * Command that inserts a figure into the current drawing document.
 */
public final class AddFigureCommand implements Command {
    private final DrawingDocument document;
    private final Figure figure;

    /**
     * Creates an add command for the given document and figure.
     *
     * @param document document receiving the figure
     * @param figure figure to insert
     */
    public AddFigureCommand(DrawingDocument document, Figure figure) {
        this.document = Objects.requireNonNull(document);
        this.figure = Objects.requireNonNull(figure);
    }

    @Override
    public void execute() {
        document.addFigure(figure);
    }

    @Override
    public void undo() {
        document.removeFigure(figure);
    }
}
