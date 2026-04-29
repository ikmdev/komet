package dev.ikm.komet.kview.mvvm.view.genpurpose.control;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

public class PatternSemanticsDefaultControlSkin extends SkinBase<PatternSemanticsDefaultControl> {
    private final VBox semanticsContainer = new VBox();

    private SemanticDefaultControl previousSemanticControlInEditMode;
    private SemanticDefaultControl previousSemanticControlInPreviewMode;

    /**
     * Constructor for all PatternSemanticsDefaultControlSkin instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public PatternSemanticsDefaultControlSkin(PatternSemanticsDefaultControl control) {
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

    private void onPreviewingSemanticChanged(SemanticDefaultControl semanticViewControl) {
        if (previousSemanticControlInPreviewMode != null) {
            previousSemanticControlInPreviewMode.setPreviewMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setPreviewMode(true);
        }
        previousSemanticControlInPreviewMode = semanticViewControl;
    }

    private void onEditingSemanticChanged(SemanticDefaultControl semanticViewControl) {
        if (previousSemanticControlInEditMode != null) {
            previousSemanticControlInEditMode.setEditMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setEditMode(true);
        }
        previousSemanticControlInEditMode = semanticViewControl;
    }

    private void onSemanticsChanged(ListChangeListener.Change<? extends SemanticDefaultControl> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(this::addSemantic);
            }
            if (change.wasRemoved()) {
                change.getRemoved().forEach(semanticViewControl -> {
                    semanticsContainer.getChildren().remove((semanticViewControl));
                });
            }
        }
    }

    private void addSemantic(SemanticDefaultControl semanticViewControl) {
        if(getSkinnable().getSemantics().indexOf(semanticViewControl) > 0) {
            Separator separator = new Separator();
            semanticsContainer.getChildren().add(separator);
        }
        semanticsContainer.getChildren().add(semanticViewControl);
    }
}