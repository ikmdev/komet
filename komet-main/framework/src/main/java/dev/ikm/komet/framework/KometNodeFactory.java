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

import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.alert.AlertReportingService;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;
import javafx.scene.control.Label;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static dev.ikm.tinkar.common.alert.AlertCategory.TAXONOMY;
import static dev.ikm.tinkar.common.alert.AlertType.INFORMATION;

public interface KometNodeFactory {

    String KOMET_NODES = "/komet-nodes/";
    String UNSUPPORTED_OPERATION = "Unsupported Operation";
    String THE_CURRENT_OPERATION_IS_NOT_SUPPORTED = "The current operation is not supported. ";
    default KometNode create(ObservableViewNoOverride windowView,
                             PublicIdStringKey<ActivityStream> activityStreamKey,
                             PublicIdStringKey<ActivityStreamOption> activityStreamOption,
                             PublicIdStringKey<Broadcaster<AlertObject>> parentAlertStreamKey,
                             boolean displayOnJournalView,
                             UUID journalTopic
    ) {
        KometPreferences nodePreferences = KometPreferencesImpl.getConfigurationRootPreferences().node(newPreferenceNodeName());
        // Add activity stream key
        if (activityStreamKey != null) {
            nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, activityStreamKey);
        }
        if (activityStreamOption != null) {
            nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_OPTION_KEY, activityStreamOption);
        }

        // Add the journal window's event topic
        if (displayOnJournalView && journalTopic != null) {
            nodePreferences.putUuid(KometNode.PreferenceKey.CURRENT_JOURNAL_WINDOW_TOPIC, journalTopic);
        }

        try{
            addDefaultNodePreferences(nodePreferences);
            // add parent alertStream key
            nodePreferences.putObject(KometNode.PreferenceKey.PARENT_ALERT_STREAM_KEY, parentAlertStreamKey);
            return create(windowView, nodePreferences, displayOnJournalView);
        }catch(UnsupportedOperationException e){
            AlertObject alertObject = new AlertObject(UNSUPPORTED_OPERATION, e.getMessage(),
                    INFORMATION, TAXONOMY);
            ServiceLoader<AlertReportingService> loader = PluggableService.load(AlertReportingService.class);
            StreamSupport.stream(loader.spliterator(), false).forEach(alertReportingService -> alertReportingService.onNext(alertObject));
        }
        return null;
    }
    default KometNode create(ObservableViewNoOverride windowView,
                             PublicIdStringKey<ActivityStream> activityStreamKey,
                             PublicIdStringKey<ActivityStreamOption> activityStreamOption,
                             PublicIdStringKey<Broadcaster<AlertObject>> parentAlertStreamKey,
                             boolean displayOnJournalView
    ) {
        return create(windowView, activityStreamKey, activityStreamOption, parentAlertStreamKey, displayOnJournalView, null);
    }
    default KometNode create(ObservableViewNoOverride windowView,
                             PublicIdStringKey<ActivityStream> activityStreamKey,
                             PublicIdStringKey<ActivityStreamOption> activityStreamOption,
                             PublicIdStringKey<Broadcaster<AlertObject>> parentAlertStreamKey) {

        return create(windowView, activityStreamKey, activityStreamOption, parentAlertStreamKey, false, null);
    }

    default String newPreferenceNodeName() {
        return KOMET_NODES + kometNodeClass().getSimpleName() + "_" + UUID.randomUUID();
    }

    void addDefaultNodePreferences(KometPreferences nodePreferences);

    KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences);

    default KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences, boolean displayOnJournalView){
        return create(windowView, nodePreferences);
    }

    Class<? extends KometNode> kometNodeClass();

    ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices();

    ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey);

    String getMenuText();

    default Label getMenuIconGraphic() {
        return Icon.makeIcon(getStyleId());
    }

    String getStyleId();

    /**
     * Getting available service providers using <code>dev.ikm.tinkar.common.service.PluggableService</code> utility class.
     * @return Iterable list of KometNodeFactory classes
     */
    static ServiceLoader<KometNodeFactory> getKometNodeFactories() {
        return PluggableService.load(KometNodeFactory.class);
    }
}
