package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ReferenceColors;
import dev.ikm.komet.kview.controls.SectionTitledPane;
import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SectionTitledPaneSkin<T> extends TitledPaneSkin {
    private static final int SPACE_BETWEEN_SEMANTIC_CB_AND_EDIT_BUTTON = 4;
    private static final int SPACE_BETWEEN_TITLE_AND_SEMANTIC_CB = 4;
    private static final int SPACE_BETWEEN_TITLE_AND_REQUIRED_CHIP = 8;
    private static final int SPACE_BETWEEN_CHIP_AND_NOTE = 6;

    /** Active on the required chip once the section's required pattern(s) have a semantic. */
    private static final PseudoClass SATISFIED = PseudoClass.getPseudoClass("satisfied");

    private EditButton editButton;
    private StackPane titleRegion;
    private Text titleRegionText;

    private GridPane contentContainer;
    private HBox referenceStrip;

    private ComboBox<T> referenceComponentSemanticsCB;

    private Label requiredChip;

    private Label unpublishedChip;
    private Label unpublishedNote;

    /**
     * Creates a new TitledPaneSkin instance, installing the necessary child
     * nodes into the Control children list, as
     * well as the necessary input mappings for handling key, mouse, etc events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public SectionTitledPaneSkin(SectionTitledPane<T> control) {
        super(control);

        createContentContainer(control);

        editButton = new EditButton(control);
        editButton.disableProperty().bind(control.editEnabledProperty().not());

        createRequiredChip(control);
        createUnpublishedChip(control);

        titleRegion = (StackPane) control.lookup(".title");
        titleRegionText = (Text) titleRegion.lookup(".text");

        getChildren().addAll(
                editButton,
                referenceComponentSemanticsCB,
                requiredChip,
                unpublishedChip,
                unpublishedNote
        );
    }

    /**
     * Creates the unpublished-changes chip and its note, shown after the section title (and after
     * the required chip when both show) while the control carries an unpublished note (see
     * {@link SectionTitledPane#unpublishedNoteProperty()}): a dashed NOT PUBLISHED chip in the
     * required chip's language, then the note itself (styled by .unpublished-chip and
     * .unpublished-note in kview.css).
     */
    private void createUnpublishedChip(SectionTitledPane<T> control) {
        unpublishedChip = new Label("NOT PUBLISHED");
        unpublishedChip.getStyleClass().add("unpublished-chip");
        unpublishedNote = new Label();
        unpublishedNote.getStyleClass().add("unpublished-note");
        unpublishedNote.textProperty().bind(control.unpublishedNoteProperty());

        ObservableValue<Boolean> shown = control.unpublishedNoteProperty().map(note -> !note.isEmpty()).orElse(false);
        unpublishedChip.visibleProperty().bind(shown);
        unpublishedChip.managedProperty().bind(shown);
        unpublishedNote.visibleProperty().bind(shown);
        unpublishedNote.managedProperty().bind(shown);
    }

    /**
     * Creates the required-pattern chip shown after the section title (see
     * {@link SectionTitledPane#requiredChipVisibleProperty()}): a dashed REQUIRED chip while the
     * section's required pattern still has no semantic, flipping to a green "✓ REQUIREMENT MET"
     * once it does (styled by .required-chip in kview.css).
     */
    private void createRequiredChip(SectionTitledPane<T> control) {
        requiredChip = new Label();
        requiredChip.getStyleClass().add("required-chip");

        requiredChip.textProperty().bind(control.requiredSatisfiedProperty()
                .map(satisfied -> satisfied ? "✓ REQUIREMENT MET" : "REQUIRED"));
        control.requiredSatisfiedProperty().subscribe(satisfied ->
                requiredChip.pseudoClassStateChanged(SATISFIED, satisfied));

        requiredChip.visibleProperty().bind(control.requiredChipVisibleProperty());
        requiredChip.managedProperty().bind(control.requiredChipVisibleProperty());
    }

    /**
     * Builds the GridPane that holds the section's content areas and installs it as the pane's
     * content, under the reference strip (see {@link #createReferenceStrip}). Users of this
     * control add views through {@link SectionTitledPane#getItems()}, and the column layout
     * follows the control's {@link SectionTitledPane#numberColumnsProperty()}. Rows stack at
     * their content height rather than splitting the section's height — see
     * {@link PrefCappedRowsGridPane}.
     */
    private void createContentContainer(SectionTitledPane<T> control) {
        // The strip's color subscription also colors the combobox, so the combobox comes first.
        createReferenceComponentCB(control);
        createReferenceStrip(control);

        contentContainer = new PrefCappedRowsGridPane();
        contentContainer.getStyleClass().add("section-titled-pane-container");

        Bindings.bindContent(contentContainer.getChildren(), control.getItems());

        control.numberColumnsProperty().subscribe(numberColumns -> {
            List<ColumnConstraints> columns = new ArrayList<>();
            for (int i = 0; i < numberColumns.intValue(); ++i) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setHgrow(Priority.ALWAYS);
                columnConstraints.setPercentWidth(100 / ((double) numberColumns.intValue()));
                columns.add(columnConstraints);
            }
            contentContainer.getColumnConstraints().setAll(columns);
        });

        VBox content = new VBox(referenceStrip, contentContainer);
        // Modena pads a VBox placed straight into a titled pane's content; .section-content zeroes it.
        content.getStyleClass().add("section-content");
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        control.setContent(content);
    }

    /**
     * Creates the reference strip shown between the title bar and the content while the control
     * says so (see {@link SectionTitledPane#referenceStripVisibleProperty()}), listing the
     * control's reference summary (see {@link SectionTitledPane#getReferenceSummary()}): a
     * REFERENCE caption, then the summary's fields as "name value" pairs on a single line that is
     * cut off with an ellipsis where the strip runs out of room (styled by .reference-strip in
     * kview.css).
     */
    private void createReferenceStrip(SectionTitledPane<T> control) {
        Label caption = new Label("REFERENCE:");
        caption.getStyleClass().add("reference-strip-caption");
        caption.setMinWidth(Region.USE_PREF_SIZE);

        ElidedTextLine fields = new ElidedTextLine();
        fields.getStyleClass().add("reference-strip-fields");
        HBox.setHgrow(fields, Priority.ALWAYS);
        control.getReferenceSummary().subscribe(() -> fillReferenceFields(fields, control.getReferenceSummary()));
        fillReferenceFields(fields, control.getReferenceSummary());

        referenceStrip = new HBox(caption, fields);
        referenceStrip.setAlignment(Pos.CENTER_LEFT);
        referenceStrip.getStyleClass().add("reference-strip");

        // The section's color, on the strip's left edge as on the combobox's.
        control.referenceColorIndexProperty().subscribe(colorIndex -> {
            ReferenceColors.apply(referenceStrip, colorIndex.intValue());
            ReferenceColors.apply(referenceComponentSemanticsCB, colorIndex.intValue());
        });

        referenceStrip.visibleProperty().bind(control.referenceStripVisibleProperty());
        referenceStrip.managedProperty().bind(control.referenceStripVisibleProperty());
    }

    private static void fillReferenceFields(ElidedTextLine line, List<SectionTitledPane.ReferenceField> summary) {
        List<Text> texts = new ArrayList<>();
        for (SectionTitledPane.ReferenceField field : summary) {
            if (!texts.isEmpty()) {
                Text separator = new Text(" · ");
                separator.getStyleClass().add("reference-strip-field-separator");
                texts.add(separator);
            }
            // Upper-cased with a colon, the way the fields in the section's content title themselves.
            Text name = new Text(field.name().toUpperCase() + ": ");
            name.getStyleClass().add("reference-strip-field-name");
            Text value = new Text(field.value());
            value.getStyleClass().add("reference-strip-field-value");
            texts.add(name);
            texts.add(value);
        }
        line.setTexts(texts);
    }

    private void createReferenceComponentCB(SectionTitledPane<T> control) {
        referenceComponentSemanticsCB = new ComboBox<>();

        referenceComponentSemanticsCB.getStyleClass().add("section-combo-box");

        referenceComponentSemanticsCB.setItems(control.getReferenceComponents());

        referenceComponentSemanticsCB.cellFactoryProperty().bind(control.referenceComponentCellFactoryProperty());
        referenceComponentSemanticsCB.buttonCellProperty().bind(control.referenceComponentButtonCellFactoryProperty());
        referenceComponentSemanticsCB.valueProperty().bindBidirectional(control.selectedReferenceComponentProperty());

        ObservableList<T> refs = control.getReferenceComponents();
        referenceComponentSemanticsCB.visibleProperty().bind(Bindings.isNotEmpty(refs));
        referenceComponentSemanticsCB.managedProperty().bind(Bindings.isNotEmpty(refs));
    }

    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);

        final double titleRegionX = titleRegion.getLayoutX();
        final double titleRegionWidth = titleRegion.getWidth();
        final double titleRegionRightInset = titleRegion.snappedRightInset();
        final double titleRegionHeight = titleRegion.getHeight();

        final double titleRegionTextX = titleRegionText.getLayoutX();
        final double titleRegionTextWidth = titleRegionText.getLayoutBounds().getWidth();

        // Edit Button
        final double editButtonWidth = editButton.prefWidth(titleRegionHeight);
        final double editButtonX = titleRegion.getLayoutX() + titleRegion.getWidth() - titleRegion.snappedRightInset() - editButtonWidth;
        editButton.resize(editButtonWidth, titleRegionHeight);
        editButton.setLayoutX(editButtonX);
        editButton.setLayoutY(titleRegion.getLayoutY());

        // Required chip, right after the section title text
        double titleRightEdge = titleRegionTextX + titleRegionTextWidth;
        if (requiredChip.isVisible()) {
            final double chipWidth = requiredChip.prefWidth(-1);
            final double chipHeight = requiredChip.prefHeight(chipWidth);
            final double chipX = titleRightEdge + SPACE_BETWEEN_TITLE_AND_REQUIRED_CHIP;
            requiredChip.resize(chipWidth, chipHeight);
            requiredChip.setLayoutX(chipX);
            requiredChip.setLayoutY(titleRegion.getLayoutY() + (titleRegionHeight - chipHeight) / 2d);
            titleRightEdge = chipX + chipWidth;
        }

        // Unpublished chip and its note, after whatever precedes them on the title line
        if (unpublishedChip.isVisible()) {
            final double chipWidth = unpublishedChip.prefWidth(-1);
            final double chipHeight = unpublishedChip.prefHeight(chipWidth);
            final double chipX = titleRightEdge + SPACE_BETWEEN_TITLE_AND_REQUIRED_CHIP;
            unpublishedChip.resize(chipWidth, chipHeight);
            unpublishedChip.setLayoutX(chipX);
            unpublishedChip.setLayoutY(titleRegion.getLayoutY() + (titleRegionHeight - chipHeight) / 2d);

            final double noteWidth = unpublishedNote.prefWidth(-1);
            final double noteHeight = unpublishedNote.prefHeight(noteWidth);
            final double noteX = chipX + chipWidth + SPACE_BETWEEN_CHIP_AND_NOTE;
            unpublishedNote.resize(noteWidth, noteHeight);
            unpublishedNote.setLayoutX(noteX);
            unpublishedNote.setLayoutY(titleRegion.getLayoutY() + (titleRegionHeight - noteHeight) / 2d);
            titleRightEdge = noteX + noteWidth;
        }

        // Reference Component Semantics Combobox: its content width, capped by its CSS max width
        // (resize does not apply the max itself) and by the room the title line leaves.
        double cbPrefWidth = Math.min(referenceComponentSemanticsCB.prefWidth(-1), referenceComponentSemanticsCB.maxWidth(-1));
        double cbMaxWidth =  editButtonX - SPACE_BETWEEN_SEMANTIC_CB_AND_EDIT_BUTTON
                - (titleRightEdge + SPACE_BETWEEN_TITLE_AND_SEMANTIC_CB);
        double cbWidth = Math.min(cbPrefWidth, cbMaxWidth);

        double cbX = titleRegionX + titleRegionWidth - titleRegionRightInset
                - editButtonWidth - cbWidth - SPACE_BETWEEN_SEMANTIC_CB_AND_EDIT_BUTTON;

        double cbHeight = referenceComponentSemanticsCB.prefHeight(cbWidth);
        double cbY = titleRegionHeight / 2d - cbHeight / 2d;
        referenceComponentSemanticsCB.resize(cbWidth, cbHeight);
        referenceComponentSemanticsCB.relocate(cbX, cbY);
    }

    /*******************************************************************************
     *                                                                             *
     * Supporting Classes                                                          *
     *                                                                             *
     ******************************************************************************/

    /**
     * A GridPane whose rows take their preferred (content) height, stacked from the top, instead
     * of stretching to split the pane's height between them. When the rows' combined preferred
     * height exceeds the available height, each row is capped at its fair share of that height:
     * rows shorter than the fair share keep their full preferred height, and the space they don't
     * use raises the share of the taller rows, which then scroll internally (the content areas
     * host their fields in a ScrollPane — see PatternSemanticsStandardControlSkin).
     */
    private static class PrefCappedRowsGridPane extends GridPane {

        private double[] appliedRowHeights = new double[0];

        @Override
        protected void layoutChildren() {
            applyRowConstraints();
            super.layoutChildren();
        }

        /**
         * The height the content wants, which is every row's preferred height stacked up. Measured
         * from the children rather than from the row constraints: those hold the heights the grid's
         * current height allowed (see {@link #applyRowConstraints()}), so a grid that was once
         * squeezed would go on asking for the squeezed height and never get its room back.
         */
        @Override
        protected double computePrefHeight(double width) {
            double contentWidth = width < 0 ? -1 : width - snappedLeftInset() - snappedRightInset();
            double[] rowPrefHeights = rowPrefHeights(contentWidth);
            double prefHeight = Math.max(0, rowPrefHeights.length - 1) * getVgap();
            for (double rowPrefHeight : rowPrefHeights) {
                prefHeight += rowPrefHeight;
            }
            return snappedTopInset() + prefHeight + snappedBottomInset();
        }

        /**
         * Recomputes the fixed per-row heights for the current size and content and installs them
         * as this grid's row constraints. Guarded so constraints are only touched when the heights
         * actually change — installing them re-requests layout, and the guard is what lets that
         * follow-up pass settle.
         */
        private void applyRowConstraints() {
            double[] rowPrefHeights = rowPrefHeights(getWidth() - snappedLeftInset() - snappedRightInset());
            int rowCount = rowPrefHeights.length;
            if (rowCount == 0) {
                if (appliedRowHeights.length > 0) {
                    appliedRowHeights = new double[0];
                    getRowConstraints().clear();
                }
                return;
            }

            double availableHeight = getHeight() - snappedTopInset() - snappedBottomInset()
                    - getVgap() * (rowCount - 1);
            double[] rowHeights = FXUtils.capAtFairShare(rowPrefHeights, availableHeight);
            for (int i = 0; i < rowHeights.length; i++) {
                rowHeights[i] = snapSizeY(rowHeights[i]);
            }

            if (!heightsChanged(rowHeights)) {
                return;
            }
            appliedRowHeights = rowHeights;
            List<RowConstraints> constraints = new ArrayList<>(rowCount);
            for (double rowHeight : rowHeights) {
                // Pin only pref and max: pinning min too would raise the grid's computed min
                // height to the full content height, freezing the section dividers and the
                // window edges. The min stays at the children's own (small, ScrollPane-backed)
                // computed min so the section remains freely resizable.
                RowConstraints constraint = new RowConstraints();
                constraint.setPrefHeight(rowHeight);
                constraint.setMaxHeight(rowHeight);
                constraints.add(constraint);
            }
            getRowConstraints().setAll(constraints);
        }

        /**
         * The preferred height of each row — the tallest preferred height among the children in it
         * — measured at the width the passed in content width leaves each cell. A negative content
         * width (the grid is being measured without one) leaves the children to their own
         * unconstrained preferred height.
         */
        private double[] rowPrefHeights(double contentWidth) {
            List<Node> children = getManagedChildren();
            int rowCount = 0;
            for (Node child : children) {
                rowCount = Math.max(rowCount, rowIndexOf(child) + 1);
            }
            if (rowCount == 0) {
                return new double[0];
            }

            // Column widths are uniform (see numberColumnsProperty subscription: every column
            // gets the same percentWidth), so each child's width — which its preferred height
            // may depend on — follows from its column span alone.
            int columnCount = Math.max(1, getColumnConstraints().size());
            double columnWidth = contentWidth < 0
                    ? -1
                    : (contentWidth - getHgap() * (columnCount - 1)) / columnCount;

            double[] rowPrefHeights = new double[rowCount];
            for (Node child : children) {
                Integer span = GridPane.getColumnSpan(child);
                int columnSpan = span == null ? 1 : span;
                double cellWidth = columnWidth < 0
                        ? -1
                        : columnWidth * columnSpan + getHgap() * (columnSpan - 1);
                int row = rowIndexOf(child);
                rowPrefHeights[row] = Math.max(rowPrefHeights[row], child.prefHeight(cellWidth));
            }
            return rowPrefHeights;
        }

        private static int rowIndexOf(Node child) {
            Integer rowIndex = GridPane.getRowIndex(child);
            return rowIndex == null ? 0 : rowIndex;
        }

        private boolean heightsChanged(double[] rowHeights) {
            if (rowHeights.length != appliedRowHeights.length) {
                return true;
            }
            for (int i = 0; i < rowHeights.length; i++) {
                if (Math.abs(rowHeights[i] - appliedRowHeights[i]) > 0.5) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A row of Text nodes on a single line, each keeping its own style: laid out left to right and,
     * where the row runs out of room, cut off with an ellipsis — it never wraps, and it never asks
     * for more than zero width, so the row beside it keeps its own items whole. The texts are
     * measured through a scratch Text in their font rather than by changing them in place, so a
     * layout pass that changes nothing leaves them untouched and does not request another.
     */
    private static class ElidedTextLine extends Region {
        private static final String ELLIPSIS = "…";

        private final List<Text> texts = new ArrayList<>();
        private final List<String> fullTexts = new ArrayList<>();
        private final Text measurer = new Text();

        void setTexts(List<Text> newTexts) {
            texts.clear();
            fullTexts.clear();
            for (Text text : newTexts) {
                text.setTextOrigin(VPos.TOP);
                texts.add(text);
                fullTexts.add(text.getText());
            }
            getChildren().setAll(newTexts);
            requestLayout();
        }

        @Override
        protected double computeMinWidth(double height) {
            return snappedLeftInset() + snappedRightInset();
        }

        @Override
        protected double computePrefWidth(double height) {
            double width = 0;
            for (int i = 0; i < texts.size(); i++) {
                width += widthOf(texts.get(i), fullTexts.get(i));
            }
            return snappedLeftInset() + width + snappedRightInset();
        }

        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            double height = 0;
            for (int i = 0; i < texts.size(); i++) {
                height = Math.max(height, heightOf(texts.get(i), fullTexts.get(i)));
            }
            return snappedTopInset() + height + snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            final double left = snappedLeftInset();
            final double right = getWidth() - snappedRightInset();
            final double top = snappedTopInset();
            final double contentHeight = getHeight() - top - snappedBottomInset();

            double x = left;
            boolean roomLeft = true;
            for (int i = 0; i < texts.size(); i++) {
                Text text = texts.get(i);
                String full = fullTexts.get(i);
                String shown = roomLeft ? fitting(text, full, right - x) : "";
                if (shown.length() < full.length()) {
                    roomLeft = false;
                }
                text.setText(shown);
                text.setVisible(!shown.isEmpty());
                if (shown.isEmpty()) {
                    continue;
                }
                double height = heightOf(text, shown);
                text.relocate(x, top + (contentHeight - height) / 2d);
                x += widthOf(text, shown);
            }
        }

        /**
         * The text itself when it fits in the room left, else its longest prefix that, with an
         * ellipsis on the end, does; empty when not even the ellipsis fits.
         */
        private String fitting(Text text, String full, double room) {
            if (widthOf(text, full) <= room) {
                return full;
            }
            int low = 0;
            int high = full.length();
            while (low < high) {
                int middle = (low + high + 1) / 2;
                if (widthOf(text, full.substring(0, middle) + ELLIPSIS) <= room) {
                    low = middle;
                } else {
                    high = middle - 1;
                }
            }
            if (low == 0 && widthOf(text, ELLIPSIS) > room) {
                return "";
            }
            return full.substring(0, low).stripTrailing() + ELLIPSIS;
        }

        private double widthOf(Text text, String string) {
            measurer.setFont(text.getFont());
            measurer.setText(string);
            return measurer.getLayoutBounds().getWidth();
        }

        private double heightOf(Text text, String string) {
            measurer.setFont(text.getFont());
            measurer.setText(string);
            return measurer.getLayoutBounds().getHeight();
        }
    }

    private static class EditButton extends Pane {
        private final HBox mainContainer = new HBox();

        private final Separator separator = new Separator();
        private final Button button = new Button();
        private final Tooltip tooltip = new Tooltip();

        public EditButton(SectionTitledPane titledPane) {
            separator.setOrientation(Orientation.VERTICAL);

            button.getStyleClass().add("add-pencil-button");
            Region graphic = new Region();
            graphic.setPrefHeight(32);
            graphic.setPrefWidth(32);
            graphic.getStyleClass().add("add-pencil");
            button.setGraphic(graphic);
            button.onActionProperty().bind(titledPane.onEditActionProperty());

            tooltip.setText("Edit Fields");
            button.setTooltip(tooltip);

            mainContainer.getStyleClass().add("section-edit-area");
            // Fill this pane's height (the full title-bar height set by layoutChildren), so the
            // separator spans top to bottom and the button centers within the title bar.
            mainContainer.prefHeightProperty().bind(heightProperty());

            mainContainer.getChildren().addAll(separator, button);
            getChildren().add(mainContainer);
        }
    }
}
