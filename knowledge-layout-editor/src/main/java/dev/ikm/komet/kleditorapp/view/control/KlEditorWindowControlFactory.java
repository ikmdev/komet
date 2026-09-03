package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.layout.editor.Selectable;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorGridNodeModel;
import dev.ikm.komet.layout.editor.model.EditorModelBase;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;
import dev.ikm.komet.layout.editor.model.EditorSupplementalAreaModel;
import dev.ikm.komet.layout.editor.property.StandardPatternProperties;

import java.util.HashMap;
import java.util.List;

public class KlEditorWindowControlFactory {
    private static final HashMap<Selectable, EditorModelBase> windowControlToModel = new HashMap<>();
    private static final HashMap<EditorModelBase, Selectable> modelToWindowControl = new HashMap<>();

    public static SectionViewControl createSectionView(EditorSectionModel editorSectionModel) {
        SectionViewControl sectionViewControl = new SectionViewControl();

        sectionViewControl.nameProperty().bindBidirectional(editorSectionModel.nameProperty());
        sectionViewControl.tagTextProperty().bind(editorSectionModel.tagTextProperty());

        sectionViewControl.numberColumnsProperty().bindBidirectional(editorSectionModel.numberColumnsProperty());

        sectionViewControl.collapsedProperty().bind(editorSectionModel.startCollapsedProperty());

        updateMaps(editorSectionModel, sectionViewControl);

        return sectionViewControl;
    }

    public static PatternStandardEditorControl createStandardPatternView(EditorPatternModel editorPatternModel) {
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
        for (EditorFieldModel fieldModel : editorPatternModel.getVisibleFields()) {
            patternStandardEditorControl.getFields().add(createFieldView(fieldModel));
        }

        return patternStandardEditorControl;
    }

    public static PatternTableEditorControl createTablePatternView(EditorPatternModel editorPatternModel) {
        PatternTableEditorControl patternTableViewControl = new PatternTableEditorControl();

        patternTableViewControl.titleProperty().bind(editorPatternModel.titleProperty());
        patternTableViewControl.titleVisibleProperty().bindBidirectional(editorPatternModel.titleVisibleProperty());

        // No numberColumns binding: a table's columns are its fields, not an author-set count.
        bindGridNodeProperties(editorPatternModel, patternTableViewControl);

        updateMaps(editorPatternModel, patternTableViewControl);

        // A table renders each field as a column (no FieldViewControls); the column is what the author
        // selects to edit the field.
        for (EditorFieldModel fieldModel : editorPatternModel.getVisibleFields()) {
            patternTableViewControl.getFields().add(createFieldColumn(fieldModel));
        }

        // Dragging a column header is how the author reorders a table pattern's fields, so write the
        // column order back into the model — the order save persists and the journal renders in.
        patternTableViewControl.getFields().subscribe(() -> {
            List<EditorModelBase> fieldOrder = patternTableViewControl.getFields().stream()
                    .map(KlEditorWindowControlFactory::getModel)
                    .toList();
            if (!fieldOrder.equals(editorPatternModel.getVisibleFields())) {
                editorPatternModel.getVisibleFields().setAll(fieldOrder.stream()
                        .map(EditorFieldModel.class::cast)
                        .toList());
            }
        });

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

    /**
     * Creates the column standing for a field in a table pattern view — the table counterpart of
     * {@link #createFieldView(EditorFieldModel)}. A column has no grid position, so only the title is bound.
     */
    public static FieldColumnControl createFieldColumn(EditorFieldModel editorFieldModel) {
        FieldColumnControl fieldColumnControl = new FieldColumnControl();
        fieldColumnControl.textProperty().bind(editorFieldModel.titleProperty());

        updateMaps(editorFieldModel, fieldColumnControl);

        return fieldColumnControl;
    }

    public static SupplementalAreaViewControl createSupplementalAreaView(EditorSupplementalAreaModel model) {
        SupplementalAreaViewControl view = new SupplementalAreaViewControl();

        view.titleProperty().bind(model.titleProperty());

        bindGridNodeProperties(model, view);

        updateMaps(model, view);

        return view;
    }

    public static EditorModelBase getModel(Selectable editorWindowControl) {
        return windowControlToModel.get(editorWindowControl);
    }

    public static Selectable getView(EditorModelBase editorModelBase) {
        return modelToWindowControl.get(editorModelBase);
    }

    private static void updateMaps(EditorModelBase editorModelBase, Selectable editorWindowControl) {
        windowControlToModel.put(editorWindowControl, editorModelBase);
        modelToWindowControl.put(editorModelBase, editorWindowControl);
    }

    private static void bindGridNodeProperties(EditorGridNodeModel gridNodeModel, GridBaseControl gridBaseControl) {
        gridBaseControl.columnIndexProperty().bindBidirectional(gridNodeModel.columnIndexProperty());
        gridBaseControl.rowIndexProperty().bindBidirectional(gridNodeModel.rowIndexProperty());
        gridBaseControl.columnSpanProperty().bindBidirectional(gridNodeModel.columnSpanProperty());
        gridBaseControl.rowSpanProperty().bindBidirectional(gridNodeModel.rowSpanProperty());

        // Not related to grid layout but every GridBaseControl that is going to be in a Section has a required flag.
        gridBaseControl.requiredProperty().bind(gridNodeModel.requiredProperty());
    }
}
