package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class PatternSemanticsTableControl extends Control {
    public static final String DEFAULT_STYLE_CLASS = "pattern-semantics-table";

    private final ViewCalculator viewCalculator;

    private PatternSemanticsTableControl(ViewCalculator viewCalculator) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.viewCalculator = viewCalculator;
    }

    public static PatternSemanticsTableControl create(ViewCalculator viewCalculator) {
        return new PatternSemanticsTableControl(viewCalculator);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PatternSemanticsTableControlSkin(this);
    }

    // -- view calculator
    public ViewCalculator getViewCalculator() { return viewCalculator; }

    // -- title
    private final StringProperty title = new SimpleStringProperty();
    public final String getTitle() { return title.get(); }
    public final StringProperty titleProperty() { return title; }
    public final void setTitle(String title) { this.title.set(title); }

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
}