package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLReadOnlyImageControl;
import dev.ikm.komet.kview.controls.KometIcon;
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

        initImage(control);
        setupContextMenu(control);

        // CSS
        imageContainer.getStyleClass().add("image-container");
    }

    private void initImage(KLReadOnlyImageControl control) {
        control.valueProperty().addListener(observable -> {
            updateImage(control);
        });
        updateImage(control);
    }

    private void updateImage(KLReadOnlyImageControl control) {
        updatePromptTextVisibility(control);
        updateImageToShow(control);
    }

    private void updatePromptTextVisibility(KLReadOnlyImageControl control) {
        NodeUtils.setShowing(promptTextLabel, control.getValue() == null);
    }

    private void updateImageToShow(KLReadOnlyImageControl control) {
        if (control.getValue()== null) {
            imageView.setImage(promptImage);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, false);
        } else {
            imageView.setImage(control.getValue());

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