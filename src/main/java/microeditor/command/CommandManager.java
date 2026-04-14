package microeditor.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Coordinates command execution and keeps the undo/redo history.
 */
public final class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Executes a command and stores it in the undo stack.
     *
     * @param command command to execute
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Reverts the most recent command when one is available.
     */
    public void undo() {
        if (canUndo()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Re-executes the most recently undone command when one is available.
     */
    public void redo() {
        if (canRedo()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    /**
     * Checks whether an undo operation can be performed.
     *
     * @return {@code true} when the undo stack is not empty
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks whether a redo operation can be performed.
     *
     * @return {@code true} when the redo stack is not empty
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Removes all undo and redo history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
