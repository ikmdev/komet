package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import dev.ikm.komet.kview.controls.ReferenceColors;
import dev.ikm.komet.layout.PatternSemanticsPresenter.ReferencingSection;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

public class SemanticStandardControlSkin extends SkinBase<SemanticStandardControl> {

    /** The reference mark's width. */
    private static final double MARK_WIDTH = 3;
    /** How far the mark stays left of the fields: inside the semantics container's 14px left padding. */
    private static final double MARK_GAP = 5;

    private final GridPane fieldsContainer = new GridPane();

    /**
     * The reference mark: a bar in the color of the sections anchored on this semantic, down the
     * left edge in the gutter the semantics container's padding leaves, so the fields do not move.
     * A child only while the control is referenced (see {@link SemanticStandardControl#getReferencedBy()});
     * laid out by this skin, outside the content area the managed children fill.
     */
    private final Region referenceMark = new Region();
    private final Tooltip referenceMarkTooltip = new Tooltip();

    /**
     * Shown instead of the fields grid while the control carries placeholder text — a semantic with
     * no version for the current view has no field values to render.
     */
    private final Label placeholderLabel = new Label();

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public SemanticStandardControlSkin(SemanticStandardControl control) {
        super(control);

        fieldsContainer.getStyleClass().add("fields-container");
        placeholderLabel.getStyleClass().add("no-version-label");
        Bindings.bindContent(fieldsContainer.getChildren(), control.getFields());

        referenceMark.getStyleClass().add("reference-mark");
        referenceMark.setManaged(false);
        Tooltip.install(referenceMark, referenceMarkTooltip);

        control.placeholderTextProperty().subscribe(placeholderText -> {
            boolean showPlaceholder = placeholderText != null && !placeholderText.isEmpty();
            placeholderLabel.setText(placeholderText);
            getChildren().setAll(showPlaceholder ? placeholderLabel : fieldsContainer);
            if (control.getReferencedBy() != null) {
                getChildren().add(referenceMark);
            }
        });

        control.referencedByProperty().subscribe(this::updateReferenceMark);

        control.numberColumnsProperty().subscribe(numberColumns -> {
            List<ColumnConstraints> columns = new ArrayList<>();
            for (int i = 0; i < numberColumns.intValue(); ++i) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setHgrow(Priority.ALWAYS);
                columnConstraints.setPercentWidth(100 / ((double) numberColumns.intValue()));
                columns.add(columnConstraints);
            }
            fieldsContainer.getColumnConstraints().setAll(columns);
        });

        control.editMode.subscribe(isEditMode -> {
            for (KLReadOnlyBaseControl field : control.getFields()) {
                field.setEditMode(isEditMode);
            }
        });

        control.previewMode.subscribe(isPreviewMode -> {
            for (KLReadOnlyBaseControl field : control.getFields()) {
                field.setPreviewMode(isPreviewMode);
            }
        });
    }

    /** Shows the mark in the referencing sections' color, naming them in its tooltip (styled by .reference-mark in kview.css), or hides it for null. */
    private void updateReferenceMark(ReferencingSection referencedBy) {
        if (referencedBy == null) {
            getChildren().remove(referenceMark);
            return;
        }
        ReferenceColors.apply(referenceMark, referencedBy.colorIndex());
        referenceMarkTooltip.setText("Referenced by " + referencedBy.sectionName());
        if (!getChildren().contains(referenceMark)) {
            getChildren().add(referenceMark);
        }
    }

    /**
     * The content (fields grid or placeholder) fills the area; the mark stands left of it,
     * {@value #MARK_GAP}px clear of the fields.
     */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        for (Node child : getChildren()) {
            if (child.isManaged()) {
                layoutInArea(child, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
            }
        }
        if (getChildren().contains(referenceMark)) {
            double rightEdge = contentX - MARK_GAP;
            referenceMark.resizeRelocate(snapPositionX(rightEdge - MARK_WIDTH), contentY, snapSizeX(MARK_WIDTH), contentHeight);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@code SkinBase} measures children with {@code prefHeight(-1)}, which for a field whose value
     * wraps reports a single line. The fields grid is measured at the width actually available
     * instead, so a semantic containing a long value is tall enough for all of its fields.
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset,
                                       double bottomInset, double leftInset) {
        double contentWidth = width < 0 ? -1 : width - leftInset - rightInset;
        Node content = getChildren().isEmpty() ? fieldsContainer : getChildren().getFirst();
        return topInset + content.prefHeight(contentWidth) + bottomInset;
    }
}
