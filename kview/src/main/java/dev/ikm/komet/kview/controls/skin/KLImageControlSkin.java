package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.KLImageControl;
import javafx.beans.binding.StringBinding;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
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

    private static final String NO_SELECTION_TEXT_FIELD_TEXT = "No Selection";
    private static final String ATTACH_BUTTON_TEXT = "ATTACH IMAGE";

    private final VBox mainContainer = new VBox();

    protected final Label titleLabel = new Label();
    private final TextField textField = new TextField();

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
                textField,
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

        textField.setPrefWidth(Double.MAX_VALUE);
        textField.setMaxWidth(Region.USE_PREF_SIZE);

        textField.setText(NO_SELECTION_TEXT_FIELD_TEXT);
        textField.setEditable(false);
        textField.setFocusTraversable(false);

        attachImageButton.setText(ATTACH_BUTTON_TEXT);
        attachImageButton.setOnAction(this::attachImageAction);

        promptTextLabel.textProperty().bind(control.promptTextProperty());
        initImage(control);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
        promptTextLabel.getStyleClass().add("prompt-text");
        textField.getStyleClass().add("text");
        imageContainer.getStyleClass().add("image-container");
        attachImageButtonContainer.getStyleClass().add("image-button-container");

        clearContainer.getStyleClass().add("clear-container");
        clearGraphic.getStyleClass().addAll("icon", "cross");
    }

    private void onClearAction(MouseEvent mouseEvent) {
        getSkinnable().setImageFile(null);
    }

    private void attachImageAction(ActionEvent actionEvent) {
        KLImageControl control = getSkinnable();

        FileChooser fileChooser = new FileChooser();

        // Set file chooser title
        fileChooser.setTitle("Open Image File");

        // Set extension filters to only show image files
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(control.getScene().getWindow());

        getSkinnable().setImageFile(selectedFile);
    }

    private void initImage(KLImageControl control) {
        control.imageFileProperty().addListener(observable -> {
            updateImage(control);
        });
        updateImage(control);
    }

    private void updateImage(KLImageControl control) {
        updatePromptTextAndClearButtonVisibility(control);
        updateImageToShow(control);
    }

    private void updatePromptTextAndClearButtonVisibility(KLImageControl control) {
        NodeUtils.setShowing(promptTextLabel, control.getImageFile() == null);
        NodeUtils.setShowing(clearContainer, control.getImageFile() != null);
    }

    private void updateImageToShow(KLImageControl control) {
        if (control.getImageFile() == null) {
            imageView.setImage(promptImage);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, false);
        } else {
            File imageFile = control.getImageFile();
            Image image = new Image(imageFile.toURI().toString());
            String fileName = imageFile.getName();

            imageView.setImage(image);
            textField.setText(fileName);

            pseudoClassStateChanged(IMAGE_SELECTED_PSEUDO_CLASS, true);
        }
    }
}