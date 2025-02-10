package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentListControl;
import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.util.Subscription;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link KLComponentListControl} control
 */
public class KLComponentListControlSkin extends SkinBase<KLComponentListControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentListControlSkin.class);;

    private final Label titleLabel;
    private final Button addEntryButton;
    private final int FIRST_CC_INDEX = 1;

    private final ListChangeListener<Node> nodeListChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded() && change.getAddedSize() == 1) {
                EntityProxy entity = ((KLComponentControl) change.getAddedSubList().getFirst()).getEntity();
                if (entity != null) {
                    int index = change.getFrom();
                    IntIdList intIdList = getSkinnable().getValue();
                    MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());

                    if (index >= getSkinnable().getValue().size()) {
                        mutableList.add(entity.nid());
                    } else {
                        if (index < 0) {
                            mutableList.addAtIndex(0, entity.nid());
                        } else {
                            mutableList.addAtIndex(index, entity.nid());
                        }
                    }
                    getSkinnable().setValue(IntIds.list.of(mutableList.toArray()));
                }
            } else if (change.wasRemoved() && change.getRemovedSize() == 1) {
                EntityProxy entity = ((KLComponentControl) change.getRemoved().getFirst()).getEntity();
                int index = change.getFrom();
                IntIdList intIdList = getSkinnable().getValue();
                MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                if (index < 0) {
                    mutableList.remove(intIdList.get(0));
                } else {
                    if (index >= getSkinnable().getValue().size()) {
                        mutableList.remove(intIdList.get(index-1));
                    } else {
                        mutableList.remove(intIdList.get(index));
                    }

                }
                getSkinnable().setValue(IntIds.list.of(mutableList.toArray()));
            }
        }
    };

    /**
     * Creates a new KLComponentListControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLComponentListControlSkin(KLComponentListControl control) {
        super(control);
        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());
        for(int i = 0; i < control.getValue().size(); i++) {
            int nid = control.getValue().get(i);
            if (nid != 0) {
                EntityProxy entityProxy = EntityProxy.make(nid);
                createComponentUI(entityProxy.nid());
            }
        }
        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(e -> {
            KLComponentControl componentControl = new KLComponentControl();
            Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
                if (entity != null) {
                    int index = getChildren().indexOf(componentControl) - FIRST_CC_INDEX;
                    if (index < control.getValue().size()) {
                        IntIdList intIdList = control.getValue();
                        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                        mutableList.set(index, entity.nid());
                        control.setValue(IntIds.list.of(mutableList.toArray()));
                    } else {
                        IntIdList intIdList = control.getValue();
                        MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                        mutableList.add(entity.nid());
                        control.setValue(IntIds.list.of(mutableList.toArray()));
                    }
                }
            });
            componentControl.setOnRemoveAction(ev -> {
                subscription.unsubscribe();
                getChildren().remove(componentControl);
                if (control.getValue().isEmpty()) {
                    addEntryButton.fire();
                }
            });
            getChildren().add(getChildren().size() - 1, componentControl);
            getSkinnable().requestLayout();
        });
        getChildren().addAll(titleLabel, addEntryButton);
        getChildren().addListener(nodeListChangeListener);
        addEntryButton.fire();
        // Only allow one empty KLComponentControl
        addEntryButton
                .disableProperty()
                .bind(Bindings.createBooleanBinding(() ->
                        getChildren()
                                .stream()
                                .anyMatch(n -> n instanceof KLComponentControl cc && cc.getEntity() == null),
                        getChildren(),
                        control.valueProperty()));

        getSkinnable().setOnMouseDragReleased(Event::consume);
    }

    private void createComponentUI(int nid) {
        KLComponentListControl control = getSkinnable();
        KLComponentControl componentControl = new KLComponentControl();
        if (nid != 0) {
            EntityProxy entityProxy = EntityProxy.make(nid);
            componentControl.setEntity(entityProxy);
        }
        Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
            if (entity != null && !control.getValue().contains(entity.nid())) {
                //control.getEntitiesList().add(entity);
                IntIdList intIdList = control.getValue();
                MutableIntList mutableList = IntLists.mutable.of(intIdList.toArray());
                mutableList.add(entity.nid());
                control.setValue(IntIds.list.of(mutableList.toArray()));
            }
        });
        componentControl.setOnRemoveAction(ev -> {
            subscription.unsubscribe();
            getChildren().remove(componentControl);
            if (control.getValue().isEmpty()) {
                addEntryButton.fire();
            }
        });

        // This needs testing: Label, component1, component2, add button.
        int index = 0;
        if (!getChildren().isEmpty()) {
            index = getChildren().size() - 1;
        }
        getChildren().add(componentControl);
        getSkinnable().requestLayout();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        getChildren().removeListener(nodeListChangeListener);
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        Insets padding = getSkinnable().getPadding();
        double labelPrefWidth = titleLabel.prefWidth(-1);
        double labelPrefHeight = titleLabel.prefHeight(labelPrefWidth);
        double x = contentX + padding.getLeft();
        double y = contentY + padding.getTop();
        titleLabel.resizeRelocate(x, y, labelPrefWidth, labelPrefHeight);
        y += labelPrefHeight;
        double spacing = 10;
        for (KLComponentControl componentControl : getChildren().stream()
                .filter(KLComponentControl.class::isInstance)
                .map(KLComponentControl.class::cast)
                .toList()) {
            double componentControlPrefWidth = contentWidth - padding.getRight() - x;
            double componentControlPrefHeight = componentControl.prefHeight(componentControlPrefWidth);
            componentControl.resizeRelocate(x, y, componentControlPrefWidth, componentControlPrefHeight);
            y += componentControlPrefHeight + spacing;
        }
        double buttonPrefWidth = addEntryButton.prefWidth(-1);
        addEntryButton.resizeRelocate(contentWidth - buttonPrefWidth - padding.getRight(), y, buttonPrefWidth, addEntryButton.prefHeight(buttonPrefWidth));
    }

    private static String getString(String key) {
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-list-control").getString(key);
    }
}
