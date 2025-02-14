package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentSetControlSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Skin;

public class KLReadOnlyComponentSetControl extends KLReadOnlyMultiComponentControl {

    public KLReadOnlyComponentSetControl() {
        getStyleClass().add("read-only-component-set-control");
    }

    // items
    private final ObservableSet<ComponentItem> items = FXCollections.observableSet();
    public ObservableSet<ComponentItem> getItems() { return items; }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentSetControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-set-control.css").toExternalForm();
    }
}
