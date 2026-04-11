package microeditor.model;

public final class PolygonFigure extends BaseFigure {
    private static final int MINIMUM_SIDE_COUNT = 3;

    private int sideCount;
    private double sideLength;

    public PolygonFigure(String id, double positionX, double positionY, int sideCount, double sideLength, String color) {
        super(id, positionX, positionY, color);
        validateSideCount(sideCount);
        this.sideCount = sideCount;
        this.sideLength = sideLength;
    }

    public int getSideCount() {
        return sideCount;
    }

    public void setSideCount(int sideCount) {
        validateSideCount(sideCount);
        this.sideCount = sideCount;
        notifyFigureChanged();
    }

    public double getSideLength() {
        return sideLength;
    }

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
