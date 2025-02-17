package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class KLConceptNavigatorControl extends TreeView<String> {

    public KLConceptNavigatorControl() {
        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        setShowRoot(false);
        setRoot(root);
        setFixedCellSize(24);
        setCellFactory(p -> new KLConceptNavigatorTreeCell(this));

        getStyleClass().add("concept-navigator-control");
        getStylesheets().add(KLConceptNavigatorControl.class.getResource("concept-navigator.css").toExternalForm());
    }

    // headerProperty
    private final StringProperty headerProperty = new SimpleStringProperty(this, "header");
    public final StringProperty headerProperty() {
       return headerProperty;
    }
    public final String getHeader() {
       return headerProperty.get();
    }
    public final void setHeader(String value) {
        headerProperty.set(value);
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLConceptNavigatorTreeViewSkin(this);
    }
}
