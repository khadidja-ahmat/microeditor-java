package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.FigureContainer;
import microeditor.model.GroupFigure;

import java.util.List;
import java.util.Objects;

/**
 * Command that replaces a group with its child figures in the same parent.
 */
public final class UngroupFigureCommand implements Command {
    private final FigureContainer parent;
    private final GroupFigure group;
    private final List<Figure> children;
    private int groupIndex = -1;

    /**
     * Creates an ungrouping command for an existing group.
     *
     * @param document document containing the group
     * @param group group to split into child figures
     */
    public UngroupFigureCommand(DrawingDocument document, GroupFigure group) {
        this((FigureContainer) document, group);
    }

    /**
     * Creates an ungrouping command for an existing group.
     *
     * @param parent container directly containing the group
     * @param group group to split into child figures
     */
    public UngroupFigureCommand(FigureContainer parent, GroupFigure group) {
        this.parent = Objects.requireNonNull(parent);
        this.group = Objects.requireNonNull(group);
        this.children = List.copyOf(group.getChildren());
    }

    @Override
    public void execute() {
        groupIndex = parent.indexOf(group);
        if (groupIndex < 0) {
            throw new IllegalStateException("Cannot ungroup a group that is not in the parent container.");
        }

        parent.removeFigure(group);
        for (int i = 0; i < children.size(); i++) {
            group.removeFigure(children.get(i));
            parent.addFigure(groupIndex + i, children.get(i));
        }
    }

    @Override
    public void undo() {
        for (Figure child : children) {
            parent.removeFigure(child);
            group.addFigure(child);
        }
        parent.addFigure(groupIndex, group);
    }
}
