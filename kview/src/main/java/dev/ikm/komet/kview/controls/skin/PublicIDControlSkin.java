package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.PublicIDControl;
import dev.ikm.komet.kview.mvvm.view.common.SVGConstants;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.util.Subscription;

/// Provides the Skin for the PublicIDControl.
/// The publicIdProperty string value from the PublicIDControl is rendered by this Skin.
///
/// - Title label
/// - Public ID label
/// - Copy to Clipboard button - when pressed, copies the Public ID text to the System clipboard
///
/// The publicIdLable and copyToClipboardButton are contained within an HBox, where the HBox is configured
/// to handle mouse enter and exit events which is used to show and hide the copyToClipboardButton.
public class PublicIDControlSkin extends SkinBase<PublicIDControl> {

    /// The root Node for the Skin
    private final BorderPane rootBorderPane = new BorderPane();

    /// The TextField that displays the public ID UUID value
    private final Label publicIdLabel = new Label("");

    /// The tooltip for the publicIdLabel, which is needed because the text in the label
    /// could exceed the Label width
    private final Tooltip publicIdTooltip = new Tooltip();

    /// The copy to clipboard button, which is shown and hidden based on the mouse entering
    /// and exiting the publicIdHBox
    private final Button copyToClipboardButton = new Button();

    /// The subscription to the PublicIDControl publicIdProperty, which receives property change events
    private Subscription publicIdSubscription;

    /// The current public id property value, as received in the subscription listener
    private String identifier;

    public PublicIDControlSkin(PublicIDControl control) {
        super(control);

        Tooltip.install(publicIdLabel, publicIdTooltip);

        rootBorderPane.getStyleClass().add("public-id");

        publicIdLabel.getStyleClass().addAll("public-id-label", "copyable-label");

        // the SVG graphic for the copy to clipboard icon
        var svgPath = new SVGPath();
        svgPath.setContent(SVGConstants.COPY_TO_CLIPBOARD_SVG_PATH);
        svgPath.setFillRule(FillRule.EVEN_ODD);
        svgPath.getStyleClass().add("copy-to-clipboard-svg");

        copyToClipboardButton.setGraphic(svgPath);
        copyToClipboardButton.getStyleClass().add("copy-to-clipboard-button");
        Tooltip.install(copyToClipboardButton, new Tooltip("Copy to Clipboard"));

        rootBorderPane.setCenter(publicIdLabel);
        rootBorderPane.setRight(copyToClipboardButton);

        // handle the button press action to copy the public id UUID value to the clipboard
        copyToClipboardButton.setOnAction(event -> {
            copyToClipboard();
            event.consume();
        });

        // initially hide the button
        copyToClipboardButton.setVisible(false);

        // when the mouse enters, show the button
        rootBorderPane.setOnMouseEntered(event -> {
            copyToClipboardButton.setVisible(true);
        });
        // when the mouse exits, hide the button
        rootBorderPane.setOnMouseExited(event -> {
            copyToClipboardButton.setVisible(false);
        });

        getChildren().add(rootBorderPane);

        // subscribe to changes to the publicIdProperty in the PublicIDControl
        publicIdSubscription = control.publicIdProperty().subscribe(publicId -> {
            identifier = publicId;
            publicIdTooltip.setText(identifier);
            publicIdLabel.setText(identifier);
        });
    }

    /// Trims the identifier to remove the prefix and colon if the trimIdentifier property has been set to true
    private String trimTheIdentifier() {
        String trimmedIdentifier = identifier;

        if (identifier != null) {
            int colonIndex = identifier.indexOf(':');
            if (colonIndex >= 0) {
                // trim the prefix
                trimmedIdentifier = identifier.substring(colonIndex + 1);

                // trim the whitespace
                trimmedIdentifier = trimmedIdentifier.trim();
            }
        }

        return trimmedIdentifier;
    }

    /// Copy the Public Identifier UUID String value to the System Clipboard
    private void copyToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(trimTheIdentifier());
        clipboard.setContent(content);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        rootBorderPane.resizeRelocate(x, y, w, h);
    }

    /// Unsubscribes from the subscription to stop receiving the publicIdProperty change events
    @Override
    public void dispose() {
        if (publicIdSubscription != null) {
            publicIdSubscription.unsubscribe();
        }
    }

}
