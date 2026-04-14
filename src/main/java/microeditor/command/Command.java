package microeditor.command;

/**
 * Reversible user action.
 * Commands are executed by the editor and can later restore the previous state.
 */
public interface Command {
    /**
     * Applies the action to the model.
     */
    void execute();

    /**
     * Reverts the changes made by {@link #execute()}.
     */
    void undo();
}
