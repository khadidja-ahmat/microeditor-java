package microeditor.command;

public interface Command {
    void execute();

    void undo();
}
