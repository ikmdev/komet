package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.controls.KometIcon;
import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.css.PseudoClass;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class KLReadOnlyImageControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyImageControl> {
    private static final PseudoClass IMAGE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("image-selected");

    private final StackPane imageContainer = new StackPane();
    private final ImageView imageView = new ImageView();

    private final Image promptImage = new Image(KLReadOnlyImageControl.class.getResource("image-96.png").toExternalForm());

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyImageControlSkin(KLReadOnlyImageControl control) {
        super(control);

        mainContainer.getChildren().add(imageContainer);

        imageContainer.getChildren().addAll(imageView, promptTextLabel);

        control.setPromptText("Add image");

        // Image
        imageView.setPreserveRatio(true);
        control.valueProperty().subscribe(this::updateImage);

        setupContextMenu(control);

        // CSS
        imageContainer.getStyleClass().add("image-container");
    }

    private void updateImage(Image newImage) {
        updatePromptTextVisibility(newImage);
        updateImageToShow(newImage);
    }

    private void updatePromptTextVisibility(Image newImage) {
        NodeUtils.setShowing(promptTextLabel, newImage == null);
    }

    private void updateImageToShow(Image newImage) {
        if (newImage == null) {
            imageView.setImage(promptImage);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, false);
        } else {
            imageView.setImage(newImage);
            FXUtils.fitImageToBounds(imageView, KLImageControlSkin.MAX_IMAGE_WIDTH, KLImageControlSkin.MAX_IMAGE_HEIGHT);

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