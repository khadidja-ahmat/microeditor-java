package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;

import java.util.Objects;

public final class RemoveFigureCommand implements Command {
    private final DrawingDocument document;
    private final Figure figure;
    private int removedIndex = -1;

    public RemoveFigureCommand(DrawingDocument document, Figure figure) {
        this.document = Objects.requireNonNull(document);
        this.figure = Objects.requireNonNull(figure);
    }

    @Override
    public void execute() {
        removedIndex = document.getFigures().indexOf(figure);
        document.removeFigure(figure);
    }

    @Override
    public void undo() {
        if (removedIndex >= 0) {
            document.addFigure(removedIndex, figure);
        }
    }
}
