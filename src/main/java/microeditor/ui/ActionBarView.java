package microeditor.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Top action bar containing project, history, grouping, editing and color controls.
 */
final class ActionBarView {
    private final HBox pane;
    private final ColorPicker colorPicker = new ColorPicker(Color.web("#4A90E2"));

    /**
     * Creates the action bar and wires each button to its editor action.
     */
    ActionBarView(
            Runnable saveAction,
            Runnable loadAction,
            Runnable undoAction,
            Runnable redoAction,
            Runnable groupAction,
            Runnable ungroupAction,
            Runnable editAction,
            Runnable colorAction) {
        Button saveButton = createIconButton(ToolbarIcons.saveIcon(), "Save", saveAction);
        Button loadButton = createIconButton(ToolbarIcons.loadIcon(), "Load", loadAction);
        Button undoButton = createIconButton(ToolbarIcons.undoIcon(), "Undo", undoAction);
        Button redoButton = createIconButton(ToolbarIcons.redoIcon(), "Redo", redoAction);
        Button groupButton = createTextButton("Group", groupAction);
        Button ungroupButton = createTextButton("De-group", ungroupAction);
        Button editButton = createTextButton("Edit", editAction);
        colorPicker.setOnAction(event -> colorAction.run());

        pane = new HBox(8, saveButton, loadButton, undoButton, redoButton,
                groupButton, ungroupButton, editButton, colorPicker);
        pane.setPadding(new Insets(8));
        pane.setAlignment(Pos.CENTER_LEFT);
        pane.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #c7c7c7;");
    }

    /**
     * Returns the JavaFX container used by this view.
     *
     * @return action bar pane
     */
    HBox getPane() {
        return pane;
    }

    /**
     * Returns the currently selected color.
     *
     * @return selected JavaFX color
     */
    Color getSelectedColor() {
        return colorPicker.getValue();
    }

    private Button createIconButton(Node icon, String helpText, Runnable action) {
        Button button = new Button();
        button.setGraphic(icon);
        button.setTooltip(new Tooltip(helpText));
        button.setMinSize(30, 28);
        button.setPrefSize(30, 28);
        button.setPadding(new Insets(3));
        button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #b5b5b5; -fx-border-radius: 4; -fx-background-radius: 4;");
        button.setOnAction(event -> Objects.requireNonNull(action).run());
        return button;
    }

    private Button createTextButton(String label, Runnable action) {
        Button button = new Button(label);
        button.setOnAction(event -> Objects.requireNonNull(action).run());
        return button;
    }
}
