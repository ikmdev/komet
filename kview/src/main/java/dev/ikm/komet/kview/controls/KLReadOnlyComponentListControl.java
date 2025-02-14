package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentListControlSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

public class KLReadOnlyComponentListControl extends KLReadOnlyMultiComponentControl {

    public KLReadOnlyComponentListControl() {
        getStyleClass().add("read-only-component-list-control");
    }

    // items
    private final ObservableList<ComponentItem> items = FXCollections.observableArrayList();
    public ObservableList<ComponentItem> getItems() { return items; }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentListControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-list-control.css").toExternalForm();
    }
}