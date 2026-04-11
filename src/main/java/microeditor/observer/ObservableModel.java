package microeditor.observer;

public interface ObservableModel {
    void addObserver(ModelObserver observer);

    void removeObserver(ModelObserver observer);
}
