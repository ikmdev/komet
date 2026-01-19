package dev.ikm.komet.kleditorapp.view.control;

import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorGridNodeModel;
import dev.ikm.komet.layout.editor.model.EditorModelBase;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.model.EditorSectionModel;

import java.util.HashMap;

public class WindowControlFactory {
    private static final HashMap<EditorWindowBaseControl, EditorModelBase> windowControlToModel = new HashMap<>();
    private static final HashMap<EditorModelBase, EditorWindowBaseControl> modelToWindowControl = new HashMap<>();

    public static SectionViewControl createSectionView(EditorSectionModel editorSectionModel) {
        SectionViewControl sectionViewControl = new SectionViewControl();

        sectionViewControl.nameProperty().bindBidirectional(editorSectionModel.nameProperty());
        sectionViewControl.tagTextProperty().bind(editorSectionModel.tagTextProperty());

        sectionViewControl.numberColumnsProperty().bindBidirectional(editorSectionModel.numberColumnsProperty());

        updateMaps(editorSectionModel, sectionViewControl);

        return sectionViewControl;
    }

    public static PatternViewControl createPatternView(EditorPatternModel editorPatternModel) {
        PatternViewControl patternViewControl = new PatternViewControl();

        patternViewControl.titleProperty().bind(editorPatternModel.titleProperty());
        patternViewControl.titleVisibleProperty().bindBidirectional(editorPatternModel.titleVisibleProperty());

        patternViewControl.numberColumnsProperty().bindBidirectional(editorPatternModel.numberColumnsProperty());

        bindGridNodeProperties(editorPatternModel, patternViewControl);

        updateMaps(editorPatternModel, patternViewControl);

        return patternViewControl;
    }

    public static FieldViewControl createFieldView(EditorFieldModel editorFieldModel) {
        FieldViewControl fieldViewControl = new FieldViewControl();
        fieldViewControl.titleProperty().bind(editorFieldModel.titleProperty());
        fieldViewControl.fieldNumberProperty().bind(editorFieldModel.indexProperty().add(1));

        bindGridNodeProperties(editorFieldModel, fieldViewControl);

        updateMaps(editorFieldModel, fieldViewControl);

        return fieldViewControl;
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
