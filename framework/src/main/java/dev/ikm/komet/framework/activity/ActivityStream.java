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
package dev.ikm.komet.framework.activity;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertStreams;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.SaveState;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import dev.ikm.tinkar.common.util.broadcast.SimpleBroadcaster;
import dev.ikm.tinkar.common.util.broadcast.Subscriber;
import dev.ikm.tinkar.terms.EntityFacade;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;

public class ActivityStream implements Broadcaster<ImmutableList<EntityFacade>>, SaveState {
    public static final int MAX_HISTORY_SIZE = 15;
    final String streamIconCssId;

    final Broadcaster<ImmutableList<EntityFacade>> processor;
    final PublicIdStringKey<ActivityStream> activityStreamKey;
    /**
     * Note that last dispatch is different from history. Last dispatch may contain a multi-select, while history is
     * a list of single elements. If a dispatch is multiselect, each of the multi-select items are added individually to
     * history.
     */
    final AtomicReference<ImmutableList<EntityFacade>> lastDispatch = new AtomicReference<>(Lists.immutable.empty());
    final KometPreferences preferences;
    final ObservableList<EntityFacade> history = FXCollections.observableArrayList();

    public ActivityStream(String streamIconCssId, PublicIdStringKey<ActivityStream> activityStreamKey) {
        this.streamIconCssId = streamIconCssId;
        this.activityStreamKey = activityStreamKey;

        this.processor = new SimpleBroadcaster<>();
        ;
        this.preferences = Preferences.get().getConfigurationPreferences().node("/activity-streams/" + activityStreamKey.getString());
        if (preferences.hasKey(PreferenceKey.HISTORY)) {
            List<EntityFacade> savedHistory = preferences.getEntityList(PreferenceKey.HISTORY);
            history.addAll(savedHistory);
        }
        if (preferences.hasKey(PreferenceKey.LAST_DISPATCH)) {
            List<EntityFacade> lastDispatchList = preferences.getEntityList(PreferenceKey.LAST_DISPATCH);
            lastDispatch.set(Lists.immutable.ofAll(lastDispatchList));
        } else {
            lastDispatch.set(Lists.immutable.empty());
        }
        PrimitiveData.getStatesToSave().add(this);
    }

    public PublicIdStringKey<ActivityStream> getActivityStreamKey() {
        return activityStreamKey;
    }

    public String getStreamName() {
        return activityStreamKey.getString();
    }


    @Override
    public void save() {
        try {
            preferences.putComponentList(PreferenceKey.HISTORY, history);
            preferences.putComponentList(PreferenceKey.LAST_DISPATCH, lastDispatch.get().castToList());
            preferences.flush();
        } catch (BackingStoreException e) {
            AlertStreams.getRoot().dispatch(AlertObject.makeError(e));
        }
    }

    @Override
    public void addSubscriberWithWeakReference(Subscriber<ImmutableList<EntityFacade>> subscriber) {
        processor.addSubscriberWithWeakReference(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber<ImmutableList<EntityFacade>> subscriber) {
        processor.removeSubscriber(subscriber);
    }

    public void dispatch(EntityFacade... entities) {
        dispatch(Lists.immutable.of(entities));
    }

    public void dispatch(ImmutableList<EntityFacade> entities) {
        lastDispatch.set(entities);
        processor.dispatch(entities);
        if (Platform.isFxApplicationThread()) {
            updateHistory(entities);
        } else {
            Platform.runLater(() -> updateHistory(entities));
        }
    }

    private void updateHistory(ImmutableList<EntityFacade> entities) {
        for (EntityFacade entity : entities) {
            if (!history.isEmpty()) {
                if (history.get(0).nid() != entity.nid()) {
                    history.add(0, entity);
                }
            } else {
                history.add(0, entity);
            }
        }
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
    }

    public ImmutableList<EntityFacade> lastDispatch() {
        return lastDispatch.get();
    }

    public Optional<EntityFacade> lastDispatchOfIndex(int index) {
        ImmutableList<EntityFacade> lastDispatchList = lastDispatch.get();
        if (index < lastDispatchList.size()) {
            return Optional.of(lastDispatchList.get(index));
        }
        return Optional.empty();
    }

    public Node getStreamIcon() {
        return Icon.makeIcon(streamIconCssId);
    }

    public ObservableList<EntityFacade> getHistory() {
        return history;
    }

    enum PreferenceKey {
        LAST_DISPATCH,
        HISTORY;
    }
}
