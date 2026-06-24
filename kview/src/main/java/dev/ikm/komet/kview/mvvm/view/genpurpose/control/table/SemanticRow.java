package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;

public class SemanticRow {
    private final List<SemanticField> fields;

    public SemanticRow(List<SemanticField> fields) {
        this.fields = fields;
    }

    public List<SemanticField> getFields() { return fields; }

    // -- semantic nid
    private final ObjectProperty<Integer> semanticNid = new SimpleObjectProperty<>();
    public ObjectProperty<Integer> semanticNidProperty() { return semanticNid; }
    public Integer getSemanticNid() { return semanticNid.get(); }
    public void setSemanticNid(Integer publicId) { this.semanticNid.set(publicId); }

    // -- edit mode
    private final BooleanProperty editMode = new SimpleBooleanProperty();
    public boolean isEditMode() { return editMode.get(); }
    public BooleanProperty editModeProperty() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode.set(editMode); }

    // -- preview mode
    private final BooleanProperty previewMode = new SimpleBooleanProperty();
    public boolean isPreviewMode() { return previewMode.get(); }
    public BooleanProperty previewModeProperty() { return previewMode; }
    public void setPreviewMode(boolean previewMode) { this.previewMode.set(previewMode); }
}