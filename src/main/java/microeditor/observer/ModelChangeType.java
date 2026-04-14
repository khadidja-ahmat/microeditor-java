package microeditor.observer;

/**
 * Types of model changes that can be sent to observers.
 */
public enum ModelChangeType {
    FIGURE_ADDED,
    FIGURE_REMOVED,
    FIGURES_CLEARED,
    FIGURE_CHANGED,
    SELECTION_CHANGED,
    TEMPLATE_ADDED,
    TEMPLATE_REMOVED
}
