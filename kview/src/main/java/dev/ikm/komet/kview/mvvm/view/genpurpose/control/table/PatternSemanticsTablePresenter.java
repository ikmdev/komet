package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.framework.Identicon;
import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableSemanticVersion;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.komet.kview.mvvm.view.genpurpose.control.AbstractPatternSemanticsPresenter;
import dev.ikm.komet.layout.PatternSemanticsPresenter;
import dev.ikm.komet.layout.editor.model.EditorFieldModel;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.komet.layout.editor.property.KlPropertySet;
import dev.ikm.komet.layout.editor.property.TablePatternProperties;
import dev.ikm.tinkar.component.FeatureDefinition;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.Field;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternSemanticsTablePresenter extends AbstractPatternSemanticsPresenter implements PatternSemanticsPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(PatternSemanticsTablePresenter.class);

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

        bindFactoryProperties();
    }

    /**
     * Binds the table control to the Table factory's configurable properties held on the model, so
     * the journal renders according to the values the user set in the editor's properties pane.
     */
    private void bindFactoryProperties() {
        KlPropertySet factoryProperties = editorPatternModel.getFactoryProperties();
        if (factoryProperties instanceof TablePatternProperties tableProperties) {
            patternSemanticsControl.headerVisibleProperty().bind(tableProperties.headerVisibleProperty());
            patternSemanticsControl.gridLinesVisibleProperty().bind(tableProperties.gridLinesVisibleProperty());
        }
    }

    private PatternSemanticsTableControl createTableControl(ViewProperties viewProperties) {
        final PatternSemanticsTableControl patternSemanticsControl;
        patternSemanticsControl = PatternSemanticsTableControl.create(viewProperties.calculator());

        ViewCalculator viewCalculator = viewProperties.calculator();

        patternSemanticsControl.setEntityProxyToComponentItem(entityProxy -> {
            String description = viewCalculator.languageCalculator()
                    .getDescriptionTextOrNid(entityProxy.nid());
            Image identicon = Identicon.generateIdenticonImage(entityProxy.publicId());

            boolean isConcept = EntityHandle.get(entityProxy.publicId()).isConcept();

            return new ComponentItem(description, identicon, entityProxy.publicId(), isConcept);
        });
        patternSemanticsControl.setNidToComponentItem(nid -> {
            EntityProxy entityProxy = EntityProxy.make(nid);
            Image icon = Identicon.generateIdenticonImage(entityProxy.publicId());

            String description = viewCalculator.languageCalculator()
                    .getDescriptionTextOrNid(entityProxy.nid());

            boolean isConcept = EntityHandle.get(entityProxy.publicId()).isConcept();

            return new ComponentItem(description, icon, entityProxy.publicId(), isConcept);
        });

        return patternSemanticsControl;
    }

    @Override
    public void addNewSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) {
        // Read fields directly from the observable semantic without creating an editable version.
        // Creating an editable version via composeSemantic()/getEditableVersion() would track this
        // semantic in the shared composer's transaction, causing a spurious new version to be
        // written for every displayed semantic when any single semantic is committed.
        Latest<ObservableSemanticVersion> latestVersion =
                latestVersionForView(semanticEntity, viewProperties.calculator());
        if (latestVersion.isAbsent()) {
            // The view holds no version of this semantic. The Standard factory renders a
            // "No version for view" line in the semantic's place; a table row has no equivalent —
            // its cells are per column, and the columns come from the fields of the rows — so the
            // semantic is left out of the table rather than listed as a blank row.
            LOG.debug("Semantic {} has no version for the current view; not listed in the table",
                    semanticEntity.nid());
            return;
        }

        // The outer loop is the editor model's fields so the row's fields — and with them the table's
        // columns — come out in the order the author arranged the fields in, not in pattern order.
        List<SemanticField> fields = new ArrayList<>();
        for (EditorFieldModel editorFieldModel : editorPatternModel.getVisibleFields()) {
            for (ObservableField<?> observableField : latestVersion.get().fields()) {
                if (observableField.indexInPattern() == editorFieldModel.getIndex()) {
                    SemanticField field = createField(observableField, editorFieldModel);
                    fields.add(field);
                }
            }
        }
        SemanticRow semanticRow = new SemanticRow(fields);

        semanticRow.setSemanticNid(semanticEntity.nid());

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