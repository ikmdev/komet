package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentSetControlSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

import java.util.ArrayList;

public class KLReadOnlyComponentSetControl extends KLReadOnlyMultiComponentControl {

    public KLReadOnlyComponentSetControl() {
        getStyleClass().add("read-only-component-set-control");
    }

    // items
    //IIA-1463 : On Submit, the order of concepts in the set under Semantic
    // Details should match the order of cocnepts in the set in the Edit Panel
    private final ObservableList<ComponentItem> items = FXCollections.observableList(new ArrayList<>());
    public ObservableList<ComponentItem> getItems() { return items; }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentSetControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-set-control.css").toExternalForm();
    }
}
