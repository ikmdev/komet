package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.*;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLReadOnlyComponentControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyComponentControl> {
    private final VBox mainContainer = new VBox();

    private final HBox textContainer = new HBox();
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentControlSkin(KLReadOnlyComponentControl control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textContainer);
        textContainer.getChildren().addAll(iconImageView, promptTextLabel, textLabel);
        getChildren().add(mainContainer);

        textLabel.textProperty().bind(control.textProperty());
        iconImageView.imageProperty().bind(control.iconProperty());

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        iconImageView.setFitWidth(20);
        iconImageView.setFitHeight(20);

        setupContextMenu(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void setupContextMenu(KLReadOnlyComponentControl control) {
        contextMenu.getItems().add(
                createMenuItem("Edit Component", KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );

        contextMenu.getItems().addAll(
                new SeparatorMenuItem(),
                createMenuItem("Remove", KometIcon.IconValue.TRASH, this::fireOnRmoveAction)
        );
    }
}