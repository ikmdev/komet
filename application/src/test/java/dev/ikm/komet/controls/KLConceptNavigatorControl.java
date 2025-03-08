package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
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

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class KLConceptNavigatorControl extends TreeView<ConceptFacade> {

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
            ConceptNavigatorTreeItem selectedItem = (ConceptNavigatorTreeItem) getSelectionModel().getSelectedItem();
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
                conceptNavigatorTreeViewSkin.selectAllAncestors((ConceptNavigatorTreeItem) n);
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
    private final ObjectProperty<Consumer<List<ConceptFacade>>> onActionProperty = new SimpleObjectProperty<>(this, "onAction");
    public final ObjectProperty<Consumer<List<ConceptFacade>>> onActionProperty() {
       return onActionProperty;
    }
    public final Consumer<List<ConceptFacade>> getOnAction() {
       return onActionProperty.get();
    }
    public final void setOnAction(Consumer<List<ConceptFacade>> value) {
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

    // navigatorProperty
    private final ObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>(this, "navigator") {
        @Override
        protected void invalidated() {
            if (get() != null) {
                setRoot(getConceptNavigatorRoot().getFirst());
            }
        }
    };
    public final ObjectProperty<Navigator> navigatorProperty() {
       return navigatorProperty;
    }
    public final Navigator getNavigator() {
       return navigatorProperty.get();
    }
    public final void setNavigator(Navigator value) {
        navigatorProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        conceptNavigatorTreeViewSkin = new KLConceptNavigatorTreeViewSkin(this);
        return conceptNavigatorTreeViewSkin;
    }

    private List<ConceptNavigatorTreeItem> getConceptNavigatorRoot() {
        return Arrays.stream(getNavigator().getRootNids())
                .mapToObj(rootNid -> {
                    ConceptNavigatorTreeItem treeItem = getConceptNavigatorTreeItem(rootNid);
                    treeItem.setExpanded(true);
                    return treeItem;
                })
                .toList();
    }

    private List<ConceptNavigatorTreeItem> getChildren(int nid) {
        return getNavigator().getChildEdges(nid).stream()
                .map(edge -> getConceptNavigatorTreeItem(edge.destinationNid()))
                .toList();
    }

    private ConceptNavigatorTreeItem getConceptNavigatorTreeItem(int nid) {
        ConceptNavigatorTreeItem conceptNavigatorTreeItem = createSingleConceptNavigatorTreeItem(nid);
        if (!getNavigator().getChildEdges(nid).isEmpty()) {
            conceptNavigatorTreeItem.getChildren().addAll(getChildren(nid));
        }
        return conceptNavigatorTreeItem;
    }

    public ConceptNavigatorTreeItem createSingleConceptNavigatorTreeItem(int nid) {
        ConceptFacade facade = Entity.getFast(nid);
        ConceptNavigatorTreeItem conceptNavigatorTreeItem = new ConceptNavigatorTreeItem(facade);
        conceptNavigatorTreeItem.setDefined(getNavigator().getViewCalculator().hasSufficientSet(facade));
        conceptNavigatorTreeItem.setMultiParent(getNavigator().getParentNids(nid).length > 1);
        return conceptNavigatorTreeItem;
    }

    public List<ConceptNavigatorTreeItem> getSecondaryParents(int nid, int parentNid) {
        return getNavigator().getParentEdges(nid).stream()
                .filter(edge -> edge.destinationNid() != parentNid)
                .map(edge -> getTreeViewItem((ConceptNavigatorTreeItem) getRoot(), edge.destinationNid()))
                .toList();
    }

    private static ConceptNavigatorTreeItem getTreeViewItem(ConceptNavigatorTreeItem item, int nid) {
        if (item == null || item.getValue() == null) {
            return null;
        }
        if (item.getValue().nid() == nid) {
            return item;
        }
        for (TreeItem<ConceptFacade> child : item.getChildren()) {
            ConceptNavigatorTreeItem treeItem = getTreeViewItem((ConceptNavigatorTreeItem) child, nid);
            if (treeItem != null) {
                return treeItem;
            }
        }
        return null;
    }
}
