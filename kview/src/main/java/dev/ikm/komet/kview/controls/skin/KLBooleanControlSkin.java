package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLBooleanControl;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Default skin implementation for the {@link KLBooleanControl} control
 *
 * @see KLBooleanControl
 */
public class KLBooleanControlSkin extends SkinBase<KLBooleanControl> {

    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();
    private final ComboBox<Boolean> comboBox = new ComboBox<>();

    /**
     * Creates a new KLBooleanControlSkin instance.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLBooleanControlSkin(KLBooleanControl control) {
        super(control);

        mainContainer.setSpacing(KLComponentListControlSkin.TITLE_SPACE);

        mainContainer.getChildren().addAll(titleLabel, comboBox);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());

        addMenuItemsToComboBox();
        comboBox.valueProperty().bindBidirectional(control.valueProperty());

        mainContainer.setFillWidth(true);
        comboBox.setPrefWidth(Double.MAX_VALUE);
        comboBox.setMaxWidth(Region.USE_PREF_SIZE);

        comboBox.setCellFactory(p -> new ComboBoxCell(control));

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(control.getPromptText());
                } else {
                    String booleanString = item.toString();
                    String capitalized = booleanString.substring(0, 1).toUpperCase() + booleanString.substring(1);
                    setText(capitalized);
                }
            }
        });

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
    }

    private void addMenuItemsToComboBox() {
        comboBox.getItems().addAll(true, false);
    }

    /*******************************************************************************
     *                                                                             *
     * Supporting Classes                                                          *
     *                                                                             *
     ******************************************************************************/

    private static class ComboBoxCell extends ListCell<Boolean> {
        public static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass. getPseudoClass("selected");

        private final HBox mainContainer = new HBox();
        private final Label label = new Label();
        private final StackPane checkMarkGraphic = new StackPane();

        private final KLBooleanControl booleanControl;

        public ComboBoxCell(KLBooleanControl control) {
            this.booleanControl = control;

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            mainContainer.getChildren().addAll(label, checkMarkGraphic);

            setMaxWidth(Double.MAX_VALUE);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);

            // CSS
            mainContainer.getStyleClass().add("main-container");
            checkMarkGraphic.getStyleClass().addAll("icon", "check-mark");
        }

        @Override protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setGraphic(null);
            } else {
                setGraphic(mainContainer);

                pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, booleanControl.isValue() == item);

                String booleanString = item.toString();
                String capitalized = booleanString.substring(0, 1).toUpperCase() + booleanString.substring(1);
                label.setText(capitalized);
            }
        }
    }
}
