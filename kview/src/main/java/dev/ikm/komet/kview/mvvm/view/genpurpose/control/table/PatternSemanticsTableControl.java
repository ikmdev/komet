package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.kview.controls.ComponentItem;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.function.Function;

public class PatternSemanticsTableControl extends Control {
    public static final String DEFAULT_STYLE_CLASS = "pattern-semantics-table";

    private PatternSemanticsTableControl() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public static PatternSemanticsTableControl create(ViewCalculator viewCalculator) {
        return new PatternSemanticsTableControl();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PatternSemanticsTableControlSkin(this);
    }

    // -- semantics
    private final ObservableList<SemanticRow> semantics = FXCollections.observableArrayList();
    public final ObservableList<SemanticRow> getSemantics() { return semantics; }

    // -- editing semantic
    private final ObjectProperty<SemanticRow> editingSemantic = new SimpleObjectProperty<>();
    public SemanticRow getEditingSemantic() { return editingSemantic.get(); }
    public ObjectProperty<SemanticRow> editingSemanticProperty() { return editingSemantic; }
    public void setEditingSemantic(SemanticRow editingSemantic) { this.editingSemantic.set(editingSemantic); }

    // -- previewing semantic
    private final ObjectProperty<SemanticRow> previewingSemantic = new SimpleObjectProperty<>();
    public SemanticRow getPreviewingSemantic() { return previewingSemantic.get(); }
    public ObjectProperty<SemanticRow> previewingSemanticProperty() { return previewingSemantic; }
    public void setPreviewingSemantic(SemanticRow semanticEntity) { previewingSemantic.set(semanticEntity); }

    // -- entity proxy to component item
    private final ObjectProperty<Function<EntityProxy, ComponentItem>> entityProxyToComponentItem = new SimpleObjectProperty<>();
    public Function<EntityProxy, ComponentItem> getEntityProxyToComponentItem() { return entityProxyToComponentItem.get(); }
    public ObjectProperty<Function<EntityProxy, ComponentItem>> entityProxyToComponentItemProperty() { return entityProxyToComponentItem; }
    public void setEntityProxyToComponentItem(Function<EntityProxy, ComponentItem> entityProxyToComponentItem) { this.entityProxyToComponentItem.set(entityProxyToComponentItem); }

    // -- nid to component item
    private final ObjectProperty<Function<Integer, ComponentItem>> nidToComponentItem = new SimpleObjectProperty<>();
    public Function<Integer, ComponentItem> getNidToComponentItem() { return nidToComponentItem.get(); }
    public ObjectProperty<Function<Integer, ComponentItem>> nidToComponentItemProperty() { return nidToComponentItem; }
    public void setNidToComponentItem(Function<Integer, ComponentItem> nidToComponentItem) { this.nidToComponentItem.set(nidToComponentItem); }
}