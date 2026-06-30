package dev.ikm.komet.layout.editor;

import javafx.beans.property.BooleanProperty;
import javafx.css.PseudoClass;

/**
 * Something in the KL editor that the user can select so that its properties show in the right
 * properties pane. Implemented both by {@link EditorWindowBaseControl} (sections, patterns, fields
 * and supplemental areas) and by the editor window control itself, so the whole Window can be
 * selected like any other element.
 */
public interface Selectable {
    /**
     * The {@code :selected} CSS pseudo-class that implementers should toggle as their selection state
     * changes, so selection styling is driven the same way across every selectable editor control.
     */
    PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    boolean isSelected();
    void setSelected(boolean selected);
    BooleanProperty selectedProperty();
}