package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.PublicIDControl;
import dev.ikm.komet.kview.mvvm.view.common.SVGConstants;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.util.Subscription;

public class PublicIDSkin implements Skin<PublicIDControl>  {

    private HBox rootHBox = new HBox();
    private Label titleLabel = new Label("IDENTIFIER:");
    private HBox publicIdHBox = new HBox();
    private Label publicIdLabel = new Label("");
    private Tooltip publicIdTooltip = new Tooltip();
    private Button copyToClipboardButton = new Button();

    private PublicIDControl control;
    private Subscription subscription;
    private String identifier;

    public PublicIDSkin(PublicIDControl control) {
        this.control = control;

        Tooltip.install(publicIdLabel, publicIdTooltip);

        rootHBox.setAlignment(Pos.CENTER_LEFT);
        rootHBox.setSpacing(4.0);

        titleLabel.setFont(new Font("Noto Sans Bold", 10.0));
        publicIdLabel.setFont(new Font("Noto Sans", 10.0));

        var svgPath = new SVGPath();
        svgPath.setContent(SVGConstants.COPY_TO_CLIPBOARD_SVG_PATH);
        svgPath.setFillRule(FillRule.EVEN_ODD);

        copyToClipboardButton.setGraphic(svgPath);
        copyToClipboardButton.setPrefSize(10, 10);
        copyToClipboardButton.getStyleClass().add("add-pencil-button");

        publicIdHBox.setAlignment(Pos.CENTER_LEFT);
        publicIdHBox.setSpacing(4.0);
        publicIdHBox.getChildren().addAll(publicIdLabel, copyToClipboardButton);

        rootHBox.getChildren().addAll(titleLabel, publicIdHBox);

        Tooltip.install(copyToClipboardButton, new Tooltip("Copy to clipboard"));

        copyToClipboardButton.setOnAction(event -> {
            copyToClipboard();
            event.consume();
        });
        copyToClipboardButton.setVisible(false);

        publicIdHBox.setOnMouseEntered(event -> {
            copyToClipboardButton.setVisible(true);
        });
        publicIdHBox.setOnMouseExited(event -> {
            copyToClipboardButton.setVisible(false);
        });

        subscription = control.publicIdProperty().subscribe(publicId -> {
            identifier = publicId;
            publicIdLabel.setText(publicId);
            publicIdTooltip.setText(publicId);
        });
    }

    /// Copy the Public Identifier string value to the System Clipboard
    private void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(identifier);
        clipboard.setContent(content);
    }

    @Override
    public PublicIDControl getSkinnable() {
        return control;
    }

    @Override
    public Node getNode() {
        return rootHBox;
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
