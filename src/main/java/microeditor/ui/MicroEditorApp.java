package microeditor.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import microeditor.command.CommandManager;
import microeditor.factory.FigureFactory;
import microeditor.model.DrawingDocument;
import microeditor.model.Selection;
import microeditor.model.Toolbar;
import microeditor.persistence.ApplicationState;
import microeditor.persistence.PersistenceService;
import microeditor.persistence.ToolbarStateStore;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX entry point that wires the editor model, commands, views and persistence.
 */
public final class MicroEditorApp extends Application {
    private static final String PROJECT_EXTENSION = "*.microeditor";

    private final FigureFactory figureFactory = new FigureFactory();
    private final PersistenceService persistenceService = new PersistenceService();
    private final ToolbarStateStore toolbarStateStore = new ToolbarStateStore(persistenceService);
    private final CommandManager commandManager = new CommandManager();
    private final Selection selection = new Selection();
    private final EditorCommands editorCommands = new EditorCommands(
            figureFactory, commandManager, selection, new DrawingDocument(), new Toolbar());

    private WhiteboardView whiteboardView;
    private ToolPaletteView toolPaletteView;
    private ActionBarView actionBarView;
    private Stage mainStage;

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Builds and displays the main editor window.
     *
     * @param stage primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        mainStage = stage;
        loadToolbarState();
        editorCommands.seedToolbarIfEmpty();
        createWhiteboardView();
        registerDocumentObserver();

        BorderPane rootPane = new BorderPane();
        actionBarView = createActionBarView();
        rootPane.setTop(actionBarView.getPane());
        toolPaletteView = new ToolPaletteView(editorCommands.getToolbar(),
                editorCommands::deleteSelectedFigures,
                editorCommands::removeToolbarFigure);
        editorCommands.setViews(whiteboardView, toolPaletteView);
        editorCommands.setSelectedColorSupplier(actionBarView::getSelectedColor);
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
        whiteboardView = new WhiteboardView(editorCommands.getDocument(), selection);
        whiteboardView.setToolDropHandler(editorCommands::addFigureFromToolbar);
        whiteboardView.setMoveHandler(editorCommands::moveSelectedFigures);
        whiteboardView.setToolbarDropChecker((screenX, screenY) ->
                toolPaletteView != null && toolPaletteView.containsScreenPoint(screenX, screenY));
        whiteboardView.setTrashDropChecker((screenX, screenY) ->
                toolPaletteView != null && toolPaletteView.trashContainsScreenPoint(screenX, screenY));
        whiteboardView.setAddSelectionToToolbarAction(editorCommands::addSelectionToToolbar);
        whiteboardView.setGroupAction(editorCommands::groupSelectedFigures);
        whiteboardView.setUngroupAction(editorCommands::ungroupSelectedGroup);
        whiteboardView.setEditAction(editorCommands::editSelectedFigure);
        whiteboardView.setDeleteAction(editorCommands::deleteSelectedFigures);
    }

    private ActionBarView createActionBarView() {
        return new ActionBarView(
                this::saveProject,
                this::loadProject,
                editorCommands::undo,
                editorCommands::redo,
                editorCommands::groupSelectedFigures,
                editorCommands::ungroupSelectedGroup,
                editorCommands::editSelectedFigure,
                editorCommands::changeSelectedColor);
    }

    private void saveProject() {
        File file = chooseProjectFile(true);
        if (file == null) {
            return;
        }

        try {
            persistenceService.save(editorCommands.getDocument(), editorCommands.getToolbar(), file.toPath());
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
            editorCommands.setDocument(state.getDocument());
            editorCommands.setToolbar(state.getToolbar());
            selection.clear();
            editorCommands.clearCommandHistory();
            whiteboardView.setDocument(editorCommands.getDocument());
            toolPaletteView.setToolbar(editorCommands.getToolbar());
            registerDocumentObserver();
            registerToolbarObserver();
            whiteboardView.render();
        } catch (IOException | ClassNotFoundException exception) {
            showError("Load failed", exception);
        }
    }

    private void loadToolbarState() {
        try {
            editorCommands.setToolbar(toolbarStateStore.loadOrDefault(editorCommands.getToolbar()));
        } catch (IOException | ClassNotFoundException exception) {
            showError("Toolbar load failed", exception);
        }
    }

    private void saveToolbarState() {
        try {
            toolbarStateStore.save(editorCommands.getToolbar());
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
        editorCommands.getDocument().addObserver(event -> whiteboardView.render());
    }

    private void registerToolbarObserver() {
        editorCommands.getToolbar().addObserver(event -> {
            if (toolPaletteView != null) {
                toolPaletteView.render();
            }
        });
    }

    private void showError(String title, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
