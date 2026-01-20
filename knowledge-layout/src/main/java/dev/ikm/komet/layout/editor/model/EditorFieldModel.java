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

/**
 * Semantics from Patterns have fields and this represents a field. It has properties like the title of the Field and
 * its index.
 */
public class EditorFieldModel extends EditorGridNodeModel {
    private static final Logger LOG = LoggerFactory.getLogger(EditorPatternModel.class);

    private final ViewCalculator viewCalculator;
    private final FieldDefinitionRecord fieldDefinitionRecord;

    /**
     * Creates a EditorFieldModel given the passed in FieldDefinitionRecord from the database.
     *
     * @param viewCalculator the view calculator
     * @param fieldDefinitionRecord the FieldDefinitionRecord from the database
     */
    public EditorFieldModel(ViewCalculator viewCalculator, FieldDefinitionRecord fieldDefinitionRecord) {
        this.viewCalculator = viewCalculator;
        this.fieldDefinitionRecord = fieldDefinitionRecord;

        title.set(fieldDefinitionRecord.meaning().description());
        index.set(fieldDefinitionRecord.indexInPattern());
        dataTypeNid.set(fieldDefinitionRecord.dataTypeNid());
    }

    /**
     * Loads and sets up the Field given an instance of KometPreferences (stored preferences).
     *
     * @param patternPreferences the stored preferences pointing to the Field
     * @param viewCalculator the view calculator
     */
    public void load(KometPreferences patternPreferences, ViewCalculator viewCalculator) {
        final KometPreferences fieldPreferences = patternPreferences.node(String.valueOf(getIndex()));
        loadGridNodeDetails(fieldPreferences);
    }

    /**
     * Saves the Field into KometPreferences (stored preferences).
     *
     * @param patternPreferences the stored preferences pointing to the Field
     */
    public void save(KometPreferences patternPreferences) {
        KometPreferences fieldPreferences = patternPreferences.node(String.valueOf(fieldDefinitionRecord.indexInPattern()));
        saveGridNodeDetails(fieldPreferences);
    }

    /*******************************************************************************
     *                                                                             *
     * Properties                                                                  *
     *                                                                             *
     ******************************************************************************/

    // -- title
    /**
     * The title of the Field.
     */
    private ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    public String getTitle() { return title.get(); }
    public ReadOnlyStringProperty titleProperty() { return title.getReadOnlyProperty(); }

    // -- index
    /**
     * The index of the field in the Pattern.
     */
    private ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
    public int getIndex() { return index.get(); }
    public ReadOnlyIntegerProperty indexProperty() { return index.getReadOnlyProperty(); }

    // -- data type nid
    private ReadOnlyIntegerWrapper dataTypeNid = new ReadOnlyIntegerWrapper();
    public int getDataTypeNid() { return dataTypeNid.get(); }
    public ReadOnlyIntegerProperty dataTypeNidProperty() { return dataTypeNid.getReadOnlyProperty(); }
}
