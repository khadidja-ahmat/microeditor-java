package microeditor.command;

import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.FigureContainer;
import microeditor.model.GroupFigure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Command that replaces several sibling figures with a single group.
 */
public final class GroupFiguresCommand implements Command {
    private final FigureContainer parent;
    private final GroupFigure group;
    private final List<Figure> figures;
    private final List<FigureWithIndex> originalFigures = new ArrayList<>();
    private int groupIndex = -1;

    /**
     * Creates a grouping command.
     *
     * @param document document containing the figures
     * @param group group that will receive the figures
     * @param figures figures to group, in document order or selection order
     */
    public GroupFiguresCommand(DrawingDocument document, GroupFigure group, List<Figure> figures) {
        this((FigureContainer) document, group, figures);
    }

    /**
     * Creates a grouping command.
     *
     * @param parent container containing the figures
     * @param group group that will receive the figures
     * @param figures sibling figures to group, in container order or selection order
     */
    public GroupFiguresCommand(FigureContainer parent, GroupFigure group, List<Figure> figures) {
        this.parent = Objects.requireNonNull(parent);
        this.group = Objects.requireNonNull(group);
        this.figures = List.copyOf(Objects.requireNonNull(figures));
        if (this.figures.size() < 2) {
            throw new IllegalArgumentException("A group must contain at least two figures.");
        }
    }

    @Override
    public void execute() {
        originalFigures.clear();
        groupIndex = Integer.MAX_VALUE;

        List<FigureWithIndex> orderedFigures = new ArrayList<>();
        for (Figure figure : figures) {
            int index = parent.indexOf(figure);
            if (index < 0) {
                throw new IllegalStateException("Cannot group a figure that is not in the parent container.");
            }
            groupIndex = Math.min(groupIndex, index);
            orderedFigures.add(new FigureWithIndex(figure, index));
        }

        orderedFigures.sort(Comparator.comparingInt(FigureWithIndex::index));
        originalFigures.addAll(orderedFigures);
        for (FigureWithIndex item : orderedFigures) {
            group.addFigure(item.figure());
        }
        for (FigureWithIndex item : orderedFigures) {
            parent.removeFigure(item.figure());
        }
        parent.addFigure(groupIndex, group);
    }

    @Override
    public void undo() {
        parent.removeFigure(group);
        for (FigureWithIndex item : originalFigures) {
            group.removeFigure(item.figure());
            parent.addFigure(item.index(), item.figure());
        }
    }

    private record FigureWithIndex(Figure figure, int index) {
    }
}
