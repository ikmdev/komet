package dev.ikm.komet.kleditorapp.view.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Editor-side representation of a pattern as an author-sized grid of field tiles — the design-time
 * counterpart of the journal control produced by {@code KlPatternSemanticsStandardFactory}. Its body is
 * an {@link EditorGridPane} of {@link FieldViewControl}s whose column count is author-controlled via
 * {@link #numberColumnsProperty()}.
 */
public class PatternStandardEditorControl extends PatternEditorControlBase {
    public static final String DEFAULT_STYLE_CLASS = "pattern-view";

    private final EditorGridPane gridPane = new EditorGridPane();

    PatternStandardEditorControl() {
        Bindings.bindContent(gridPane.getItems(), getFields());

        fields.addListener(this::onFieldsChanged);

        gridPane.numberColumnsProperty().bind(numberColumns);
        gridPane.setHgap(5);
        gridPane.setVgap(0);
        gridPane.setOnShouldDragAndDropRearrange(gridBaseControl -> gridBaseControl instanceof FieldViewControl);

        setContent(gridPane);

        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    private void onFieldsChanged(ListChangeListener.Change<? extends FieldViewControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(fieldViewControl -> fieldViewControl.setParentPattern(this));
            }
        }
    }

    // -- number columns
    private final IntegerProperty numberColumns = new SimpleIntegerProperty(1);
    public int getNumberColumns() { return numberColumns.get(); }
    public IntegerProperty numberColumnsProperty() { return numberColumns; }
    public void setNumberColumns(int number) { numberColumns.set(number); }

    // -- fields
    private final ObservableList<FieldViewControl> fields = FXCollections.observableArrayList();
    public ObservableList<FieldViewControl> getFields() { return fields; }
}
