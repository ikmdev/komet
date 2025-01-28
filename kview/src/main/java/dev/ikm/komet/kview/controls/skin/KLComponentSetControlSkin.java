package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLComponentControl;
import dev.ikm.komet.kview.controls.KLComponentSetControl;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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

    private final ListChangeListener<Node> nodeListChangeListener = change -> {
        while(change.next()) {
            if (change.wasAdded() && change.getAddedSize() == 1) {
                EntityProxy entity = ((KLComponentControl) change.getAddedSubList().getFirst()).getEntity();
                if (entity != null) {
                    getSkinnable().getEntitiesSet().add(entity);
                }
            } else if (change.wasRemoved() && change.getRemovedSize() == 1) {
                EntityProxy entity = ((KLComponentControl) change.getRemoved().getFirst()).getEntity();
                getSkinnable().getEntitiesSet().remove(entity);
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
        addEntryButton.setOnAction(e -> createComponentUI());
        getChildren().addAll(titleLabel, addEntryButton);
        getChildren().addListener(nodeListChangeListener);
        // Only allow one empty KLComponentControl
        BooleanBinding booleanBinding = Bindings.createBooleanBinding(() ->
                        getChildren().stream().anyMatch(n ->
                                     n instanceof KLComponentControl cc && cc.getEntity() == null)
                ,getChildren(), control.entitiesProperty());
        addEntryButton.disableProperty().bind(booleanBinding);

        getSkinnable().setOnMouseDragReleased(Event::consume);

        control.getEntitiesSet().forEach(this::createComponentUI);

    }

    private void createComponentUI(){
        createComponentUI(null);
    }

    private void createComponentUI(EntityProxy entityProxy) {
        {
            KLComponentSetControl klComponentSetControl = getSkinnable();
            KLComponentControl componentControl = new KLComponentControl();
            componentControl.setEntity(entityProxy);
            Subscription subscription = componentControl.entityProperty().subscribe(entity -> {
                if (entity != null && !klComponentSetControl.getEntitiesSet().contains(entity)) {
                    klComponentSetControl.getEntitiesSet().add(entity);
                    componentControl.setEntity(entity);
                }
            });
            componentControl.setOnRemoveAction(ev -> {
                subscription.unsubscribe();
                getChildren().remove(componentControl);
                if (klComponentSetControl.getEntitiesSet().isEmpty()) {
                    addEntryButton.fire();
                }
            });

            getChildren().add(componentControl);
            getSkinnable().requestLayout();
        }
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
