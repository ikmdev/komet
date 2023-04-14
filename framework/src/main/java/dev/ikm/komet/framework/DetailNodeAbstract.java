package dev.ikm.komet.framework;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;

public abstract class DetailNodeAbstract extends ExplorationNodeAbstract {

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected final FlowSubscriber<Integer> invalidationSubscriber;

    {
        entityFocusProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newValue));
                toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newValue));
            } else {
                titleProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
                toolTipTextProperty.set(EntityLabelWithDragAndDrop.EMPTY_TEXT);
            }
        });
    }

    public DetailNodeAbstract(ViewProperties viewProperties, KometPreferences nodePreferences) {
        super(viewProperties, nodePreferences);
        revertPreferences();
        this.invalidationSubscriber = new FlowSubscriber<>(nid -> {
            if (entityFocusProperty.get() != null && entityFocusProperty.get().nid() == nid) {
                // component has changed, need to update.
                Platform.runLater(() -> entityFocusProperty.set(null));
                Platform.runLater(() -> entityFocusProperty.set(Entity.provider().getEntityFast(nid)));
            }
        });
    }


    @Override
    public final void revertAdditionalPreferences() {
        if (nodePreferences.hasKey(DetailNodeKey.ENTITY_FOCUS)) {
            nodePreferences.getEntity(DetailNodeKey.ENTITY_FOCUS).ifPresentOrElse(entityFacade -> entityFocusProperty.set(entityFacade),
                    () -> entityFocusProperty.set(null));
        }
        revertDetailsPreferences();
    }

    @Override
    protected final void saveAdditionalPreferences() {
        if (entityFocusProperty.get() != null) {
            nodePreferences.putEntity(DetailNodeKey.ENTITY_FOCUS, entityFocusProperty.get());
        } else {
            nodePreferences.remove(DetailNodeKey.ENTITY_FOCUS);
        }
        saveDetailsPreferences();
    }

    protected abstract void saveDetailsPreferences();

    protected abstract void revertDetailsPreferences();

    enum DetailNodeKey {
        ENTITY_FOCUS
    }
}
