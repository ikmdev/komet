package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLBooleanControl;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

        mainContainer.getChildren().addAll(titleLabel, comboBox);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());

        addMenuItemsToComboBox();
        comboBox.valueProperty().bindBidirectional(control.valueProperty());

        mainContainer.setFillWidth(true);
        comboBox.setPrefWidth(Double.MAX_VALUE);
        comboBox.setMaxWidth(Region.USE_PREF_SIZE);

        comboBox.setCellFactory(p -> new ListCell<>() {
            {
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText("");
                } else {
                    String booleanString = item.toString();
                    String capitalized = booleanString.substring(0, 1).toUpperCase() + booleanString.substring(1);
                    setText(capitalized);
                }
            }
        });

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
}
