package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentComboBoxControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
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

        comboBox.getStyleClass().add("component-combo-box");

        Bindings.bindContent(comboBox.getItems(), control.getItems());
        comboBox.valueProperty().bindBidirectional(control.valueProperty());

        mainContainer.setFillWidth(true);
        comboBox.setPrefWidth(Double.MAX_VALUE);
        comboBox.setMaxWidth(Region.USE_PREF_SIZE);

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

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
    }
}
