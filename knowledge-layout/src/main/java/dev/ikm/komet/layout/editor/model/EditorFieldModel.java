package dev.ikm.komet.layout.editor.model;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.FieldDefinitionRecord;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorFieldModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternModel.class);

    private final ViewCalculator viewCalculator;
    private final FieldDefinitionRecord fieldDefinitionRecord;

    public EditorFieldModel(ViewCalculator viewCalculator, FieldDefinitionRecord fieldDefinitionRecord) {
        this.viewCalculator = viewCalculator;
        this.fieldDefinitionRecord = fieldDefinitionRecord;

        title.set(fieldDefinitionRecord.meaning().description());
        index.set(fieldDefinitionRecord.indexInPattern());
    }

    public void load(KometPreferences patternPreferences, ViewCalculator viewCalculator) {
        final KometPreferences fieldPreferences = patternPreferences.node(String.valueOf(getIndex()));
        loadGridNodeDetails(fieldPreferences);
    }

    public void save(KometPreferences patternPreferences) {
        KometPreferences fieldPreferences = patternPreferences.node(String.valueOf(fieldDefinitionRecord.indexInPattern()));
        saveGridNodeDetails(fieldPreferences);
    }

    // -- title
    private ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    public String getTitle() { return title.get(); }
    public ReadOnlyStringProperty titleProperty() { return title.getReadOnlyProperty(); }

    // -- index
    private ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
    public int getIndex() { return index.get(); }
    public ReadOnlyIntegerProperty indexProperty() { return index.getReadOnlyProperty(); }
}
