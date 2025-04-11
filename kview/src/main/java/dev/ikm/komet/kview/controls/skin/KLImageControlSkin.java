package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLImageControl;
import dev.ikm.komet.kview.fxutils.FXUtils;
import javafx.beans.binding.StringBinding;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

public class KLImageControlSkin extends SkinBase<KLImageControl> {
    private static final PseudoClass IMAGE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("image-selected");

    private static final String ATTACH_BUTTON_TEXT = "ATTACH IMAGE";

    public static final int MAX_IMAGE_WIDTH = 640;
    public static final int MAX_IMAGE_HEIGHT = 262;

    private final VBox mainContainer = new VBox();

    protected final Label titleLabel = new Label();

    private final StackPane attachImageButtonContainer = new StackPane();
    private final Button attachImageButton = new Button();

    private final StackPane imageContainer = new StackPane();
    private final ImageView imageView = new ImageView();

    private final Image promptImage = new Image(KLImageControl.class.getResource("image-96.png").toExternalForm());
    private final Label promptTextLabel = new Label();

    private final StackPane clearContainer = new StackPane();
    private final StackPane clearGraphic = new StackPane();


    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public KLImageControlSkin(KLImageControl control) {
        super(control);

        getChildren().add(mainContainer);

        mainContainer.getChildren().addAll(
                titleLabel,
                imageContainer,
                attachImageButtonContainer
        );

        clearContainer.getChildren().add(clearGraphic);
        imageContainer.getChildren().addAll(imageView, promptTextLabel, clearContainer);

        clearContainer.setOnMouseClicked(this::onClearAction);

        StackPane.setAlignment(clearContainer, Pos.TOP_RIGHT);
        StackPane.setMargin(clearContainer, new Insets(8));

        attachImageButtonContainer.getChildren().add(attachImageButton);

        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        titleLabel.textProperty().bind(new StringBinding() {
            {
                super.bind(control.titleProperty());
            }
            @Override
            protected String computeValue() {
                String title = control.getTitle();
                if (title != null) {
                    return title;
                } else {
                    return "";
                }
            }
        });

        // Image View
        imageView.setPreserveRatio(true);
        control.imageProperty().subscribe(this::updateImage);

        // Attach Button
        attachImageButton.setText(ATTACH_BUTTON_TEXT);
        attachImageButton.setOnAction(this::attachImageAction);

        promptTextLabel.textProperty().bind(control.promptTextProperty());

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
        promptTextLabel.getStyleClass().add("prompt-text");
        imageContainer.getStyleClass().add("image-container");
        attachImageButtonContainer.getStyleClass().add("image-button-container");

        clearContainer.getStyleClass().add("clear-container");
        clearGraphic.getStyleClass().addAll("icon", "cross");
    }

    private void onClearAction(MouseEvent mouseEvent) {
        getSkinnable().setImage(null);
    }

    private void attachImageAction(ActionEvent actionEvent) {
        KLImageControl control = getSkinnable();

        FileChooser fileChooser = new FileChooser();

        // Set file chooser title
        fileChooser.setTitle("Open Image File");

        // Set extension filters to only show image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(control.getScene().getWindow());
        Image image = new Image(selectedFile.toURI().toString());

        getSkinnable().setImage(image);
    }

    private void updateImage(Image newImage) {
        updatePromptTextAndClearButtonVisibility(newImage);
        updateImageToShow(newImage);
    }

    private void updatePromptTextAndClearButtonVisibility(Image newImage) {
        NodeUtils.setShowing(promptTextLabel, newImage == null);
        NodeUtils.setShowing(clearContainer, newImage != null);
    }

    private void updateImageToShow(Image newImage) {
        if (newImage == null) {
            imageView.setImage(promptImage);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, false);
        } else {
            imageView.setImage(newImage);
            FXUtils.fitImageToBounds(imageView, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, true);
        }
    }
}