package dev.ikm.komet.kleditorapp.view.control;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.layout.Region;

public abstract class EditorWindowBaseControl extends Region {
    public static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    public static final String DEFAULT_STYLE_CLASS = "editor-window-control";

    protected EditorWindowBaseControl() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public abstract void delete();

    // -- selection
    private BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }
    };
    public boolean isSelected() { return selected.get(); }
    public BooleanProperty selectedProperty() { return selected; }
    public void setSelected(boolean selected) { this.selected.set(selected); }
}