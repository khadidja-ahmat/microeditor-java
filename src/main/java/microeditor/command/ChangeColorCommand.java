package microeditor.command;

import microeditor.model.Figure;

import java.util.Objects;

public final class ChangeColorCommand implements Command {
    private final Figure figure;
    private final String newColor;
    private String oldColor;

    public ChangeColorCommand(Figure figure, String newColor) {
        this.figure = Objects.requireNonNull(figure);
        this.newColor = Objects.requireNonNull(newColor);
    }

    @Override
    public void execute() {
        oldColor = figure.getColor();
        figure.setColor(newColor);
    }

    @Override
    public void undo() {
        if (oldColor != null) {
            figure.setColor(oldColor);
        }
    }
}
