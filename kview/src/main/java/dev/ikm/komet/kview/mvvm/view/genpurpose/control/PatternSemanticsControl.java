package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import dev.ikm.komet.framework.observable.ObservableComposer;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.layout.editor.model.EditorPatternModel;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.UUID;

public class PatternSemanticsControl extends Control {

    private final EditorPatternModel editorPatternModel;
    private final EntityFacade referenceComponent;
    private final ViewProperties viewProperties;
    private final ObservableComposer composer;
    private final UUID journalTopic;

    private PatternSemanticsControl(EditorPatternModel editorPatternModel, EntityFacade referenceComponent,
                                    ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic) {
        this.editorPatternModel = editorPatternModel;
        this.referenceComponent = referenceComponent;
        this.viewProperties = viewProperties;
        this.composer = composer;
        this.journalTopic = journalTopic;
    }

    public static PatternSemanticsControl create(EditorPatternModel editorPatternModel, EntityFacade referenceComponent,
                                                 ViewProperties viewProperties, ObservableComposer composer, UUID journalTopic) {
        return new PatternSemanticsControl(editorPatternModel, referenceComponent, viewProperties, composer, journalTopic);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PatternSemanticsControlSkin(this);
    }

    // -- editor pattern model
    public EditorPatternModel getEditorPatternModel() { return editorPatternModel; }

    // -- reference component
    public EntityFacade getReferenceComponent() { return referenceComponent; }

    // -- view properties
    public ViewProperties getViewProperties() { return viewProperties; }

    // -- composer
    public ObservableComposer getComposer() { return composer; }

    // -- journal topic
    public UUID getJournalTopic() { return journalTopic; }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public final String getTitle() { return title.get(); }
    public final StringProperty titleProperty() { return title; }
    public final void setTitle(String title) { this.title.set(title); }

    // -- semantics
    private final ObservableList<SemanticEntity<SemanticEntityVersion>> semantics = FXCollections.observableArrayList();
    public final ObservableList<SemanticEntity<SemanticEntityVersion>> getSemantics() { return semantics; }

    // -- editing semantic
    private final ObjectProperty<SemanticEntity<SemanticEntityVersion>> editingSemantic = new SimpleObjectProperty<>();
    public SemanticEntity<SemanticEntityVersion> getEditingSemantic() { return editingSemantic.get(); }
    public ObjectProperty<SemanticEntity<SemanticEntityVersion>> editingSemanticProperty() { return editingSemantic; }
    public void setEditingSemantic(SemanticEntity<SemanticEntityVersion> editingSemantic) { this.editingSemantic.set(editingSemantic); }

    // -- previewing semantic
    private final ObjectProperty<SemanticEntity<SemanticEntityVersion>> previewingSemantic = new SimpleObjectProperty<>();
    public SemanticEntity<SemanticEntityVersion> getPreviewingSemantic() { return previewingSemantic.get(); }
    public ObjectProperty<SemanticEntity<SemanticEntityVersion>> previewingSemanticProperty() { return previewingSemantic; }
    public void setPreviewingSemantic(SemanticEntity<SemanticEntityVersion> semanticEntity) { previewingSemantic.set(semanticEntity); }
}