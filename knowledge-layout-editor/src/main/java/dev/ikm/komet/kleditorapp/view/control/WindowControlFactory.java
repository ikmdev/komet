package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.layout.editor.EditorWindowBaseControl;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorGridNodeModel;
import dev.ikm.komet.layout.editor.model.EditorModelBase;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorSupplementalAreaModel;
import dev.ikm.komet.layout.editor.property.StandardPatternProperties;

import java.util.HashMap;

public class WindowControlFactory {
    private static final HashMap<EditorWindowBaseControl, EditorModelBase> windowControlToModel = new HashMap<>();
    private static final HashMap<EditorModelBase, EditorWindowBaseControl> modelToWindowControl = new HashMap<>();

    public static SectionViewControl createSectionView(EditorSectionModel editorSectionModel) {
        SectionViewControl sectionViewControl = new SectionViewControl();

        sectionViewControl.nameProperty().bindBidirectional(editorSectionModel.nameProperty());
        sectionViewControl.tagTextProperty().bind(editorSectionModel.tagTextProperty());

        sectionViewControl.numberColumnsProperty().bindBidirectional(editorSectionModel.numberColumnsProperty());

        sectionViewControl.collapsedProperty().bind(editorSectionModel.startCollapsedProperty());

        updateMaps(editorSectionModel, sectionViewControl);

        return sectionViewControl;
    }

    public static PatternStandardEditorControl createPatternView(EditorPatternModel editorPatternModel) {
        PatternStandardEditorControl patternStandardEditorControl = new PatternStandardEditorControl();

        patternStandardEditorControl.titleProperty().bind(editorPatternModel.titleProperty());
        patternStandardEditorControl.titleVisibleProperty().bindBidirectional(editorPatternModel.titleVisibleProperty());

        // The column count is a Standard-factory property; bind it from the pattern's factory property set.
        if (editorPatternModel.getFactoryProperties() instanceof StandardPatternProperties standardProperties) {
            patternStandardEditorControl.numberColumnsProperty().bindBidirectional(standardProperties.numberColumnsProperty());
        }

        bindGridNodeProperties(editorPatternModel, patternStandardEditorControl);

        updateMaps(editorPatternModel, patternStandardEditorControl);

        // Populate the field tiles from the pattern's fields.
        for (EditorFieldModel fieldModel : editorPatternModel.getFields()) {
            patternStandardEditorControl.getFields().add(createFieldView(fieldModel));
        }

        return patternStandardEditorControl;
    }

    public static PatternTableEditorControl createPatternTableView(EditorPatternModel editorPatternModel) {
        PatternTableEditorControl patternTableViewControl = new PatternTableEditorControl();

        patternTableViewControl.titleProperty().bind(editorPatternModel.titleProperty());
        patternTableViewControl.titleVisibleProperty().bindBidirectional(editorPatternModel.titleVisibleProperty());

        // No numberColumns binding: a table's columns are its fields, not an author-set count.
        bindGridNodeProperties(editorPatternModel, patternTableViewControl);

        updateMaps(editorPatternModel, patternTableViewControl);

        // A table renders each field as a column header (no FieldViewControls).
        for (EditorFieldModel fieldModel : editorPatternModel.getFields()) {
            patternTableViewControl.addColumn(fieldModel.titleProperty());
        }

        return patternTableViewControl;
    }

    public static FieldViewControl createFieldView(EditorFieldModel editorFieldModel) {
        FieldViewControl fieldViewControl = new FieldViewControl();
        fieldViewControl.titleProperty().bind(editorFieldModel.titleProperty());
        fieldViewControl.fieldNumberProperty().bind(editorFieldModel.indexProperty().add(1));

        bindGridNodeProperties(editorFieldModel, fieldViewControl);

        updateMaps(editorFieldModel, fieldViewControl);

        return fieldViewControl;
    }

    public static SupplementalAreaViewControl createSupplementalAreaView(EditorSupplementalAreaModel model) {
        SupplementalAreaViewControl view = new SupplementalAreaViewControl();

        view.titleProperty().bind(model.titleProperty());

        bindGridNodeProperties(model, view);

        updateMaps(model, view);

        return view;
    }

    public static EditorModelBase getModel(EditorWindowBaseControl editorWindowBaseControl) {
        return windowControlToModel.get(editorWindowBaseControl);
    }

    public static EditorWindowBaseControl getView(EditorModelBase editorModelBase) {
        return modelToWindowControl.get(editorModelBase);
    }

    private static void updateMaps(EditorModelBase editorModelBase, EditorWindowBaseControl editorWindowBaseControl) {
        windowControlToModel.put(editorWindowBaseControl, editorModelBase);
        modelToWindowControl.put(editorModelBase, editorWindowBaseControl);
    }

    private static void bindGridNodeProperties(EditorGridNodeModel gridNodeModel, GridBaseControl gridBaseControl) {
        gridBaseControl.columnIndexProperty().bindBidirectional(gridNodeModel.columnIndexProperty());
        gridBaseControl.rowIndexProperty().bindBidirectional(gridNodeModel.rowIndexProperty());
        gridBaseControl.columnSpanProperty().bindBidirectional(gridNodeModel.columnSpanProperty());
        gridBaseControl.rowSpanProperty().bindBidirectional(gridNodeModel.rowSpanProperty());
    }
}
