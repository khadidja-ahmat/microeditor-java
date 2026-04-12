package microeditor.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import microeditor.command.AddFigureCommand;
import microeditor.command.AddToolbarFigureCommand;
import microeditor.command.ChangeColorCommand;
import microeditor.command.CommandManager;
import microeditor.command.GroupFiguresCommand;
import microeditor.command.MoveFigureCommand;
import microeditor.command.RemoveFigureCommand;
import microeditor.command.RemoveToolbarFigureCommand;
import microeditor.command.EditFigureCommand;
import microeditor.command.UngroupFigureCommand;
import microeditor.factory.FigureFactory;
import microeditor.model.DrawingDocument;
import microeditor.model.Figure;
import microeditor.model.FigureState;
import microeditor.model.GroupFigure;
import microeditor.model.Selection;
import microeditor.model.Toolbar;
import microeditor.persistence.ApplicationState;
import microeditor.persistence.PersistenceService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class MicroEditorApp extends Application {
    private static final String PROJECT_EXTENSION = "*.microeditor";
    private static final Path TOOLBAR_STATE_PATH = Path.of(
            System.getProperty("user.home"), ".microeditor-toolbar.microeditor");

    private final FigureFactory figureFactory = new FigureFactory();
    private final PersistenceService persistenceService = new PersistenceService();
    private final CommandManager commandManager = new CommandManager();
    private final Selection selection = new Selection();

    private DrawingDocument drawingDocument = new DrawingDocument();
    private Toolbar toolbarModel = new Toolbar();
    private WhiteboardView whiteboardView;
    private ToolPaletteView toolPaletteView;
    private Stage mainStage;
    private ColorPicker colorPicker;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        loadToolbarState();
        seedToolbarModel();
        createWhiteboardView();
        registerDocumentObserver();

        BorderPane rootPane = new BorderPane();
        rootPane.setTop(createTopActionBar());
        toolPaletteView = new ToolPaletteView(toolbarModel, this::deleteSelectedFigures, this::removeToolbarFigure);
        rootPane.setLeft(toolPaletteView.getPane());
        rootPane.setCenter(whiteboardView.getPane());
        registerToolbarObserver();

        Scene scene = new Scene(rootPane, 780, 540);
        stage.setTitle("Micro editor de figures");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> saveToolbarState());
        stage.show();
        whiteboardView.render();
    }

    private void createWhiteboardView() {
        whiteboardView = new WhiteboardView(drawingDocument, selection);
        whiteboardView.setToolDropHandler(this::addFigureFromToolbar);
        whiteboardView.setMoveHandler(this::moveSelectedFigures);
        whiteboardView.setToolbarDropChecker((screenX, screenY) ->
                toolPaletteView != null && toolPaletteView.containsScreenPoint(screenX, screenY));
        whiteboardView.setTrashDropChecker((screenX, screenY) ->
                toolPaletteView != null && toolPaletteView.trashContainsScreenPoint(screenX, screenY));
        whiteboardView.setAddSelectionToToolbarAction(this::addSelectionToToolbar);
        whiteboardView.setGroupAction(this::groupSelectedFigures);
        whiteboardView.setUngroupAction(this::ungroupSelectedGroup);
        whiteboardView.setEditAction(this::editSelectedFigure);
        whiteboardView.setDeleteAction(this::deleteSelectedFigures);
    }

    private HBox createTopActionBar() {
        Button saveButton = createToolbarIconButton(ToolbarIcons.saveIcon(), "Save");
        saveButton.setOnAction(event -> saveProject());

        Button loadButton = createToolbarIconButton(ToolbarIcons.loadIcon(), "Load");
        loadButton.setOnAction(event -> loadProject());

        Button undoButton = createToolbarIconButton(ToolbarIcons.undoIcon(), "Undo");
        undoButton.setOnAction(event -> {
            commandManager.undo();
            whiteboardView.render();
            toolPaletteView.render();
        });

        Button redoButton = createToolbarIconButton(ToolbarIcons.redoIcon(), "Redo");
        redoButton.setOnAction(event -> {
            commandManager.redo();
            whiteboardView.render();
            toolPaletteView.render();
        });

        Button groupButton = new Button("Group");
        groupButton.setOnAction(event -> groupSelectedFigures());

        Button ungroupButton = new Button("De-group");
        ungroupButton.setOnAction(event -> ungroupSelectedGroup());

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> editSelectedFigure());

        colorPicker = new ColorPicker(Color.web("#4A90E2"));
        colorPicker.setOnAction(event -> changeSelectedColor());

        HBox actionBar = new HBox(8, saveButton, loadButton, undoButton, redoButton,
                groupButton, ungroupButton, editButton, colorPicker);
        actionBar.setPadding(new Insets(8));
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #c7c7c7;");
        return actionBar;
    }

    private Button createToolbarIconButton(Node icon, String helpText) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setTooltip(new Tooltip(helpText));
        button.setMinSize(30, 28);
        button.setPrefSize(30, 28);
        button.setPadding(new Insets(3));
        button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #b5b5b5; -fx-border-radius: 4; -fx-background-radius: 4;");
        return button;
    }

    private void addFigureFromToolbar(String toolName, double x, double y) {
        Figure figure = createFigureFromTool(toolName, x, y);
        if (figure == null) {
            return;
        }

        commandManager.executeCommand(new AddFigureCommand(drawingDocument, figure));
        selection.clear();
        selection.select(figure);
        whiteboardView.render();
    }

    private Figure createFigureFromTool(String toolName, double x, double y) {
        if (toolName.startsWith(ToolPaletteView.TOOL_PREFIX)) {
            int index = Integer.parseInt(toolName.substring(ToolPaletteView.TOOL_PREFIX.length()));
            Figure figure = toolbarModel.createFigure(index);
            figure.setPosition(x, y);
            return figure;
        }
        return null;
    }

    private void addSelectionToToolbar() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        if (selectedFigures.isEmpty()) {
            return;
        }

        if (selectedFigures.size() == 1) {
            commandManager.executeCommand(new AddToolbarFigureCommand(toolbarModel, selectedFigures.get(0)));
        } else {
            GroupFigure group = new GroupFigure("toolbar-group-" + UUID.randomUUID());
            for (Figure selectedFigure : selectedFigures) {
                group.addFigure(selectedFigure);
            }
            commandManager.executeCommand(new AddToolbarFigureCommand(toolbarModel, group));
        }

        toolPaletteView.render();
    }

    private void removeToolbarFigure(int index) {
        if (index < 0 || index >= toolbarModel.getFigures().size()) {
            return;
        }

        commandManager.executeCommand(new RemoveToolbarFigureCommand(toolbarModel, index));
        toolPaletteView.render();
    }

    private void moveSelectedFigures(double deltaX, double deltaY) {
        for (Figure selectedFigure : selection.getSelectedFigures()) {
            commandManager.executeCommand(new MoveFigureCommand(selectedFigure, deltaX, deltaY));
        }
    }

    private void groupSelectedFigures() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        if (selectedFigures.size() < 2) {
            return;
        }

        GroupFigure group = new GroupFigure("group-" + UUID.randomUUID());
        commandManager.executeCommand(new GroupFiguresCommand(drawingDocument, group, selectedFigures));
        selection.clear();
        selection.select(group);
        whiteboardView.render();
    }

    private void ungroupSelectedGroup() {
        Figure selectedFigure = getSingleSelectedFigure();
        if (!(selectedFigure instanceof GroupFigure group)) {
            return;
        }

        commandManager.executeCommand(new UngroupFigureCommand(drawingDocument, group));
        selection.clear();
        whiteboardView.render();
    }

    private void editSelectedFigure() {
        Figure selectedFigure = getSingleSelectedFigure();
        if (selectedFigure == null) {
            return;
        }

        FigureState oldState = selectedFigure.saveState();
        Optional<FigureState> newState = FigureEditDialog.show(selectedFigure, whiteboardView::render);
        if (newState.isPresent()) {
            selectedFigure.restoreState(oldState);
            commandManager.executeCommand(new EditFigureCommand(selectedFigure, oldState, newState.get()));
        } else {
            selectedFigure.restoreState(oldState);
        }
        whiteboardView.render();
    }

    private void changeSelectedColor() {
        String selectedColor = toHex(colorPicker.getValue());
        for (Figure selectedFigure : selection.getSelectedFigures()) {
            commandManager.executeCommand(new ChangeColorCommand(selectedFigure, selectedColor));
        }
        whiteboardView.render();
    }

    private void deleteSelectedFigures() {
        List<Figure> selectedFigures = List.copyOf(selection.getSelectedFigures());
        for (Figure selectedFigure : selectedFigures) {
            commandManager.executeCommand(new RemoveFigureCommand(drawingDocument, selectedFigure));
        }
        selection.clear();
        whiteboardView.render();
    }

    private Figure getSingleSelectedFigure() {
        if (selection.getSelectedFigures().size() != 1) {
            return null;
        }
        return selection.getSelectedFigures().iterator().next();
    }

    private void saveProject() {
        File file = chooseProjectFile(true);
        if (file == null) {
            return;
        }

        try {
            persistenceService.save(drawingDocument, toolbarModel, file.toPath());
        } catch (IOException exception) {
            showError("Save failed", exception);
        }
    }

    private void loadProject() {
        File file = chooseProjectFile(false);
        if (file == null) {
            return;
        }

        try {
            ApplicationState state = persistenceService.load(file.toPath());
            drawingDocument = state.getDocument();
            toolbarModel = state.getToolbar();
            selection.clear();
            commandManager.clear();
            whiteboardView.setDocument(drawingDocument);
            toolPaletteView.setToolbar(toolbarModel);
            registerDocumentObserver();
            registerToolbarObserver();
            whiteboardView.render();
        } catch (IOException | ClassNotFoundException exception) {
            showError("Load failed", exception);
        }
    }

    private void loadToolbarState() {
        if (!Files.exists(TOOLBAR_STATE_PATH)) {
            return;
        }

        try {
            toolbarModel = persistenceService.load(TOOLBAR_STATE_PATH).getToolbar();
        } catch (IOException | ClassNotFoundException exception) {
            showError("Toolbar load failed", exception);
        }
    }

    private void saveToolbarState() {
        try {
            persistenceService.save(new DrawingDocument(), toolbarModel, TOOLBAR_STATE_PATH);
        } catch (IOException exception) {
            showError("Toolbar save failed", exception);
        }
    }

    private File chooseProjectFile(boolean saveMode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(saveMode ? "Save project" : "Load project");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Microeditor project", PROJECT_EXTENSION));
        if (saveMode) {
            fileChooser.setInitialFileName("drawing.microeditor");
            return fileChooser.showSaveDialog(mainStage);
        }
        return fileChooser.showOpenDialog(mainStage);
    }

    private void registerDocumentObserver() {
        drawingDocument.addObserver(event -> whiteboardView.render());
    }

    private void registerToolbarObserver() {
        toolbarModel.addObserver(event -> {
            if (toolPaletteView != null) {
                toolPaletteView.render();
            }
        });
    }

    private void seedToolbarModel() {
        if (toolbarModel.getFigures().isEmpty()) {
            toolbarModel.addFigure(figureFactory.createRectangle(0, 0, 120, 70));
            toolbarModel.addFigure(figureFactory.createPolygon(0, 0, 5, 45));
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
