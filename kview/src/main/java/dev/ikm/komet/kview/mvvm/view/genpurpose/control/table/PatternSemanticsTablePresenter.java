package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableEntityHandle;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservablePattern;
import dev.ikm.komet.framework.observable.ObservableSemantic;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternSemanticsTablePresenter implements PatternSemanticsPresenter {

    private final ObservableComposer composer;
    private final ViewProperties viewProperties;
    private final EditorPatternModel editorPatternModel;

    private final PatternSemanticsTableControl patternSemanticsControl;

    private final Map<SemanticEntity<SemanticEntityVersion>, SemanticRow> semanticEntityToSemanticRow = new HashMap<>();

    private SemanticRow previousSemanticRowPreviewMode;
    private SemanticRow previousSemanticRowEditMode;

    public PatternSemanticsTablePresenter(EditorPatternModel editorPatternModel, ViewProperties viewProperties,
                                          ObservableComposer composer) {
        this.composer = composer;
        this.viewProperties = viewProperties;
        this.editorPatternModel = editorPatternModel;

        patternSemanticsControl = createTableControl(viewProperties);
    }

    private PatternSemanticsTableControl createTableControl(ViewProperties viewProperties) {
        final PatternSemanticsTableControl patternSemanticsControl;
        patternSemanticsControl = PatternSemanticsTableControl.create(viewProperties.calculator());

        ViewCalculator viewCalculator = viewProperties.calculator();

        patternSemanticsControl.setEntityProxyToComponentItem(entityProxy -> {
            String description = viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());
            Image identicon = Identicon.generateIdenticonImage(entityProxy.publicId());
            ComponentItem componentItem = new ComponentItem(description, identicon);
            return componentItem;
        });
        patternSemanticsControl.setNidToComponentItem(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

            String description = viewCalculator.languageCalculator()
                    .getFullyQualifiedDescriptionTextWithFallbackOrNid(entityProxy.nid());

            return new ComponentItem(description, icon);
        });

        return patternSemanticsControl;
    }

    @Override
    public void addNewSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        ObservableEntity referencedComponent = ObservableEntityHandle.get(semanticEntity.referencedComponent().nid()).expectEntity();
        ObservablePattern pattern = ObservableEntityHandle.get(semanticEntity.pattern().nid()).expectPattern();
        ObservableComposer.EntityComposer<ObservableSemanticVersion.Editable, ObservableSemantic> semanticEditor
                = composer.composeSemantic(semanticEntity.publicId(), referencedComponent, pattern);

        // Get editable version with cached editing capabilities
        ObservableSemanticVersion.Editable editableVersion = semanticEditor.getEditableVersion();

        ObservableList<ObservableField.Editable<?>> editableFields = editableVersion.getEditableFields();

        List<SemanticField> fields = new ArrayList<>();
        for (int i = 0; i < editableFields.size(); ++i) {
            ObservableField.Editable observableField = editableFields.get(i);

            for (EditorFieldModel editorFieldModel : editorPatternModel.getFields()) {
                if (observableField.getFieldIndex() == editorFieldModel.getIndex()) {
                    SemanticField field = createField(observableField.getObservableFeature(), editorFieldModel);
                    fields.add(field);
                }
            }
        }
        SemanticRow semanticRow = new SemanticRow(fields);

        Image semanticIdenticon = Identicon.generateIdenticonImage(semanticEntity.publicId());
        semanticRow.setIdenticon(semanticIdenticon);

        semanticEntityToSemanticRow.put(semanticEntity, semanticRow);

        patternSemanticsControl.getSemantics().add(semanticRow);
    }

    @Override
    public void clearSemantics() {
        patternSemanticsControl.getSemantics().clear();
    }

    @Override
    public void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        if (previousSemanticRowPreviewMode != null) {
            previousSemanticRowPreviewMode.setPreviewMode(false);
        }

        SemanticRow semanticRow = semanticEntityToSemanticRow.get(semanticEntity);
        if (semanticRow != null) {
            semanticRow.setPreviewMode(true);
        }

        previousSemanticRowPreviewMode = semanticRow;
    }

    @Override
    public void setEditingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        if (previousSemanticRowEditMode != null) {
            previousSemanticRowEditMode.setEditMode(false);
        }

        SemanticRow semanticRow = semanticEntityToSemanticRow.get(semanticEntity);
        if (semanticRow != null) {
            semanticRow.setEditMode(true);
        }

        previousSemanticRowEditMode = semanticRow;
    }

    @Override
    public Node getView() {
        return patternSemanticsControl;
    }

    private SemanticField<?> createField(ObservableField<?> observableField, EditorFieldModel fieldModel) {
        Field<?> field = observableField.field();
        ObservableView observableView = viewProperties.nodeView();

        final FeatureDefinition featureDef = field.fieldDefinition(observableView.calculator());

        int dataType = featureDef.dataTypeNid();
        String fieldTitle = observableView.getDescriptionTextOrNid(featureDef.meaningNid());
        String fieldPurpose = observableView.getDescriptionTextOrNid(featureDef.purposeNid());
        return new SemanticField(observableField, dataType, fieldTitle, fieldPurpose);
    }
}