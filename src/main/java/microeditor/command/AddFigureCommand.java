package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;

import java.util.Objects;

public final class AddFigureCommand implements Command {
    private final DrawingDocument document;
    private final Figure figure;

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
