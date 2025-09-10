package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.PublicIDListControlSkin;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.List;

public class PublicIDListControl extends Control {

    /// The public ID (UUID)
    private SimpleListProperty<String> publicIdList = new SimpleListProperty<>(this, "publicIdList");

    public SimpleListProperty<String> publicIdListProperty() {
        return publicIdList;
    }

    public List<String> getPublicIdList() {
        return publicIdListProperty().get();
    }

    public void setPublicIdList(List<String> publicIdList) {
        ObservableList<String> obsList = publicIdList != null ? FXCollections.observableList(publicIdList) : FXCollections.observableList(new ArrayList<>());

        publicIdListProperty().set(obsList);
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

}
