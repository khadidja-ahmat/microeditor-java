package microeditor.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Command composed of several commands executed as one undoable operation.
 */
public final class CompositeCommand implements Command {
    private final List<Command> commands;

    /**
     * Creates a composite command.
     *
     * @param commands commands to execute in order
     */
    public CompositeCommand(List<Command> commands) {
        Objects.requireNonNull(commands);
        if (commands.isEmpty()) {
            throw new IllegalArgumentException("A composite command must contain at least one command.");
        }
        this.commands = List.copyOf(commands);
    }

    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        List<Command> reverseCommands = new ArrayList<>(commands);
        Collections.reverse(reverseCommands);
        for (Command command : reverseCommands) {
            command.undo();
        }
    }
}
