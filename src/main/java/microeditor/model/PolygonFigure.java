package microeditor.model;

/**
 * Regular polygon figure defined by a side count and a radius-like side length.
 */
public final class PolygonFigure extends BaseFigure {
    private static final int MINIMUM_SIDE_COUNT = 3;

    private int sideCount;
    private double sideLength;

    /**
     * Creates a polygon figure.
     *
     * @param id unique figure identifier
     * @param positionX horizontal position
     * @param positionY vertical position
     * @param sideCount number of sides, at least three
     * @param sideLength distance used to draw the polygon vertices
     * @param color fill color in web format
     */
    public PolygonFigure(String id, double positionX, double positionY, int sideCount, double sideLength, String color) {
        super(id, positionX, positionY, color);
        validateSideCount(sideCount);
        this.sideCount = sideCount;
        this.sideLength = sideLength;
    }

    /**
     * Returns the number of polygon sides.
     *
     * @return number of sides
     */
    public int getSideCount() {
        return sideCount;
    }

    /**
     * Sets the number of polygon sides.
     *
     * @param sideCount number of sides, at least three
     */
    public void setSideCount(int sideCount) {
        validateSideCount(sideCount);
        this.sideCount = sideCount;
        notifyFigureChanged();
    }

    /**
     * Returns the side length used for drawing.
     *
     * @return side length
     */
    public double getSideLength() {
        return sideLength;
    }

    /**
     * Sets the side length used for drawing.
     *
     * @param sideLength side length
     */
    public void setSideLength(double sideLength) {
        this.sideLength = sideLength;
        notifyFigureChanged();
    }

    @Override
    public Figure clone() {
        PolygonFigure clone = new PolygonFigure(getId(), getX(), getY(), sideCount, sideLength, getColor());
        clone.setRotation(getRotation());
        clone.setRotationCenter(getRotationCenterX(), getRotationCenterY());
        clone.move(getTranslateX(), getTranslateY());
        return clone;
    }

    @Override
    public void restoreState(FigureState state) {
        super.restoreState(state);
        Figure snapshot = state.getSnapshot();
        if (snapshot instanceof PolygonFigure polygonSnapshot) {
            this.sideCount = polygonSnapshot.sideCount;
            this.sideLength = polygonSnapshot.sideLength;
            notifyFigureChanged();
        }
    }

    private void validateSideCount(int sideCount) {
        if (sideCount < MINIMUM_SIDE_COUNT) {
            throw new IllegalArgumentException("A polygon must have at least " + MINIMUM_SIDE_COUNT + " sides.");
        }
    }
}
