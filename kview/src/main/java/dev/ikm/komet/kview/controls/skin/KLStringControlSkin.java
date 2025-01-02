package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLStringControl;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLStringControlSkin<T extends KLStringControl> extends SkinBase<T> {
    private final VBox mainContainer = new VBox();
    protected final TextField textField = new TextField();
    protected final Label titleLabel = new Label();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLStringControlSkin(T control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textField);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());
        textField.textProperty().bindBidirectional(control.textProperty());

        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);

        mainContainer.setFillWidth(false);

        textField.setPromptText("Enter text here");

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
        textField.getStyleClass().add("text");
    }
}