package microeditor.command;

import microeditor.model.Figure;
import microeditor.model.FigureState;

import java.util.Objects;

public final class EditFigureCommand implements Command {
    private final Figure figure;
    private final FigureState oldState;
    private final FigureState newState;

    public EditFigureCommand(Figure figure, FigureState oldState, FigureState newState) {
        this.figure = Objects.requireNonNull(figure);
        this.oldState = Objects.requireNonNull(oldState);
        this.newState = Objects.requireNonNull(newState);
    }

    @Override
    public void execute() {
        figure.restoreState(newState);
    }

    @Override
    public void undo() {
        figure.restoreState(oldState);
    }
}
