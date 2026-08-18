package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class PatternSemanticsStandardControl extends Control {

    private PatternSemanticsStandardControl() {
    }

    public static PatternSemanticsStandardControl create() {
        return new PatternSemanticsStandardControl();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PatternSemanticsStandardControlSkin(this);
    }

    // -- semantics
    private final ObservableList<SemanticStandardControl> semantics = FXCollections.observableArrayList();
    public final ObservableList<SemanticStandardControl> getSemantics() { return semantics; }

    // -- editing semantic
    private final ObjectProperty<SemanticStandardControl> editingSemantic = new SimpleObjectProperty<>();
    public SemanticStandardControl getEditingSemantic() { return editingSemantic.get(); }
    public ObjectProperty<SemanticStandardControl> editingSemanticProperty() { return editingSemantic; }
    public void setEditingSemantic(SemanticStandardControl editingSemantic) { this.editingSemantic.set(editingSemantic); }

    // -- previewing semantic
    private final ObjectProperty<SemanticStandardControl> previewingSemantic = new SimpleObjectProperty<>();
    public SemanticStandardControl getPreviewingSemantic() { return previewingSemantic.get(); }
    public ObjectProperty<SemanticStandardControl> previewingSemanticProperty() { return previewingSemantic; }
    public void setPreviewingSemantic(SemanticStandardControl semanticEntity) { previewingSemantic.set(semanticEntity); }
}