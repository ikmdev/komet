package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Default skin implementation for the {@link KLComponentComboBoxControl} control
 *
 * @see KLComponentComboBoxControl
 */
public class KLComponentComboBoxControlSkin extends SkinBase<KLComponentComboBoxControl> {

    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();
    private final ComboBox<EntityProxy> comboBox = new ComboBox<>();
    private final Button clearButton = new Button();

    /**
     * Space, in pixels, between the clear button and the combo box's arrow button. When changing it,
     * grow or shrink the right padding of the button cell in the {@code :clearable} rules of
     * {@code component-combo-box-control.css} (and of stylesheets restyling the control) by the same
     * amount, so the text keeps clear of the button.
     */
    private static final double CLEAR_BUTTON_GAP = 8;

    /**
     * Creates a new KLComponentComboBoxControlSkin instance.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLComponentComboBoxControlSkin(KLComponentComboBoxControl control) {
        super(control);
        control.setFocusTraversable(false);
        mainContainer.getChildren().addAll(titleLabel, comboBox);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());
        titleLabel.getStyleClass().add("editable-title-label");
        // No title, no title row — a host that labels the control itself leaves the title unset
        titleLabel.managedProperty().bind(titleLabel.visibleProperty());
        titleLabel.visibleProperty().bind(titleLabel.textProperty().isNotEmpty());

        comboBox.getStyleClass().add("component-combo-box");

        Bindings.bindContent(comboBox.getItems(), control.getItems());
        comboBox.valueProperty().bindBidirectional(control.valueProperty());

        // Let the combo box stretch to whatever width the control is given (the VBox fills its
        // width by default). Its preferred width stays its natural one: a MAX_VALUE preferred width
        // would make an HBox host treat the control as oversized and shrink it to its minimum.
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // Renders the popup list cells, and — when the value is not in the items list (e.g. the
        // blank concept a new semantic's fields are seeded with) — the button area too, since the
        // ComboBox skin then bypasses the button cell (RT-21336): without this converter the raw
        // EntityProxy toString would show.
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(EntityProxy entityProxy) {
                if (KLComponentControl.isEmpty(entityProxy)) {
                    return control.getPromptText();
                }
                return control.getComponentNameRenderer().apply(entityProxy);
            }

            @Override
            public EntityProxy fromString(String string) {
                return null;
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EntityProxy item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || KLComponentControl.isEmpty(item)) {
                    setText(control.getPromptText());
                } else {
                    setText(control.getComponentNameRenderer().apply(item));
                }
            }
        });

        // Clear button: overlaid on the combo box (a sibling, not a child of it, so its clicks never
        // reach the combo box and toggle the popup), positioned by layoutChildren just before the
        // arrow button. Shown only while clearable and a value is chosen.
        Region clearIcon = new Region();
        clearIcon.getStyleClass().add("clear-icon");
        clearButton.setGraphic(clearIcon);
        clearButton.getStyleClass().add("clear-button");
        clearButton.setFocusTraversable(false);
        clearButton.setManaged(false);
        clearButton.visibleProperty().bind(control.clearableProperty().and(control.valueProperty().isNotNull()));
        // Unmanaged, so its appearing does not trigger a layout pass by itself
        clearButton.visibleProperty().subscribe(control::requestLayout);
        clearButton.setOnAction(_ -> control.setValue(null));
        getChildren().add(clearButton);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        if (!clearButton.isVisible()) {
            return;
        }
        Node arrowButton = comboBox.lookup(".arrow-button");
        double arrowButtonWidth = arrowButton == null ? 0 : arrowButton.getLayoutBounds().getWidth();
        double buttonWidth = clearButton.prefWidth(-1);
        double buttonHeight = clearButton.prefHeight(buttonWidth);
        Bounds comboBoxBounds = comboBox.getBoundsInParent();
        double x = contentX + mainContainer.getLayoutX() + comboBoxBounds.getMaxX()
                - arrowButtonWidth - CLEAR_BUTTON_GAP - buttonWidth;
        double y = contentY + mainContainer.getLayoutY() + comboBoxBounds.getMinY()
                + (comboBoxBounds.getHeight() - buttonHeight) / 2;
        clearButton.resizeRelocate(snapPositionX(x), snapPositionY(y), buttonWidth, buttonHeight);
    }
}
