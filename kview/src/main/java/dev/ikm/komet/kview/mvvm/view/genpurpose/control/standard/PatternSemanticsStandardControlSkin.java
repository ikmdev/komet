package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;

public class PatternSemanticsStandardControlSkin extends SkinBase<PatternSemanticsStandardControl> {

    /**
     * Active while no separator is drawn between the semantics. The gap between semantics is sized
     * for the separator that normally sits in it, so kview.css tightens it when there is none — the
     * compact list the author turned the separators off to get.
     */
    private static final PseudoClass SEPARATOR_HIDDEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("separator-hidden");

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
        // A ScrollPane's own minimum height (36px) is taller than a one-line semantic, and a child
        // is never laid out below its minimum, so it would overflow the view. The view's minimum is
        // computed below instead.
        scrollPane.setMinHeight(0);
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

        // Drives both the separators built into the container and, through the pseudo-class, the
        // spacing around them. Also does the initial build.
        control.separatorVisibleProperty().subscribe(this::onSeparatorVisibleChanged);
        onSeparatorVisibleChanged();

        control.editingSemanticProperty().subscribe(semanticInEditMode -> onEditingSemanticChanged(semanticInEditMode));
        control.previewingSemanticProperty().subscribe(semanticInPreviewMode -> onPreviewingSemanticChanged(semanticInPreviewMode));
        control.revealedSemanticProperty().subscribe(this::onRevealedSemanticChanged);

        // CSS
        semanticsContainer.getStyleClass().add("semantics-container");
        control.getStyleClass().add("pattern-container");
    }

    /**
     * Scrolls the semantics list so the revealed semantic is in view, then clears the request so the
     * next reveal of the same semantic fires again. The semantic's position is only known once laid
     * out, so the scroll waits for the next pulse.
     */
    private void onRevealedSemanticChanged(SemanticStandardControl semantic) {
        if (semantic == null) {
            return;
        }
        Platform.runLater(() -> {
            double contentHeight = semanticsContainer.getBoundsInLocal().getHeight();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            double semanticTop = semantic.getBoundsInParent().getMinY();
            double scrollableHeight = contentHeight - viewportHeight;
            scrollPane.setVvalue(scrollableHeight <= 0 ? 0 : Math.min(1, semanticTop / scrollableHeight));
            getSkinnable().revealSemantic(null);
        });
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
        // Measured from the semantics rather than from the ScrollPane: a ScrollPane's preferred
        // height is not its content's (it reports a fixed 36px for a one-line semantic), which
        // left every pattern view carrying about 14px of blank space under its last semantic.
        Insets scrollInsets = scrollPane.getInsets();
        double contentWidth = width < 0 ? -1
                : width - leftInset - rightInset - scrollInsets.getLeft() - scrollInsets.getRight();
        return topInset + scrollInsets.getTop() + semanticsContainer.prefHeight(contentWidth)
                + scrollInsets.getBottom() + bottomInset;
    }

    /**
     * Small, so a section can shrink below its content and the semantics scroll instead, and never
     * more than the content wants, so a short view is not padded up to a minimum.
     */
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset,
                                      double bottomInset, double leftInset) {
        double contentWidth = width < 0 ? -1 : width - leftInset - rightInset;
        double scrollPaneMin = topInset + scrollPane.minHeight(contentWidth) + bottomInset;
        return Math.min(scrollPaneMin, computePrefHeight(width, topInset, rightInset, bottomInset, leftInset));
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

    private void onSeparatorVisibleChanged() {
        getSkinnable().pseudoClassStateChanged(SEPARATOR_HIDDEN_PSEUDO_CLASS, !getSkinnable().isSeparatorVisible());
        rebuildSemantics();
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
            Label noSemanticLabel = new Label("[empty]");
            noSemanticLabel.getStyleClass().add("no-semantic-label");
            semanticsContainer.getChildren().add(noSemanticLabel);
            return;
        }
        boolean separatorVisible = getSkinnable().isSeparatorVisible();
        for (int i = 0; i < semantics.size(); i++) {
            if (i > 0 && separatorVisible) {
                semanticsContainer.getChildren().add(new Separator());
            }
            semanticsContainer.getChildren().add(semantics.get(i));
        }
    }
}