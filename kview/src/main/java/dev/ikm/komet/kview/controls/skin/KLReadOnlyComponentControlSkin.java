package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KLReadOnlyComponentControlSkin extends SkinBase<KLReadOnlyComponentControl> {
    private static final PseudoClass EDIT_MODE_PSEUDO_CLASS = PseudoClass.getPseudoClass("edit-mode");

    private final VBox mainContainer = new VBox();

    private final Label titleLabel = new Label();

    private final HBox textContainer = new HBox();
    private final ImageView iconImageView = new ImageView();
    private final Label textLabel = new Label();

    private InvalidationListener editModeChanged = this::onEditModeChanged;

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentControlSkin(KLReadOnlyComponentControl control) {
        super(control);

        mainContainer.getChildren().addAll(titleLabel, textContainer);
        textContainer.getChildren().addAll(iconImageView, textLabel);
        getChildren().add(mainContainer);

        titleLabel.textProperty().bind(control.titleProperty());
        textLabel.textProperty().bind(control.textProperty());
        iconImageView.imageProperty().bind(control.iconProperty());
        control.editModeProperty().addListener(editModeChanged);

        mainContainer.setFillWidth(true);
        titleLabel.setPrefWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Region.USE_PREF_SIZE);
        textLabel.setMaxWidth(Region.USE_PREF_SIZE);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        iconImageView.setFitWidth(20);
        iconImageView.setFitHeight(20);

        // CSS
        mainContainer.getStyleClass().add("main-container");
        titleLabel.getStyleClass().add("title");
        textContainer.getStyleClass().add("text-container");
        textLabel.getStyleClass().add("text");
    }

    private void onEditModeChanged(Observable observable) {
        pseudoClassStateChanged(EDIT_MODE_PSEUDO_CLASS, getSkinnable().isEditMode());
    }
}