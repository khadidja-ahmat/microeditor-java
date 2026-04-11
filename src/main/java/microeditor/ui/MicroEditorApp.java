package microeditor.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import microeditor.command.AddFigureCommand;
import microeditor.command.ChangeColorCommand;
import microeditor.command.CommandManager;
import microeditor.command.GroupFiguresCommand;
import microeditor.command.MoveFigureCommand;
import microeditor.command.RemoveFigureCommand;
import microeditor.command.UngroupFigureCommand;
import microeditor.factory.FigureFactory;
import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.GroupFigure;
import microeditor.model.PolygonFigure;
import microeditor.persistence.ApplicationState;
import microeditor.persistence.PersistenceService;
import microeditor.model.RectangleFigure;
import microeditor.model.Selection;
import microeditor.model.Toolbar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class MicroEditorApp extends Application {
    private static final double CANVAS_WIDTH = 900.0;
    private static final double CANVAS_HEIGHT = 600.0;
    private static final String PROJECT_EXTENSION = "*.microeditor";

    private final FigureFactory figureFactory = new FigureFactory();
    private final PersistenceService persistenceService = new PersistenceService();
    private final CommandManager commandManager = new CommandManager();
    private final Pane canvas = new Pane();
    private final Selection selection = new Selection();

    private DrawingDocument document = new DrawingDocument();
    private Toolbar toolbar = new Toolbar();
    private Stage stage;
    private ColorPicker colorPicker;
    private double dragStartX;
    private double dragStartY;
    private int createdFigureCount;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        registerDocumentObserver();
        seedToolbar();

        BorderPane root = new BorderPane();
        root.setTop(createToolbar());
        root.setCenter(canvas);

        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setMinSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setStyle("-fx-background-color: white; -fx-border-color: #c8c8c8;");
        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                selection.clear();
                render();
            }
        });

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Microeditor JavaFX");
        stage.setScene(scene);
        stage.show();
        render();
    }

    private ToolBar createToolbar() {
        Button rectangleButton = new Button("Rectangle");
        rectangleButton.setOnAction(event -> addRectangle());

        Button polygonButton = new Button("Polygon");
        polygonButton.setOnAction(event -> addPolygon());

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> deleteSelectedFigure());

        Button groupButton = new Button("Group");
        groupButton.setOnAction(event -> groupSelectedFigures());

        Button ungroupButton = new Button("Ungroup");
        ungroupButton.setOnAction(event -> ungroupSelectedFigure());

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(event -> {
            commandManager.undo();
            render();
        });

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(event -> {
            commandManager.redo();
            render();
        });

        colorPicker = new ColorPicker(Color.web("#4A90E2"));
        colorPicker.setOnAction(event -> changeSelectedColor());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> saveProject());

        Button loadButton = new Button("Load");
        loadButton.setOnAction(event -> loadProject());

        ToolBar toolBar = new ToolBar(
                rectangleButton,
                polygonButton,
                deleteButton,
                groupButton,
                ungroupButton,
                undoButton,
                redoButton,
                colorPicker,
                saveButton,
                loadButton);
        toolBar.setPadding(new Insets(8));
        return toolBar;
    }

    private void addRectangle() {
        addFigureFromToolbar(0);
    }

    private void addPolygon() {
        addFigureFromToolbar(1);
    }

    private void addFigureFromToolbar(int index) {
        Figure figure = toolbar.createFigure(index);
        double offset = 30.0 * createdFigureCount;
        figure.setPosition(80.0 + offset, 80.0 + offset);
        commandManager.executeCommand(new AddFigureCommand(document, figure));
        selection.clear();
        selection.select(figure);
        createdFigureCount++;
        render();
    }

    private void deleteSelectedFigure() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        for (Figure figure : selectedFigures) {
            commandManager.executeCommand(new RemoveFigureCommand(document, figure));
        }
        selection.clear();
        render();
    }

    private void groupSelectedFigures() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        if (selectedFigures.size() < 2) {
            return;
        }

        GroupFigure group = new GroupFigure("group-" + UUID.randomUUID());
        commandManager.executeCommand(new GroupFiguresCommand(document, group, selectedFigures));
        selection.clear();
        selection.select(group);
        render();
    }

    private void ungroupSelectedFigure() {
        Figure selectedFigure = getSingleSelectedFigure();
        if (!(selectedFigure instanceof GroupFigure group)) {
            return;
        }

        List<Figure> children = List.copyOf(group.getChildren());
        commandManager.executeCommand(new UngroupFigureCommand(document, group));
        selection.clear();
        for (Figure child : children) {
            selection.select(child);
        }
        render();
    }

    private void changeSelectedColor() {
        Color color = colorPicker.getValue();
        String hexColor = toHex(color);
        for (Figure figure : selection.getSelectedFigures()) {
            commandManager.executeCommand(new ChangeColorCommand(figure, hexColor));
        }
        render();
    }

    private void saveProject() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }

        try {
            persistenceService.save(document, toolbar, file.toPath());
        } catch (IOException exception) {
            showError("Save failed", exception);
        }
    }

    private void loadProject() {
        File file = chooseLoadFile();
        if (file == null) {
            return;
        }

        try {
            ApplicationState state = persistenceService.load(file.toPath());
            document = state.getDocument();
            toolbar = state.getToolbar();
            selection.clear();
            commandManager.clear();
            registerDocumentObserver();
            render();
        } catch (IOException | ClassNotFoundException exception) {
            showError("Load failed", exception);
        }
    }

    private File chooseSaveFile() {
        FileChooser chooser = createProjectFileChooser();
        chooser.setTitle("Save project");
        chooser.setInitialFileName("drawing.microeditor");
        return chooser.showSaveDialog(stage);
    }

    private File chooseLoadFile() {
        FileChooser chooser = createProjectFileChooser();
        chooser.setTitle("Load project");
        return chooser.showOpenDialog(stage);
    }

    private FileChooser createProjectFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Microeditor project", PROJECT_EXTENSION));
        return chooser;
    }

    private void render() {
        canvas.getChildren().clear();
        for (Figure figure : document.getFigures()) {
            canvas.getChildren().add(createNode(figure, true, false));
        }
    }

    private Node createNode(Figure figure, boolean interactive, boolean highlighted) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            return createRectangleNode(rectangleFigure, interactive, highlighted);
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            return createPolygonNode(polygonFigure, interactive, highlighted);
        }
        if (figure instanceof GroupFigure groupFigure) {
            Group group = new Group();
            boolean groupHighlighted = highlighted || selection.isSelected(groupFigure);
            for (Figure child : groupFigure.getChildren()) {
                group.getChildren().add(createNode(child, false, groupHighlighted));
            }
            if (interactive) {
                installFigureHandlers(group, groupFigure);
            }
            return group;
        }
        return new Group();
    }

    private Rectangle createRectangleNode(RectangleFigure figure, boolean interactive, boolean highlighted) {
        Rectangle rectangle = new Rectangle(figure.getX(), figure.getY(), figure.getWidth(), figure.getHeight());
        rectangle.setArcWidth(figure.getCornerRadius());
        rectangle.setArcHeight(figure.getCornerRadius());
        styleShape(rectangle, figure, highlighted);
        if (interactive) {
            installFigureHandlers(rectangle, figure);
        }
        return rectangle;
    }

    private Polygon createPolygonNode(PolygonFigure figure, boolean interactive, boolean highlighted) {
        Polygon polygon = new Polygon();
        double radius = figure.getSideLength();
        for (int i = 0; i < figure.getSideCount(); i++) {
            double angle = -Math.PI / 2.0 + (2.0 * Math.PI * i / figure.getSideCount());
            polygon.getPoints().add(figure.getX() + Math.cos(angle) * radius);
            polygon.getPoints().add(figure.getY() + Math.sin(angle) * radius);
        }
        styleShape(polygon, figure, highlighted);
        if (interactive) {
            installFigureHandlers(polygon, figure);
        }
        return polygon;
    }

    private void styleShape(Shape shape, Figure figure, boolean highlighted) {
        boolean selected = highlighted || selection.isSelected(figure);
        shape.setFill(Color.web(figure.getColor()));
        shape.setTranslateX(figure.getTranslateX());
        shape.setTranslateY(figure.getTranslateY());
        shape.setRotate(figure.getRotation());
        shape.setStroke(selected ? Color.BLACK : Color.web("#555555"));
        shape.setStrokeWidth(selected ? 3.0 : 1.0);
    }

    private void installFigureHandlers(Node node, Figure figure) {
        node.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
                if (event.isControlDown()) {
                    toggleSelection(figure);
                } else if (!selection.isSelected(figure)) {
                    selection.clear();
                    selection.select(figure);
                }
                event.consume();
            }
        });

        node.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && selection.isSelected(figure)) {
                double deltaX = event.getSceneX() - dragStartX;
                double deltaY = event.getSceneY() - dragStartY;
                if (deltaX != 0.0 || deltaY != 0.0) {
                    moveSelectedFigures(deltaX, deltaY);
                }
                render();
                event.consume();
            }
        });
    }

    private void toggleSelection(Figure figure) {
        if (selection.isSelected(figure)) {
            selection.unselect(figure);
        } else {
            selection.select(figure);
        }
    }

    private void moveSelectedFigures(double deltaX, double deltaY) {
        for (Figure selectedFigure : selection.getSelectedFigures()) {
            commandManager.executeCommand(new MoveFigureCommand(selectedFigure, deltaX, deltaY));
        }
    }

    private Figure getSingleSelectedFigure() {
        if (selection.getSelectedFigures().size() != 1) {
            return null;
        }
        return selection.getSelectedFigures().iterator().next();
    }

    private void registerDocumentObserver() {
        document.addObserver(event -> render());
    }

    private void seedToolbar() {
        if (toolbar.getFigures().isEmpty()) {
            toolbar.addFigure(figureFactory.createRectangle(0, 0, 140, 90));
            toolbar.addFigure(figureFactory.createPolygon(0, 0, 5, 60));
        }
    }

    private String toHex(Color color) {
        int red = (int) Math.round(color.getRed() * 255.0);
        int green = (int) Math.round(color.getGreen() * 255.0);
        int blue = (int) Math.round(color.getBlue() * 255.0);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    private void showError(String title, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
