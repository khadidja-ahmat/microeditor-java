package microeditor.model;

import microeditor.observer.ObservableModel;

import java.io.Serializable;
import java.util.List;

/**
 * Common abstraction for everything that can be placed on the whiteboard.
 * A figure can be simple, like a rectangle, or composed, like a group.
 */
public interface Figure extends Serializable, ObservableModel {
    String getId();

    double getX();

    double getY();

    void setPosition(double x, double y);

    double getRotation();

    void setRotation(double rotation);

    double getRotationCenterX();

    double getRotationCenterY();

    void setRotationCenter(double x, double y);

    double getTranslateX();

    double getTranslateY();

    void move(double deltaX, double deltaY);

    String getColor();

    void setColor(String color);

    Figure clone();

    FigureState saveState();

    void restoreState(FigureState state);

    default boolean isGroup() {
        return false;
    }

    default List<Figure> getChildren() {
        return List.of();
    }
}
