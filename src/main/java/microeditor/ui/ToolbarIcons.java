package microeditor.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public final class ToolbarIcons {
    private static final Color ICON_COLOR = Color.web("#222222");

    private ToolbarIcons() {
    }

    public static Node saveIcon() {
        return createIcon("M17 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V7L17 3ZM12 19C10.3 19 9 17.7 9 16C9 14.3 10.3 13 12 13C13.7 13 15 14.3 15 16C15 17.7 13.7 19 12 19ZM15 9H5V5H15V9Z");
    }

    public static Node loadIcon() {
        return createIcon("M19 20H5C3.9 20 3 19.1 3 18V6C3 4.9 3.9 4 5 4H10L12 6H19C20.1 6 21 6.9 21 8V18C21 19.1 20.1 20 19 20ZM12 17L17 12H14V9H10V12H7L12 17Z");
    }

    public static Node undoIcon() {
        return createIcon("M12 5C9.6 5 7.4 6.1 6 7.8V5H4V12H11V10H7.2C8.3 8.2 10.1 7 12 7C15.3 7 18 9.7 18 13C18 16.3 15.3 19 12 19C9.8 19 7.9 17.8 6.8 16H4.6C5.8 18.9 8.7 21 12 21C16.4 21 20 17.4 20 13C20 8.6 16.4 5 12 5Z");
    }

    public static Node redoIcon() {
        return createIcon("M12 5C14.4 5 16.6 6.1 18 7.8V5H20V12H13V10H16.8C15.7 8.2 13.9 7 12 7C8.7 7 6 9.7 6 13C6 16.3 8.7 19 12 19C14.2 19 16.1 17.8 17.2 16H19.4C18.2 18.9 15.3 21 12 21C7.6 21 4 17.4 4 13C4 8.6 7.6 5 12 5Z");
    }

    public static Node trashIcon() {
        return createIcon("M6 19C6 20.1 6.9 21 8 21H16C17.1 21 18 20.1 18 19V7H6V19ZM8 9H16V19H8V9ZM15.5 4L14.5 3H9.5L8.5 4H5V6H19V4H15.5Z");
    }

    private static Node createIcon(String pathContent) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathContent);
        icon.setFill(ICON_COLOR);
        icon.setScaleX(0.82);
        icon.setScaleY(0.82);
        return new Group(icon);
    }
}
