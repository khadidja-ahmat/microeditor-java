package microeditor.command;

import microeditor.model.Figure;

import java.util.Objects;

/**
 * Command that translates a figure by a fixed delta.
 */
public final class MoveFigureCommand implements Command {
    private final Figure figure;
    private final double dx;
    private final double dy;

    /**
     * Creates a move command.
     *
     * @param figure figure to move
     * @param dx horizontal displacement
     * @param dy vertical displacement
     */
    public MoveFigureCommand(Figure figure, double dx, double dy) {
        this.figure = Objects.requireNonNull(figure);
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public void execute() {
        figure.move(dx, dy);
    }

    @Override
    public void undo() {
        figure.move(-dx, -dy);
    }
}
