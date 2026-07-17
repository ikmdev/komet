package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

public class PatternSemanticsStandardControlSkin extends SkinBase<PatternSemanticsStandardControl> {
    private final VBox semanticsContainer = new VBox();

    private SemanticStandardControl previousSemanticControlInEditMode;
    private SemanticStandardControl previousSemanticControlInPreviewMode;

    /**
     * Constructor for all PatternSemanticsDefaultControlSkin instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public PatternSemanticsStandardControlSkin(PatternSemanticsStandardControl control) {
        super(control);

        getChildren().add(semanticsContainer);

        // listen to semantics ObservableList
        control.getSemantics().addListener(this::onSemanticsChanged);
        rebuildSemantics();

        control.editingSemanticProperty().subscribe(semanticInEditMode -> onEditingSemanticChanged(semanticInEditMode));
        control.previewingSemanticProperty().subscribe(semanticInPreviewMode -> onPreviewingSemanticChanged(semanticInPreviewMode));

        // CSS
        semanticsContainer.getStyleClass().add("semantics-container");
        control.getStyleClass().add("pattern-container");
    }

    private void onPreviewingSemanticChanged(SemanticStandardControl semanticViewControl) {
        if (previousSemanticControlInPreviewMode != null) {
            previousSemanticControlInPreviewMode.setPreviewMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setPreviewMode(true);
        }
        previousSemanticControlInPreviewMode = semanticViewControl;
    }

    private void onEditingSemanticChanged(SemanticStandardControl semanticViewControl) {
        if (previousSemanticControlInEditMode != null) {
            previousSemanticControlInEditMode.setEditMode(false);
        }

        if (semanticViewControl != null) {
            semanticViewControl.setEditMode(true);
        }
        previousSemanticControlInEditMode = semanticViewControl;
    }

    private void onSemanticsChanged(ListChangeListener.Change<? extends SemanticStandardControl> change) {
        // Rebuild the whole container from the current list rather than mutating it incrementally.
        // This isn't the most performant solution so if there are performance issues we can revisit.
        rebuildSemantics();
    }

    private void rebuildSemantics() {
        semanticsContainer.getChildren().clear();

        var semantics = getSkinnable().getSemantics();
        if (semantics.isEmpty()) {
            // A pattern with no semantic shows a single muted placeholder line where its
            // semantic's fields would render.
            Label noSemanticLabel = new Label("No semantic");
            noSemanticLabel.getStyleClass().add("no-semantic-label");
            semanticsContainer.getChildren().add(noSemanticLabel);
            return;
        }
        for (int i = 0; i < semantics.size(); i++) {
            if (i > 0) {
                semanticsContainer.getChildren().add(new Separator());
            }
            semanticsContainer.getChildren().add(semantics.get(i));
        }
    }
}