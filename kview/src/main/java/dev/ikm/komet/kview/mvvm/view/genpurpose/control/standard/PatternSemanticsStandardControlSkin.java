package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

public class PatternSemanticsStandardControlSkin extends SkinBase<PatternSemanticsStandardControl> {
    private final VBox semanticsContainer = new VBox();

    // Hosting the semantics in a ScrollPane keeps the view's min height small, so the user can
    // shrink the section/window below the content height and scroll instead — matching the
    // table view, whose TableView scrolls internally.
    private final ScrollPane scrollPane = new ScrollPane(semanticsContainer);

    private SemanticStandardControl previousSemanticControlInEditMode;
    private SemanticStandardControl previousSemanticControlInPreviewMode;

    /**
     * Constructor for all PatternSemanticsDefaultControlSkin instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public PatternSemanticsStandardControlSkin(PatternSemanticsStandardControl control) {
        super(control);

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("transparent-scroll");
        getChildren().add(scrollPane);

        // ScrollPane's skin caches its viewport as a bitmap, which drops LCD subpixel
        // antialiasing and makes the field text render bolder. Uncache it so the text renders
        // the same as it did without the ScrollPane.
        scrollPane.skinProperty().subscribe(skin -> {
            if (skin != null) {
                Node viewport = scrollPane.lookup(".viewport");
                if (viewport != null) {
                    viewport.setCache(false);
                }
            }
        });

        // listen to semantics ObservableList
        control.getSemantics().addListener(this::onSemanticsChanged);
        rebuildSemantics();

        control.editingSemanticProperty().subscribe(semanticInEditMode -> onEditingSemanticChanged(semanticInEditMode));
        control.previewingSemanticProperty().subscribe(semanticInPreviewMode -> onPreviewingSemanticChanged(semanticInPreviewMode));

        // CSS
        semanticsContainer.getStyleClass().add("semantics-container");
        control.getStyleClass().add("pattern-container");
    }

    /**
     * SkinBase's default preferred-height measurement ignores the control's own insets, but this
     * control carries asymmetric CSS padding (.pattern-container in kview.css), so the default
     * under-reports by the net inset. The section grid sizes this control's row to exactly the
     * reported pref (see SectionTitledPaneSkin), and the shortfall showed as a ScrollPane
     * scrollbar over a few phantom pixels — so report the true content-plus-insets height.
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        double contentWidth = width < 0 ? -1 : width - leftInset - rightInset;
        return topInset + scrollPane.prefHeight(contentWidth) + bottomInset;
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