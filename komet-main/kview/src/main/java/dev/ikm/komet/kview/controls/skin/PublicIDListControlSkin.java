package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.PublicIDControl;
import dev.ikm.komet.kview.controls.PublicIDListControl;
import javafx.application.Platform;
import javafx.scene.control.ScrollBar;
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

        identifierVBox.getStyleClass().add("identifier-vbox");

        rootScrollPane.setContent(identifierVBox);

        // subscribe to changes to the publicIdProperty in the PublicIDControl
        subscription = control.publicIdListProperty().subscribe(publicIdList -> {
            identifierList = publicIdList;

            identifierVBox.getChildren().clear();

            if (publicIdList != null && !publicIdList.isEmpty()) {
                for (String identifier : publicIdList) {
                    PublicIDControl publicIDControl = new PublicIDControl(identifier);

                    identifierVBox.getChildren().add(publicIDControl);
                }
            }
        });

        getChildren().add(rootScrollPane);
    }

    /// Determines if the vertical scroll bar is visible
    private boolean isVerticalScrollBarVisible() {
        boolean isVisible = false;
        // the vertical scroll bar must be looked up each time this method is called
        // because the ScrollBar object changes during the life of a ScrollPane
        ScrollBar verticalScrollBar = (ScrollBar) rootScrollPane.lookup(".scroll-bar:vertical");;

        if (verticalScrollBar != null) {
            isVisible = verticalScrollBar.isVisible();
        }

        return isVisible;
    }

    /// Layout the children, which includes the ScrollPane and the VBox.
    /// This method sets the width of the VBox for optimal display, which is
    /// based on the visibility of the vertical ScrollBar.
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        rootScrollPane.resizeRelocate(x, y, w, h);

        // runLater() added to fix a weird error of the PublicIDListControl having a small width on
        // initial creation of the concept, pattern, and semantic windows.
        Platform.runLater(() -> {
            // if the vertical scroll bar is visible set the width of the
            // vbox to the viewport width
            if (isVerticalScrollBarVisible()) {
                var viewPortBounds = rootScrollPane.getViewportBounds();

                identifierVBox.setPrefWidth(viewPortBounds.getWidth());
            } else {
                // if not visible, set the width to the same as the PublicIDListControlSkin (this) width
                identifierVBox.setPrefWidth(w);
            }
        });
    }

    /// Unsubscribes from the subscription to stop receiving the publicIdProperty change events
    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

}
