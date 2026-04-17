package microeditor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite figure that applies movement and color changes to its children.
 */
public final class GroupFigure extends BaseFigure implements FigureContainer {
    private static final double DEFAULT_POSITION = 0.0;
    private static final String DEFAULT_GROUP_COLOR = "#000000";

    private final List<Figure> children = new ArrayList<>();

    /**
     * Creates an empty group.
     *
     * @param id unique group identifier
     */
    public GroupFigure(String id) {
        super(id, DEFAULT_POSITION, DEFAULT_POSITION, DEFAULT_GROUP_COLOR);
    }

    @Override
    public void setPosition(double positionX, double positionY) {
        double deltaX = positionX - getX();
        double deltaY = positionY - getY();
        super.setPosition(positionX, positionY);
        for (Figure child : children) {
            child.move(deltaX, deltaY);
        }
    }

    /**
     * Adds a child figure to the group.
     *
     * @param figure child figure to add
     */
    @Override
    public void addFigure(Figure figure) {
        if (!children.contains(figure)) {
            children.add(figure);
            notifyFigureChanged();
        }
    }

    @Override
    public void addFigure(int index, Figure figure) {
        if (!children.contains(figure)) {
            int insertionIndex = Math.max(0, Math.min(index, children.size()));
            children.add(insertionIndex, figure);
            notifyFigureChanged();
        }
    }

    /**
     * Removes a child figure from the group.
     *
     * @param figure child figure to remove
     */
    @Override
    public void removeFigure(Figure figure) {
        if (children.remove(figure)) {
            notifyFigureChanged();
        }
    }

    @Override
    public int indexOf(Figure figure) {
        return children.indexOf(figure);
    }

    @Override
    public void move(double deltaX, double deltaY) {
        super.move(deltaX, deltaY);
        for (Figure child : children) {
            child.move(deltaX, deltaY);
        }
    }

    @Override
    public void setColor(String color) {
        super.setColor(color);
        for (Figure child : children) {
            child.setColor(color);
        }
    }

    @Override
    public Figure clone() {
        GroupFigure clone = new GroupFigure(getId());
        clone.setPosition(getX(), getY());
        clone.setRotation(getRotation());
        clone.setRotationCenter(getRotationCenterX(), getRotationCenterY());
        clone.move(getTranslateX(), getTranslateY());
        clone.setColor(getColor());
        for (Figure child : children) {
            clone.addFigure(child.clone());
        }
        return clone;
    }

    @Override
    public void restoreState(FigureState state) {
        super.restoreState(state);
        Figure snapshot = state.getSnapshot();
        if (snapshot instanceof GroupFigure groupSnapshot) {
            children.clear();
            for (Figure child : groupSnapshot.children) {
                children.add(child.clone());
            }
            notifyFigureChanged();
        }
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    @Override
    public List<Figure> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public List<Figure> getFigures() {
        return getChildren();
    }
}
