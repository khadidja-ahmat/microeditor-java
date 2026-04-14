package microeditor.model;

import microeditor.observer.ObservableModel;

import java.io.Serializable;
import java.util.List;

/**
 * Common abstraction for everything that can be placed on the whiteboard.
 * A figure can be simple, like a rectangle, or composed, like a group.
 */
public interface Figure extends Serializable, ObservableModel {
    /**
     * Returns the stable identifier of this figure.
     *
     * @return unique figure identifier
     */
    String getId();

    /**
     * Returns the base X coordinate before translation.
     *
     * @return horizontal position
     */
    double getX();

    /**
     * Returns the base Y coordinate before translation.
     *
     * @return vertical position
     */
    double getY();

    /**
     * Sets the base position of the figure.
     *
     * @param x horizontal position
     * @param y vertical position
     */
    void setPosition(double x, double y);

    /**
     * Returns the rotation angle in degrees.
     *
     * @return rotation angle
     */
    double getRotation();

    /**
     * Sets the rotation angle in degrees.
     *
     * @param rotation rotation angle
     */
    void setRotation(double rotation);

    double getRotationCenterX();

    double getRotationCenterY();

    void setRotationCenter(double x, double y);

    double getTranslateX();

    double getTranslateY();

    /**
     * Moves the figure relatively to its current translation.
     *
     * @param deltaX horizontal displacement
     * @param deltaY vertical displacement
     */
    void move(double deltaX, double deltaY);

    /**
     * Returns the figure color.
     *
     * @return color in web format
     */
    String getColor();

    /**
     * Changes the figure color.
     *
     * @param color color in web format
     */
    void setColor(String color);

    /**
     * Creates an independent copy of this figure.
     *
     * @return cloned figure
     */
    Figure clone();

    /**
     * Saves the current state for undo/redo operations.
     *
     * @return immutable state snapshot
     */
    FigureState saveState();

    /**
     * Restores a previously saved figure state.
     *
     * @param state state to restore
     */
    void restoreState(FigureState state);

    /**
     * Indicates whether the figure is a group.
     *
     * @return {@code true} for group figures
     */
    default boolean isGroup() {
        return false;
    }

    /**
     * Returns children when this figure is a group.
     *
     * @return group children or an empty list for simple figures
     */
    default List<Figure> getChildren() {
        return List.of();
    }
}
