package dev.ikm.komet.layout_engine.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * An editable control for Boolean data types.
 * <p>
 * Displays a title label and a ComboBox with True/False options.
 */
public class KlBooleanControl extends Control {

    public KlBooleanControl() {
        getStyleClass().add("kl-boolean-control");
    }

    // -- title
    private final StringProperty title = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() { return title; }
    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }

    // -- value
    private final ObjectProperty<Boolean> value = new SimpleObjectProperty<>(this, "value", Boolean.FALSE);
    public final ObjectProperty<Boolean> valueProperty() { return value; }
    public final Boolean getValue() { return value.get(); }
    public final void setValue(Boolean value) { this.value.set(value); }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "Choose Selection");
    public final StringProperty promptTextProperty() { return promptText; }
    public final String getPromptText() { return promptText.get(); }
    public final void setPromptText(String value) { this.promptText.set(value); }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KlBooleanControlSkin(this);
    }

    /**
     * Default skin implementation for KlBooleanControl.
     */
    public static class KlBooleanControlSkin extends SkinBase<KlBooleanControl> {

        private final VBox mainContainer = new VBox();
        private final Label titleLabel = new Label();
        private final ComboBox<Boolean> comboBox = new ComboBox<>();

        public KlBooleanControlSkin(KlBooleanControl control) {
            super(control);
            control.setFocusTraversable(false);

            mainContainer.getChildren().addAll(titleLabel, comboBox);
            getChildren().add(mainContainer);

            // Title binding
            titleLabel.textProperty().bind(control.titleProperty());
            titleLabel.getStyleClass().add("kl-editable-title-label");

            // ComboBox setup
            comboBox.getItems().addAll(Boolean.TRUE, Boolean.FALSE);
            comboBox.valueProperty().bindBidirectional(control.valueProperty());

            mainContainer.setFillWidth(true);
            comboBox.setPrefWidth(Double.MAX_VALUE);
            comboBox.setMaxWidth(Region.USE_PREF_SIZE);

            // Custom cell factory for display
            comboBox.setCellFactory(p -> new BooleanComboBoxCell(control));
            comboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(control.getPromptText());
                    } else {
                        setText(capitalize(item.toString()));
                    }
                }
            });

            // CSS classes
            mainContainer.getStyleClass().add("kl-main-container");
            titleLabel.getStyleClass().add("kl-title");
        }

        private static String capitalize(String s) {
            return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        /**
         * Custom cell for the ComboBox dropdown.
         */
        private static class BooleanComboBoxCell extends ListCell<Boolean> {
            private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

            private final HBox mainContainer = new HBox();
            private final Label label = new Label();
            private final StackPane checkMarkGraphic = new StackPane();
            private final KlBooleanControl booleanControl;

            public BooleanComboBoxCell(KlBooleanControl control) {
                this.booleanControl = control;
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                mainContainer.getChildren().addAll(label, checkMarkGraphic);
                setMaxWidth(Double.MAX_VALUE);
                label.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(label, Priority.ALWAYS);

                // CSS classes
                mainContainer.getStyleClass().add("kl-cell-container");
                checkMarkGraphic.getStyleClass().addAll("kl-icon", "kl-check-mark");
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                } else {
                    setGraphic(mainContainer);
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS,
                            booleanControl.getValue() != null && booleanControl.getValue().equals(item));
                    label.setText(capitalize(item.toString()));
                }
            }

            private static String capitalize(String s) {
                return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
            }
        }
    }
}