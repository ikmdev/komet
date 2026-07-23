package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.SectionTitledPane;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
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
     * {@link SectionTitledPane#numberColumnsProperty()}.
     */
    private void createContentContainer(SectionTitledPane<T> control) {
        contentContainer = new GridPane();
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
