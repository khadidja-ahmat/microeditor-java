package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;

import java.util.List;
import java.util.Objects;

public final class UngroupFigureCommand implements Command {
    private final DrawingDocument document;
    private final GroupFigure group;
    private final List<Figure> children;
    private int groupIndex = -1;

    public UngroupFigureCommand(DrawingDocument document, GroupFigure group) {
        this.document = Objects.requireNonNull(document);
        this.group = Objects.requireNonNull(group);
        this.children = List.copyOf(group.getChildren());
    }

    @Override
    public void execute() {
        groupIndex = document.indexOf(group);
        if (groupIndex < 0) {
            throw new IllegalStateException("Cannot ungroup a group that is not in the document.");
        }

        document.removeFigure(group);
        for (int i = 0; i < children.size(); i++) {
            group.removeFigure(children.get(i));
            document.addFigure(groupIndex + i, children.get(i));
        }
    }

    @Override
    public void undo() {
        for (Figure child : children) {
            document.removeFigure(child);
            group.addFigure(child);
        }
        document.addFigure(groupIndex, group);
    }
}
