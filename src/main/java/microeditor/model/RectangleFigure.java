package microeditor.model;

/**
 * Rectangular figure with editable dimensions and optional rounded corners.
 */
public final class RectangleFigure extends BaseFigure {
    private double width;
    private double height;
    private double cornerRadius;

    /**
     * Creates a rectangle figure.
     *
     * @param id unique figure identifier
     * @param x horizontal position
     * @param y vertical position
     * @param width rectangle width
     * @param height rectangle height
     * @param color fill color in web format
     */
    public RectangleFigure(String id, double x, double y, double width, double height, String color) {
        super(id, x, y, color);
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the rectangle width.
     *
     * @return rectangle width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the rectangle width.
     *
     * @param width rectangle width
     */
    public void setWidth(double width) {
        this.width = width;
        notifyFigureChanged();
    }

    /**
     * Returns the rectangle height.
     *
     * @return rectangle height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the rectangle height.
     *
     * @param height rectangle height
     */
    public void setHeight(double height) {
        this.height = height;
        notifyFigureChanged();
    }

    /**
     * Returns the corner radius.
     *
     * @return corner radius
     */
    public double getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius.
     *
     * @param cornerRadius corner radius
     */
    public void setCornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
        notifyFigureChanged();
    }

    @Override
    public Figure clone() {
        RectangleFigure clone = new RectangleFigure(getId(), getX(), getY(), width, height, getColor());
        clone.setRotation(getRotation());
        clone.setRotationCenter(getRotationCenterX(), getRotationCenterY());
        clone.move(getTranslateX(), getTranslateY());
        clone.setCornerRadius(cornerRadius);
        return clone;
    }

    @Override
    public void restoreState(FigureState state) {
        super.restoreState(state);
        Figure snapshot = state.getSnapshot();
        if (snapshot instanceof RectangleFigure rectangleSnapshot) {
            this.width = rectangleSnapshot.width;
            this.height = rectangleSnapshot.height;
            this.cornerRadius = rectangleSnapshot.cornerRadius;
            notifyFigureChanged();
        }
    }
}
