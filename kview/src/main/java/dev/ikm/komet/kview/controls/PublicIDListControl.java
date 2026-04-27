package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDListControlSkin;
import dev.ikm.komet.kview.mvvm.model.DataModelHelper;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/// Control for the Public ID UUID list.
/// This control only has a single property, which is rendered in the default skin PublicIDListControlSkin.
public class PublicIDListControl extends Control {

    public static final String KOMET_ID_IDENTIFIER_PREFIX = "Komet ID: ";

    public PublicIDListControl() {
        getStyleClass().add("public-id-list");

        // make styles reloadable
        sceneProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (getScene() != null) {
                    getScene().getStylesheets().add(getUserAgentStylesheet());
                }
                sceneProperty().removeListener(this);
            }
        });
    }

    /// Creates a Public ID List from the provided viewCalculator and EntityFacade.
    /// @param viewCalculator the view calculator to use to determine identifiers
    /// @param entityFacade the entity facade to use to determine identifiers
    public void updatePublicIdList(ViewCalculator viewCalculator, EntityFacade entityFacade) {
        if (viewCalculator != null && entityFacade != null) {
            List<String> idList = entityFacade.publicId().asUuidList().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());

            idList.addAll(DataModelHelper.getIdsToAppend(viewCalculator, entityFacade.toProxy()));

            // this assumes that the first ID is always the Komet ID
            if (!idList.isEmpty()) {
                var firstID = KOMET_ID_IDENTIFIER_PREFIX + idList.getFirst();
                idList.set(0, firstID);
            }

            // to test the vertical scroll bar, uncomment the following line
    //        idList.addAll(Arrays.asList("test line 1", "test line 2", "test vertical scroll bar"));

            setPublicIdList(idList);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new PublicIDListControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return PublicIDListControl.class.getResource("public-id-list-control.css").toExternalForm();
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- public id list
    private SimpleObjectProperty<List<String>> publicIdList = new SimpleObjectProperty<>();
    public SimpleObjectProperty<List<String>> publicIdListProperty() { return publicIdList; }
    public List<String> getPublicIdList() { return publicIdList.get(); }
    public void setPublicIdList(List<String> publicIdList) { this.publicIdList.set(publicIdList); }
}
