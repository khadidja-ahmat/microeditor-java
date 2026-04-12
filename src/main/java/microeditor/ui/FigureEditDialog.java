package microeditor.ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import microeditor.model.Figure;
import microeditor.model.FigureState;
import microeditor.model.PolygonFigure;
import microeditor.model.RectangleFigure;

import java.util.Optional;

public final class FigureEditDialog {
    private static final ButtonType APPLY_BUTTON = new ButtonType("Appliquer", ButtonBar.ButtonData.APPLY);

    private FigureEditDialog() {
    }

    public static Optional<FigureState> show(Figure figure) {
        return show(figure, () -> { });
    }

    public static Optional<FigureState> show(Figure figure, Runnable applyAction) {
        Dialog<Boolean> dialog = createDialog(figure, applyAction);
        Optional<Boolean> result = dialog.showAndWait();
        if (result.orElse(false)) {
            return Optional.of(figure.saveState());
        }
        return Optional.empty();
    }

    private static Dialog<Boolean> createDialog(Figure figure, Runnable applyAction) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit figure");
        dialog.getDialogPane().getButtonTypes().addAll(APPLY_BUTTON, ButtonType.OK, ButtonType.CANCEL);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);
        formGrid.setPadding(new Insets(12));

        TextField xField = addDoubleField(formGrid, "Position X", figure.getX(), 0);
        TextField yField = addDoubleField(formGrid, "Position Y", figure.getY(), 1);
        TextField rotationField = addDoubleField(formGrid, "Rotation", figure.getRotation(), 2);
        TextField centerXField = addDoubleField(formGrid, "Center X", figure.getRotationCenterX(), 3);
        TextField centerYField = addDoubleField(formGrid, "Center Y", figure.getRotationCenterY(), 4);
        TextField translateXField = addDoubleField(formGrid, "Translate X", figure.getTranslateX(), 5);
        TextField translateYField = addDoubleField(formGrid, "Translate Y", figure.getTranslateY(), 6);
        TextField colorField = addTextField(formGrid, "Color", figure.getColor(), 7);

        TextField widthField = null;
        TextField heightField = null;
        TextField cornerRadiusField = null;
        TextField sideCountField = null;
        TextField sideLengthField = null;

        int nextRow = 8;
        if (figure instanceof RectangleFigure rectangleFigure) {
            widthField = addDoubleField(formGrid, "Width", rectangleFigure.getWidth(), nextRow++);
            heightField = addDoubleField(formGrid, "Height", rectangleFigure.getHeight(), nextRow++);
            cornerRadiusField = addDoubleField(formGrid, "Corner radius", rectangleFigure.getCornerRadius(), nextRow);
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            sideCountField = addTextField(formGrid, "Side count", String.valueOf(polygonFigure.getSideCount()), nextRow++);
            sideLengthField = addDoubleField(formGrid, "Side length", polygonFigure.getSideLength(), nextRow);
        }

        TextField finalWidthField = widthField;
        TextField finalHeightField = heightField;
        TextField finalCornerRadiusField = cornerRadiusField;
        TextField finalSideCountField = sideCountField;
        TextField finalSideLengthField = sideLengthField;

        dialog.getDialogPane().setContent(formGrid);
        Button applyButton = (Button) dialog.getDialogPane().lookupButton(APPLY_BUTTON);
        applyButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (applyFields(figure, xField, yField, rotationField, centerXField, centerYField,
                    translateXField, translateYField, colorField, finalWidthField, finalHeightField,
                    finalCornerRadiusField, finalSideCountField, finalSideLengthField)) {
                applyAction.run();
            }
            event.consume();
        });

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!applyFields(figure, xField, yField, rotationField, centerXField, centerYField,
                    translateXField, translateYField, colorField, finalWidthField, finalHeightField,
                    finalCornerRadiusField, finalSideCountField, finalSideLengthField)) {
                event.consume();
            }
        });

        dialog.setResultConverter(buttonType -> {
            return buttonType == ButtonType.OK;
        });
        return dialog;
    }

    private static boolean applyFields(
            Figure figure,
            TextField xField,
            TextField yField,
            TextField rotationField,
            TextField centerXField,
            TextField centerYField,
            TextField translateXField,
            TextField translateYField,
            TextField colorField,
            TextField widthField,
            TextField heightField,
            TextField cornerRadiusField,
            TextField sideCountField,
            TextField sideLengthField) {
        FigureState stateBeforeApply = figure.saveState();
        try {
            applyCommonFields(figure, xField, yField, rotationField,
                    centerXField, centerYField, translateXField, translateYField, colorField);
            applySpecificFields(figure, widthField, heightField,
                    cornerRadiusField, sideCountField, sideLengthField);
            return true;
        } catch (IllegalArgumentException exception) {
            figure.restoreState(stateBeforeApply);
            showInvalidValueAlert(exception);
            return false;
        }
    }

    private static TextField addDoubleField(GridPane formGrid, String label, double value, int row) {
        return addTextField(formGrid, label, String.valueOf(value), row);
    }

    private static TextField addTextField(GridPane formGrid, String label, String value, int row) {
        Label fieldLabel = new Label(label);
        TextField textField = new TextField(value);
        formGrid.add(fieldLabel, 0, row);
        formGrid.add(textField, 1, row);
        return textField;
    }

    private static void applyCommonFields(
            Figure figure,
            TextField xField,
            TextField yField,
            TextField rotationField,
            TextField centerXField,
            TextField centerYField,
            TextField translateXField,
            TextField translateYField,
            TextField colorField) {
        figure.setPosition(readDouble(xField), readDouble(yField));
        figure.setRotation(readDouble(rotationField));
        figure.setRotationCenter(readDouble(centerXField), readDouble(centerYField));
        figure.move(readDouble(translateXField) - figure.getTranslateX(),
                readDouble(translateYField) - figure.getTranslateY());
        figure.setColor(colorField.getText().trim());
    }

    private static void applySpecificFields(
            Figure figure,
            TextField widthField,
            TextField heightField,
            TextField cornerRadiusField,
            TextField sideCountField,
            TextField sideLengthField) {
        if (figure instanceof RectangleFigure rectangleFigure) {
            rectangleFigure.setWidth(readDouble(widthField));
            rectangleFigure.setHeight(readDouble(heightField));
            rectangleFigure.setCornerRadius(readDouble(cornerRadiusField));
        }
        if (figure instanceof PolygonFigure polygonFigure) {
            polygonFigure.setSideCount(readInteger(sideCountField));
            polygonFigure.setSideLength(readDouble(sideLengthField));
        }
    }

    private static double readDouble(TextField textField) {
        return Double.parseDouble(textField.getText().trim());
    }

    private static int readInteger(TextField textField) {
        return Integer.parseInt(textField.getText().trim());
    }

    private static void showInvalidValueAlert(Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid value");
        alert.setHeaderText("Invalid value");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
