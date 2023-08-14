/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.framework;

import dev.ikm.komet.framework.activity.ActivityStreamOption;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import dev.ikm.komet.framework.controls.EntityLabelWithDragAndDrop;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.flow.FlowSubscriber;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;

import static dev.ikm.komet.framework.DetailNodeAbstract.DetailNodeKey.REQUEST_FOCUS_ON_ACTIVITY;

public abstract class DetailNodeAbstract extends ExplorationNodeAbstract {

    protected final SimpleObjectProperty<EntityFacade> entityFocusProperty = new SimpleObjectProperty<>();
    protected final FlowSubscriber<Integer> invalidationSubscriber;

    {
        entityFocusProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                titleProperty.set(viewProperties.calculator().getPreferredDescriptionTextWithFallbackOrNid(newValue));
                toolTipTextProperty.set(viewProperties.calculator().getFullyQualifiedDescriptionTextWithFallbackOrNid(newValue));
                if (ActivityStreamOption.PUBLISH.keyForOption().equals(super.optionForActivityStreamKeyProperty.get()) ||
                        ActivityStreamOption.SYNCHRONIZE.keyForOption().equals(super.optionForActivityStreamKeyProperty.get())) {
                    getActivityStream().dispatch(newValue);
                }
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
        nodePreferences.putBoolean(REQUEST_FOCUS_ON_ACTIVITY, false);
        saveDetailsPreferences();
    }

    protected abstract void saveDetailsPreferences();

    protected abstract void revertDetailsPreferences();

    enum DetailNodeKey {
        ENTITY_FOCUS,
        REQUEST_FOCUS_ON_ACTIVITY
    }
}
