package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.layout.editor.Selectable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableColumn;

/**
 * Editor-side representation of a field of a pattern displayed as a table: the column that stands for
 * the field. It is the table counterpart of {@link FieldViewControl} — selecting the column selects the
 * field — but, a column not being a node, it is a {@link TableColumn} rather than an
 * {@link dev.ikm.komet.layout.editor.EditorWindowBaseControl}. Selection is reflected with a
 * {@value #SELECTED_STYLE_CLASS} style class, which the table propagates to the column's header (and cells).
 */
public class FieldColumnControl extends TableColumn<Object, Object> implements Selectable {
    public static final String SELECTED_STYLE_CLASS = "selected";

    /** Wider than the JavaFX default (80px) so the field name in the header is readable at design time. */
    private static final double PREFERRED_WIDTH = 180;

    FieldColumnControl() {
        // A design-time table: pressing a header selects the column's field rather than sorting.
        setSortable(false);
        setPrefWidth(PREFERRED_WIDTH);
    }

    // -- selected
    private final BooleanProperty selected = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (get()) {
                getStyleClass().add(SELECTED_STYLE_CLASS);
            } else {
                getStyleClass().remove(SELECTED_STYLE_CLASS);
            }
        }
    };
    @Override public boolean isSelected() { return selected.get(); }
    @Override public BooleanProperty selectedProperty() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected.set(selected); }
}
