package microeditor.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CompositeCommand implements Command {
    private final List<Command> commands;

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
