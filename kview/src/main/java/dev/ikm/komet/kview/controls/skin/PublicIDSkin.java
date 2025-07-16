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

/// Provides the Skin for the PublicIDControl.
/// The publicIdProperty string value from the PublicIDControl is rendered by this Skin.
///
/// This Skin provides the following in an HBox layout:
/// - Title label
/// - Public ID label
/// - Copy to Clipboard button - when pressed, copies the Public ID text to the System clipboard
///
/// The publicIdLable and copyToClipboardButton are contained within an HBox, where the HBox is configured
/// to handle mouse enter and exit events which is used to show and hide the copyToClipboardButton.
public class PublicIDSkin implements Skin<PublicIDControl>  {

    /// The spacing between children in the HBoxes used in this Skin
    private static final double HBOX_SPACING = 4.0;

    /// The root Node for the Skin
    private HBox rootHBox = new HBox();

    /// The title label that precedes the public ID UUID label
    private Label titleLabel = new Label("IDENTIFIER:");

    /// The HBox that contians the public ID label and copy to clipboard button
    private HBox publicIdHBox = new HBox();

    /// The Label that displays the public ID UUID value
    private Label publicIdLabel = new Label("");

    /// The tooltip for the publicIdLabel, which is needed because the text in the label
    /// could exceed the Label width
    private Tooltip publicIdTooltip = new Tooltip();

    /// The copy to clipboard button, which is shown and hidden based on the mouse entering
    /// and exiting the publicIdHBox
    private Button copyToClipboardButton = new Button();

    /// The control that this Skin renders
    private PublicIDControl control;

    /// The subscription to the PublicIDControl publicIdProperty, which receives property change events
    private Subscription subscription;

    /// The current public id property value, as received in the subscription listener
    private String identifier;

    public PublicIDSkin(PublicIDControl control) {
        this.control = control;

        Tooltip.install(publicIdLabel, publicIdTooltip);

        rootHBox.setAlignment(Pos.CENTER_LEFT);
        rootHBox.setSpacing(HBOX_SPACING);

        titleLabel.setFont(new Font("Noto Sans Bold", 10.0));
        publicIdLabel.setFont(new Font("Noto Sans", 10.0));

        // the SVG graphic for the copy to clipboard icon
        var svgPath = new SVGPath();
        svgPath.setContent(SVGConstants.COPY_TO_CLIPBOARD_SVG_PATH);
        svgPath.setFillRule(FillRule.EVEN_ODD);
        svgPath.getStyleClass().add("copy-to-clipboard");

        copyToClipboardButton.setGraphic(svgPath);
        copyToClipboardButton.getStyleClass().add("add-pencil-button");
        Tooltip.install(copyToClipboardButton, new Tooltip("Copy to Clipboard"));

        // the publicIdHBox contains the publicIdLabel and the copyToClipboardButton.
        // Both controls need to be in a single HBox to be able to show and hide the
        // Button when the mouse enters and exits the HBox.
        publicIdHBox.setAlignment(Pos.CENTER_LEFT);
        publicIdHBox.setSpacing(HBOX_SPACING);
        publicIdHBox.getChildren().addAll(publicIdLabel, copyToClipboardButton);

        rootHBox.getChildren().addAll(titleLabel, publicIdHBox);

        // handle the button press action to copy the public id UUID value to the clipboard
        copyToClipboardButton.setOnAction(event -> {
            copyToClipboard();
            event.consume();
        });

        // initially hide the button
        copyToClipboardButton.setVisible(false);

        // when the mouse enters, show the button
        publicIdHBox.setOnMouseEntered(event -> {
            copyToClipboardButton.setVisible(true);
        });
        // when the mouse exits, hide the button
        publicIdHBox.setOnMouseExited(event -> {
            copyToClipboardButton.setVisible(false);
        });

        // subscribe to changes to the publicIdProperty in the PublicIDControl
        subscription = control.publicIdProperty().subscribe(publicId -> {
            identifier = publicId;
            publicIdLabel.setText(publicId);
            publicIdTooltip.setText(publicId);
        });
    }

    /// Copy the Public Identifier UUID String value to the System Clipboard
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

    /// Unsubscribes from the subscription to stop receiving the publicIdProperty change events
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
