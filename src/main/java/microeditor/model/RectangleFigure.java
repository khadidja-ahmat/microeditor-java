package microeditor.model;

public final class RectangleFigure extends BaseFigure {
    private double width;
    private double height;
    private double cornerRadius;

    public RectangleFigure(String id, double x, double y, double width, double height, String color) {
        super(id, x, y, color);
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
        notifyFigureChanged();
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        notifyFigureChanged();
    }

    public double getCornerRadius() {
        return cornerRadius;
    }

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
