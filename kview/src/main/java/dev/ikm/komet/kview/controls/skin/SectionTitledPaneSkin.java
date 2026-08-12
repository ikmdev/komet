package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.SectionTitledPane;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
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
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SectionTitledPaneSkin<T> extends TitledPaneSkin {
    private static final int SPACE_BETWEEN_SEMANTIC_CB_AND_EDIT_BUTTON = 4;
    private static final int SPACE_BETWEEN_TITLE_AND_SEMANTIC_CB = 4;
    private static final int SPACE_BETWEEN_TITLE_AND_REQUIRED_CHIP = 8;

    /** Active on the required chip once the section's required pattern(s) have a semantic. */
    private static final PseudoClass SATISFIED = PseudoClass.getPseudoClass("satisfied");

    private EditButton editButton;
    private StackPane titleRegion;
    private Text titleRegionText;

    private GridPane contentContainer;

    private ComboBox<T> referenceComponentSemanticsCB;

    private Label requiredChip;

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

        createReferenceComponentCB(control);
        createRequiredChip(control);

        titleRegion = (StackPane) control.lookup(".title");
        titleRegionText = (Text) titleRegion.lookup(".text");

        getChildren().addAll(
                editButton,
                referenceComponentSemanticsCB,
                requiredChip
        );
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
     * content. Users of this control add views through
     * {@link SectionTitledPane#getItems()}, and the column layout follows the control's
     * {@link SectionTitledPane#numberColumnsProperty()}. Rows stack at their content height
     * rather than splitting the section's height — see {@link PrefCappedRowsGridPane}.
     */
    private void createContentContainer(SectionTitledPane<T> control) {
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

        control.setContent(contentContainer);
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

        // Reference Component Semantics Combobox
        double cbPrefWidth = referenceComponentSemanticsCB.prefWidth(-1);
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
         * Recomputes the fixed per-row heights for the current size and content and installs them
         * as this grid's row constraints. Guarded so constraints are only touched when the heights
         * actually change — installing them re-requests layout, and the guard is what lets that
         * follow-up pass settle.
         */
        private void applyRowConstraints() {
            List<Node> children = getManagedChildren();
            int rowCount = 0;
            for (Node child : children) {
                rowCount = Math.max(rowCount, rowIndexOf(child) + 1);
            }
            if (rowCount == 0) {
                if (appliedRowHeights.length > 0) {
                    appliedRowHeights = new double[0];
                    getRowConstraints().clear();
                }
                return;
            }

            // Column widths are uniform (see numberColumnsProperty subscription: every column
            // gets the same percentWidth), so each child's width — which its preferred height
            // may depend on — follows from its column span alone.
            int columnCount = Math.max(1, getColumnConstraints().size());
            double contentWidth = getWidth() - snappedLeftInset() - snappedRightInset();
            double columnWidth = (contentWidth - getHgap() * (columnCount - 1)) / columnCount;

            double[] rowPrefHeights = new double[rowCount];
            for (Node child : children) {
                Integer span = GridPane.getColumnSpan(child);
                int columnSpan = span == null ? 1 : span;
                double cellWidth = columnWidth * columnSpan + getHgap() * (columnSpan - 1);
                int row = rowIndexOf(child);
                rowPrefHeights[row] = Math.max(rowPrefHeights[row], child.prefHeight(cellWidth));
            }

            double availableHeight = getHeight() - snappedTopInset() - snappedBottomInset()
                    - getVgap() * (rowCount - 1);
            double[] rowHeights = capAtFairShare(rowPrefHeights, availableHeight);
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

        private static int rowIndexOf(Node child) {
            Integer rowIndex = GridPane.getRowIndex(child);
            return rowIndex == null ? 0 : rowIndex;
        }

        /**
         * Allocates the available height among the rows: every row gets at most its preferred
         * height, and a row only gets less when the rows that need more than an equal split
         * cannot be satisfied — those tall rows then share what the short rows left over.
         */
        private static double[] capAtFairShare(double[] prefHeights, double availableHeight) {
            int rowCount = prefHeights.length;
            double[] heights = new double[rowCount];
            boolean[] keepsPref = new boolean[rowCount];
            double remaining = Math.max(0, availableHeight);
            int uncapped = rowCount;

            // Settle the rows that fit within the current fair share; every row settled frees
            // up share for the rest, so iterate until a full pass settles nothing.
            boolean settledAny = true;
            while (settledAny && uncapped > 0) {
                settledAny = false;
                double fairShare = remaining / uncapped;
                for (int i = 0; i < rowCount; i++) {
                    if (!keepsPref[i] && prefHeights[i] <= fairShare) {
                        keepsPref[i] = true;
                        heights[i] = prefHeights[i];
                        remaining -= prefHeights[i];
                        uncapped--;
                        settledAny = true;
                    }
                }
            }
            if (uncapped > 0) {
                double fairShare = remaining / uncapped;
                for (int i = 0; i < rowCount; i++) {
                    if (!keepsPref[i]) {
                        heights[i] = fairShare;
                    }
                }
            }
            return heights;
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
