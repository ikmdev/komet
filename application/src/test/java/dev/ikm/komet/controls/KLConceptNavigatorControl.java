package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.List;
import java.util.function.Consumer;

public class KLConceptNavigatorControl extends TreeView<ConceptNavigatorModel> {

    private KLConceptNavigatorTreeViewSkin conceptNavigatorTreeViewSkin;

    public KLConceptNavigatorControl() {
        setShowRoot(false);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
                conceptNavigatorTreeViewSkin.selectAllAncestors(selectedItem);
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
                conceptNavigatorTreeViewSkin.selectAllAncestors(n);
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

    // onActionProperty
    private final ObjectProperty<Consumer<List<ConceptNavigatorModel>>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<Consumer<List<ConceptNavigatorModel>>> onActionProperty() {
       return onActionProperty;
    }
    public final Consumer<List<ConceptNavigatorModel>> getOnAction() {
       return onActionProperty.get();
    }
    public final void setOnAction(Consumer<List<ConceptNavigatorModel>> value) {
        onActionProperty.set(value);
    }

    // activationProperty
    private final DoubleProperty activationProperty = new SimpleDoubleProperty(this, "activation", 500);
    public final DoubleProperty activationProperty() {
       return activationProperty;
    }
    public final double getActivation() {
       return activationProperty.get();
    }
    public final void setActivation(double value) {
        activationProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        conceptNavigatorTreeViewSkin = new KLConceptNavigatorTreeViewSkin(this);
        return conceptNavigatorTreeViewSkin;
    }
}
