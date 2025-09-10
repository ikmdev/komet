package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.PublicIDControl;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.VBox;
import javafx.util.Subscription;

import java.util.List;

/// Provides the Skin for the PublicIDListControl.
/// This control is a composite control that contains a VBox of PublicIDControls.
/// The PublicIDControls that are created have the label turned off and the publicId value set.
public class PublicIDListControlSkin extends SkinBase<PublicIDListControl> {

    /// The VBox is placed within a ScrollPane, which is the root control of this custom control
    private final ScrollPane rootScrollPane = new ScrollPane();

    /// The root Node for the Skin
    private final VBox identifierVBox = new VBox();

    /// The subscription to the PublicIDControl publicIdProperty, which receives property change events
    private Subscription subscription;

    /// The current public id property value, as received in the subscription listener
    private List<String> identifierList;

    public PublicIDListControlSkin(PublicIDListControl control) {
        super(control);

        rootScrollPane.getStyleClass().add("public-id-list");
        identifierVBox.getStyleClass().add("identifier-vbox");

        rootScrollPane.setContent(identifierVBox);

        // subscribe to changes to the publicIdProperty in the PublicIDControl
        subscription = control.publicIdListProperty().subscribe(publicIdList -> {
            identifierList = publicIdList;

            identifierVBox.getChildren().clear();

            if (publicIdList != null && !publicIdList.isEmpty()) {
                for (String identifier : publicIdList) {
                    PublicIDControl publicIDControl = new PublicIDControl(false, identifier);

                    identifierVBox.getChildren().add(publicIDControl);
                }
            }
        });

        getChildren().add(rootScrollPane);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        rootScrollPane.resizeRelocate(x, y, w, h);
    }

    /// Unsubscribes from the subscription to stop receiving the publicIdProperty change events
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
