package microeditor.model;

import microeditor.observer.ModelChangeType;
import microeditor.observer.ModelObserver;
import microeditor.observer.ObservableSupport;

import java.util.Objects;

/**
 * Shared implementation for all figures.
 * The model stays independent from JavaFX: it stores data, not graphical nodes.
 */
public abstract class BaseFigure implements Figure {
    private final String id;
    private transient ObservableSupport observableSupport;
    private double positionX;
    private double positionY;
    private double rotation;
    private double rotationCenterX;
    private double rotationCenterY;
    private double translationX;
    private double translationY;
    private String color;

    protected BaseFigure(String id, double positionX, double positionY, String color) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.positionX = positionX;
        this.positionY = positionY;
        this.rotationCenterX = positionX;
        this.rotationCenterY = positionY;
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.observableSupport = new ObservableSupport(this);
    }

    @Override
    public abstract Figure clone();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getX() {
        return positionX;
    }

    @Override
    public double getY() {
        return positionY;
    }

    @Override
    public void setPosition(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        notifyFigureChanged();
    }

    @Override
    public double getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation = rotation;
        notifyFigureChanged();
    }

    @Override
    public double getRotationCenterX() {
        return rotationCenterX;
    }

    @Override
    public double getRotationCenterY() {
        return rotationCenterY;
    }

    @Override
    public void setRotationCenter(double centerX, double centerY) {
        this.rotationCenterX = centerX;
        this.rotationCenterY = centerY;
        notifyFigureChanged();
    }

    @Override
    public double getTranslateX() {
        return translationX;
    }

    @Override
    public double getTranslateY() {
        return translationY;
    }

    @Override
    public void move(double deltaX, double deltaY) {
        this.translationX += deltaX;
        this.translationY += deltaY;
        notifyFigureChanged();
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public void setColor(String color) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        notifyFigureChanged();
    }

    @Override
    public FigureState saveState() {
        return new FigureState(clone());
    }

    @Override
    public void restoreState(FigureState state) {
        Figure snapshot = state.getSnapshot();
        this.positionX = snapshot.getX();
        this.positionY = snapshot.getY();
        this.rotation = snapshot.getRotation();
        this.rotationCenterX = snapshot.getRotationCenterX();
        this.rotationCenterY = snapshot.getRotationCenterY();
        this.translationX = snapshot.getTranslateX();
        this.translationY = snapshot.getTranslateY();
        this.color = snapshot.getColor();
    }

    @Override
    public void addObserver(ModelObserver observer) {
        getObservableSupport().addObserver(observer);
    }

    @Override
    public void removeObserver(ModelObserver observer) {
        getObservableSupport().removeObserver(observer);
    }

    protected void notifyFigureChanged() {
        getObservableSupport().notifyObservers(ModelChangeType.FIGURE_CHANGED, this);
    }

    private ObservableSupport getObservableSupport() {
        if (observableSupport == null) {
            observableSupport = new ObservableSupport(this);
        }
        return observableSupport;
    }
}
