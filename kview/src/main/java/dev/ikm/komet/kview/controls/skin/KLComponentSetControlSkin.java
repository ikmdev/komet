package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.util.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * Default skin implementation for the {@link KLComponentSetControl} control
 */
public class KLComponentSetControlSkin extends SkinBase<KLComponentSetControl> {

    private static final Logger LOG = LoggerFactory.getLogger(KLComponentSetControl.class);

    private final Label titleLabel;
    private final Button addEntryButton;
    private final int FIRST_CC_INDEX = 1;

    private final ListChangeListener<Node> nodeListChangeListener = change -> {
        while (change.next()) {
            if (change.wasAdded() && change.getAddedSize() == 1) {
                Entity<?> entity = ((KLComponentControl) change.getAddedSubList().getFirst()).getEntity();
                if (entity != null) {
                    int index = change.getFrom() - FIRST_CC_INDEX;
                    if (index >= getSkinnable().getEntitiesList().size()) {
                        getSkinnable().getEntitiesList().add(entity);
                    } else {
                        getSkinnable().getEntitiesList().add(index, entity);
                    }
                }
            } else if (change.wasRemoved() && change.getRemovedSize() == 1) {
                int index = change.getFrom() - FIRST_CC_INDEX;
                if (index >= 0) {
                    getSkinnable().getEntitiesList().remove(index);
                }
            }
        }

    };

    /**
     * Creates a new KLComponentSetControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLComponentSetControlSkin(KLComponentSetControl control) {
        super(control);

        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");
        titleLabel.textProperty().bind(control.titleProperty());

        addEntryButton = new Button(getString("add.entry.button.text"));
        addEntryButton.getStyleClass().add("add-entry-button");
        addEntryButton.setOnAction(e -> {
            KLComponentControl componentControl = new KLComponentControl();
            Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
                if (entity != null) {
                    int index = getChildren().indexOf(componentControl) - FIRST_CC_INDEX;
                    if (index < control.getEntitiesList().size()) {
                        control.getEntitiesList().set(index, entity);
                    } else {
                        control.getEntitiesList().add(entity);
                    }
                }
            });
            componentControl.setOnRemoveAction(ev -> {
                subscription.unsubscribe();
                getChildren().remove(componentControl);
                if (control.getEntitiesList().isEmpty()) {
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
        addEntryButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                getChildren().stream().anyMatch(n -> n instanceof KLComponentControl cc && cc.getEntity() == null),
                getChildren(), control.entitiesProperty()));

        getSkinnable().setOnMouseDragReleased(Event::consume);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        getChildren().removeListener(nodeListChangeListener);
        addEntryButton.disableProperty().unbind();
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
        return ResourceBundle.getBundle("dev.ikm.komet.kview.controls.component-set-control").getString(key);
    }
}
