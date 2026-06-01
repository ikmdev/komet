package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.klfields.KlFieldHelper;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.AbstractPatternSemanticsPresenter;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PatternSemanticsStandardPresenter extends AbstractPatternSemanticsPresenter implements PatternSemanticsPresenter {

    /**
     * Given a SemanticEntity what's its associated Semantic Control.
     */
    private final Map<SemanticEntity<SemanticEntityVersion>, SemanticStandardControl> semanticEntityToSemanticView = new HashMap<>();

    private final PatternSemanticsStandardControl patternSemanticsControl;

    private final ObservableComposer composer;
    private final ViewProperties viewProperties;
    private final UUID journalTopic;
    private final EditorPatternModel editorPatternModel;

    public PatternSemanticsStandardPresenter(EditorPatternModel editorPatternModel, ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic) {
        this.composer = composer;
        this.viewProperties = viewProperties;
        this.journalTopic = journalTopic;
        this.editorPatternModel = editorPatternModel;

        patternSemanticsControl = PatternSemanticsStandardControl.create();
    }

    @Override
    public void addNewSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticStandardControl semanticViewControl = createSemanticControl(editorPatternModel, semanticEntity);

        patternSemanticsControl.getSemantics().add(semanticViewControl);
        semanticEntityToSemanticView.put(semanticEntity, semanticViewControl);
    }

    @Override
    public void clearSemantics() {
        patternSemanticsControl.getSemantics().clear();
    }

    @Override
    public void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticStandardControl semanticStandardControl = semanticEntityToSemanticView.get(semanticEntity);
        patternSemanticsControl.setPreviewingSemantic(semanticStandardControl);
    }

    @Override
    public void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticStandardControl semanticStandardControl = semanticEntityToSemanticView.get(semanticEntity);
        patternSemanticsControl.setEditingSemantic(semanticStandardControl);
    }

    @Override
    public Node getView() {
        return patternSemanticsControl;
    }

    private SemanticStandardControl createSemanticControl(EditorPatternModel editorPatternModel, SemanticEntity<SemanticEntityVersion> semanticEntity) {
        SemanticStandardControl semanticViewControl = new SemanticStandardControl();

        semanticEntityToSemanticView.put(semanticEntity, semanticViewControl);

        // Read fields directly from the observable semantic without creating an editable version.
        // Creating an editable version via composeSemantic()/getEditableVersion() would track this
        // semantic in the shared composer's transaction, causing a spurious new version to be
        // written for every displayed semantic when any single semantic is committed.
        ObservableSemanticVersion latestVersion = getObservableSemanticFromSemanticEntity(semanticEntity);

        for (ObservableField<?> observableField : latestVersion.fields()) {
            for (EditorFieldModel editorFieldModel : editorPatternModel.getFields()) {
                if (observableField.indexInPattern() == editorFieldModel.getIndex()) {
                    addFieldView(observableField, editorFieldModel, semanticViewControl);
                }
            }
        }

        semanticViewControl.numberColumnsProperty().bind(editorPatternModel.numberColumnsProperty());

        return semanticViewControl;
    }

    private void addFieldView(ObservableField<?> observableField, EditorFieldModel fieldModel, SemanticStandardControl semanticViewControl) {
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