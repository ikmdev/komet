package dev.ikm.komet.kview.mvvm.view.genpurpose.control.standard;

import dev.ikm.komet.kview.controls.KLReadOnlyBaseControl;
import javafx.beans.binding.Bindings;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;

public class SemanticStandardControlSkin extends SkinBase<SemanticStandardControl> {

    private final GridPane fieldsContainer = new GridPane();

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public SemanticStandardControlSkin(SemanticStandardControl control) {
        super(control);

        getChildren().add(fieldsContainer);

        fieldsContainer.getStyleClass().add("fields-container");

        Bindings.bindContent(fieldsContainer.getChildren(), control.getFields());

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
        return topInset + fieldsContainer.prefHeight(contentWidth) + bottomInset;
    }
}
