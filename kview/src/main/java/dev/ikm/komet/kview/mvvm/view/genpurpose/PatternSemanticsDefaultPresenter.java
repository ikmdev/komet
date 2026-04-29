package dev.ikm.komet.kview.mvvm.view.genpurpose;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.PatternSemanticsDefaultControl;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.SemanticDefaultControl;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PatternSemanticsDefaultPresenter implements PatternSemanticsPresenter {

    /**
     * Given a SemanticEntity what's its associated Semantic Control.
     */
    private final Map<SemanticEntity<SemanticEntityVersion>, SemanticDefaultControl> semanticEntityToSemanticView = new HashMap<>();

    private final PatternSemanticsDefaultControl patternSemanticsControl;

    private final ObservableComposer composer;
    private final ViewProperties viewProperties;
    private final UUID journalTopic;
    private final EditorPatternModel editorPatternModel;

    public PatternSemanticsDefaultPresenter(EditorPatternModel editorPatternModel, ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic) {
        this.composer = composer;
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;
        this.editorPatternModel = editorPatternModel;

        patternSemanticsControl = PatternSemanticsDefaultControl.create();
    }

    @Override
    public void createSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticDefaultControl semanticViewControl = createSemanticControl(editorPatternModel, semanticEntity);

        patternSemanticsControl.getSemantics().add(semanticViewControl);
        semanticEntityToSemanticView.put(semanticEntity, semanticViewControl);
    }

    @Override
    public void clearSemantics() {
        patternSemanticsControl.getSemantics().clear();
    }

    @Override
    public void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticDefaultControl semanticDefaultControl = semanticEntityToSemanticView.get(semanticEntity);
        patternSemanticsControl.setPreviewingSemantic(semanticDefaultControl);
    }

    @Override
    public void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticDefaultControl semanticDefaultControl = semanticEntityToSemanticView.get(semanticEntity);
        patternSemanticsControl.setEditingSemantic(semanticDefaultControl);
    }

    @Override
    public Node getView() {
        return patternSemanticsControl;
    }

    private SemanticDefaultControl createSemanticControl(EditorPatternModel editorPatternModel, SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticDefaultControl semanticViewControl = new SemanticDefaultControl();

        semanticEntityToSemanticView.put(semanticEntity, semanticViewControl);

        ObservableEntity referencedComponent = ObservableEntityHandle.get(semanticEntity.referencedComponent().nid()).expectEntity();
        ObservablePattern pattern = ObservableEntityHandle.get(semanticEntity.pattern().nid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor
                = composer.composeSemantic(semanticEntity.publicId(), referencedComponent, pattern);

        // Get editable version with cached editing capabilities
        ObservableSemanticVersion.Editable editableVersion = semanticEditor.getEditableVersion();

        ObservableList<ObservableField.Editable<?>> editableFields = editableVersion.getEditableFields();

        for (int i = 0; i < editableFields.size(); ++i) {
            ObservableField.Editable observableField = editableFields.get(i);

            for (EditorFieldModel editorFieldModel : editorPatternModel.getFields()) {
                if (observableField.getFieldIndex() == editorFieldModel.getIndex()) {
                    addFieldView(observableField.getObservableFeature(), editorFieldModel, semanticViewControl);
                }
            }
        }

        semanticViewControl.numberColumnsProperty().bind(editorPatternModel.numberColumnsProperty());

        return semanticViewControl;
    }

    private void addFieldView(ObservableField<?> observableField, EditorFieldModel fieldModel, SemanticDefaultControl semanticViewControl) {
        Field<?> field = observableField.field();

        // Generate node using the underlying ObservableField (read-only view)
        // This was throwing a cast exception, expecting KLReadOnlyBaseControl.
        Node baseControl = KlFieldHelper.createReadOnlyKlField(
                (FieldRecord<?>) field,
                observableField, // Use underlying ObservableField for display
                viewProperties,
                null,
                journalTopic
        );

        fieldModel.rowIndexProperty().subscribe(newRowIndex -> {
            GridPane.setRowIndex(baseControl, newRowIndex.intValue());
        });

        fieldModel.columnIndexProperty().subscribe(newColumnIndex -> {
            GridPane.setColumnIndex(baseControl, newColumnIndex.intValue());
        });

        fieldModel.columnSpanProperty().subscribe(newColumnSpan -> {
            GridPane.setColumnSpan(baseControl, newColumnSpan.intValue());
        });

        semanticViewControl.getFields().add((KLReadOnlyBaseControl) baseControl);
    }
}