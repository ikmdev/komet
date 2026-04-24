package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

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
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.FieldRecord;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.UUID;

public class PatternSemanticsControlSkin extends SkinBase<PatternSemanticsControl> {
    /**
     * Given a SemanticEntity what's its associated SemanticView.
     */
    private final HashMap<SemanticEntity<SemanticEntityVersion>, SemanticViewControl> semanticEntityToSemanticView = new HashMap<>();

    private final VBox semanticsContainer = new VBox();

    private ObservableComposer composer;

    private SemanticViewControl previousSemanticControlInEditMode;
    private SemanticViewControl previousSemanticControlInPreviewMode;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public PatternSemanticsControlSkin(PatternSemanticsControl control) {
        super(control);

        getChildren().add(semanticsContainer);

        // listen to semantics ObservableList
        control.getSemantics().addListener(this::onSemanticsChanged);
        control.getSemantics().forEach(this::addSemantic);

        control.editingSemanticProperty().subscribe(semanticInEditMode -> onEditingSemanticChanged(semanticInEditMode));
        control.previewingSemanticProperty().subscribe(semanticInPreviewMode -> onPreviewingSemanticChanged(semanticInPreviewMode));

        // CSS
        semanticsContainer.getStyleClass().add("semantics-container");
        control.getStyleClass().add("pattern-container");
    }

    private void onPreviewingSemanticChanged(SemanticEntity<SemanticEntityVersion> semanticInPreviewMode) {
        SemanticViewControl semanticViewControl = semanticEntityToSemanticView.get(semanticInPreviewMode);

        if (previousSemanticControlInPreviewMode != null) {
            previousSemanticControlInPreviewMode.setPreviewMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setPreviewMode(true);
        }
        previousSemanticControlInPreviewMode = semanticViewControl;
    }

    private void onEditingSemanticChanged(SemanticEntity<SemanticEntityVersion> semanticInEditMode) {
        SemanticViewControl semanticViewControl = semanticEntityToSemanticView.get(semanticInEditMode);

        if (previousSemanticControlInEditMode != null) {
            previousSemanticControlInEditMode.setEditMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setEditMode(true);
        }
        previousSemanticControlInEditMode = semanticViewControl;
    }

    private void onSemanticsChanged(ListChangeListener.Change<? extends SemanticEntity<SemanticEntityVersion>> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::addSemantic);
            }
            if (change.wasRemoved()) {
                change.getRemoved().forEach(semantic -> {
                    SemanticViewControl semanticViewControl = semanticEntityToSemanticView.get(semantic);
                    semanticsContainer.getChildren().remove((semanticViewControl));
                });
            }
        }
    }

    private void addSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        EditorPatternModel editorPatternModel = getSkinnable().getEditorPatternModel();

        SemanticViewControl semanticViewControl = new SemanticViewControl();

        semanticEntityToSemanticView.put(semanticEntity, semanticViewControl);

        ObservableComposer composer = getSkinnable().getComposer();

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

        if(getSkinnable().getSemantics().indexOf(semanticEntity) > 0) {
            Separator separator = new Separator();
            semanticsContainer.getChildren().add(separator);
        }
        semanticsContainer.getChildren().add(semanticViewControl);
    }

    private void addFieldView(ObservableField<?> observableField, EditorFieldModel fieldModel, SemanticViewControl semanticViewControl) {
        ViewProperties viewProperties = getSkinnable().getViewProperties();
        UUID journalTopic = getSkinnable().getJournalTopic();

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