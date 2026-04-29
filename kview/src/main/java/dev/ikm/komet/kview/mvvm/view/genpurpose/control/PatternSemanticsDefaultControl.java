package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class PatternSemanticsDefaultControl extends Control {

    private PatternSemanticsDefaultControl() {
    }

    public static PatternSemanticsDefaultControl create() {
        return new PatternSemanticsDefaultControl();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PatternSemanticsDefaultControlSkin(this);
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public final String getTitle() { return title.get(); }
    public final StringProperty titleProperty() { return title; }
    public final void setTitle(String title) { this.title.set(title); }

    // -- semantics
    private final ObservableList<SemanticDefaultControl> semantics = FXCollections.observableArrayList();
    public final ObservableList<SemanticDefaultControl> getSemantics() { return semantics; }

    // -- editing semantic
    private final ObjectProperty<SemanticDefaultControl> editingSemantic = new SimpleObjectProperty<>();
    public SemanticDefaultControl getEditingSemantic() { return editingSemantic.get(); }
    public ObjectProperty<SemanticDefaultControl> editingSemanticProperty() { return editingSemantic; }
    public void setEditingSemantic(SemanticDefaultControl editingSemantic) { this.editingSemantic.set(editingSemantic); }

    // -- previewing semantic
    private final ObjectProperty<SemanticDefaultControl> previewingSemantic = new SimpleObjectProperty<>();
    public SemanticDefaultControl getPreviewingSemantic() { return previewingSemantic.get(); }
    public ObjectProperty<SemanticDefaultControl> previewingSemanticProperty() { return previewingSemantic; }
    public void setPreviewingSemantic(SemanticDefaultControl semanticEntity) { previewingSemantic.set(semanticEntity); }
}