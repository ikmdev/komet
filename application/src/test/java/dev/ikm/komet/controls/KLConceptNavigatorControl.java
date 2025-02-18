package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class KLConceptNavigatorControl extends TreeView<ConceptNavigatorModel> {

    private KLConceptNavigatorTreeViewSkin conceptNavigatorTreeViewSkin;

    public KLConceptNavigatorControl() {
        TreeItem<ConceptNavigatorModel> root = new TreeItem<>(new ConceptNavigatorModel("Root"));
        root.setExpanded(true);
        setShowRoot(false);
        setRoot(root);
        setFixedCellSize(24);

        setCellFactory(p -> new KLConceptNavigatorTreeCell(this));
        expandedItemCountProperty().subscribe((o, n) -> {
            if (conceptNavigatorTreeViewSkin == null) {
                return;
            }
            // clean up
            conceptNavigatorTreeViewSkin.unhoverAllItems();
            conceptNavigatorTreeViewSkin.unselectAllItems();
            TreeItem<ConceptNavigatorModel> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                conceptNavigatorTreeViewSkin.selectItem(selectedItem);
            }
        });
        getSelectionModel().selectedItemProperty().subscribe((o, n) -> {
            if (conceptNavigatorTreeViewSkin == null) {
                return;
            }
            conceptNavigatorTreeViewSkin.unhoverAllItems();
            if (o != null) {
                conceptNavigatorTreeViewSkin.unselectAllItems();
            }
            if (n != null) {
                conceptNavigatorTreeViewSkin.selectItem(n);
            }
        });

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
        conceptNavigatorTreeViewSkin = new KLConceptNavigatorTreeViewSkin(this);
        return conceptNavigatorTreeViewSkin;
    }
}
