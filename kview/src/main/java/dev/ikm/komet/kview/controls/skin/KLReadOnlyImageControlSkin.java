package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.controls.KometIcon;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;

public class KLReadOnlyImageControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyImageControl> {
    private static final PseudoClass IMAGE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("image-selected");

    private final VBox mainContainer = new VBox();
    private final VBox textContainer = new VBox();
    private final Label textLabel = new Label();

    private final StackPane imageContainer = new StackPane();
    private final ImageView imageView = new ImageView();

    private final Image promptImage = new Image(KLReadOnlyImageControl.class.getResource("image-96.png").toExternalForm());

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyImageControlSkin(KLReadOnlyImageControl control) {
        super(control);

        imageContainer.getChildren().addAll(imageView, promptTextLabel);
        textContainer.getChildren().add(textLabel);
        mainContainer.getChildren().addAll(titleLabel, imageContainer, textContainer);
        getChildren().add(mainContainer);

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textLabel.setPrefWidth(Double.MAX_VALUE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);

        // Initial texts
        control.setTitle("IMAGE:");
        control.setPromptText("Add image");
        textLabel.setText("No Selection");

        initImage(control);
        setupContextMenu(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
        imageContainer.getStyleClass().add("image-container");
    }

    private void initImage(KLReadOnlyImageControl control) {
        control.imageFileProperty().addListener(observable -> {
            updateImage(control);
        });
        updateImage(control);
    }

    private void updateImage(KLReadOnlyImageControl control) {
        updatePromptTextVisibility(control);
        updateImageToShow(control);
    }

    private void updatePromptTextVisibility(KLReadOnlyImageControl control) {
        NodeUtils.setShowing(promptTextLabel, control.getImageFile() == null);
    }

    private void updateImageToShow(KLReadOnlyImageControl control) {
        if (control.getImageFile() == null) {
            imageView.setImage(promptImage);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, false);
        } else {
            File imageFile = control.getImageFile();
            Image image = new Image(imageFile.toURI().toString());
            String fileName = imageFile.getName();

            imageView.setImage(image);
            textLabel.setText(fileName);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, true);
        }
    }

    private void setupContextMenu(KLReadOnlyImageControl control) {
        contextMenu.getItems().add(
            createMenuItem("Edit Attachment", KometIcon.IconValue.PENCIL, this::fireOnEditAction)
        );

        contextMenu.getItems().addAll(
            new SeparatorMenuItem(),
            createMenuItem("Remove", KometIcon.IconValue.TRASH, this::fireOnRmoveAction)
        );
    }
}