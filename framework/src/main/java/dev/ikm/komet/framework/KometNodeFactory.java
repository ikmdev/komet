package dev.ikm.komet.framework;

import javafx.scene.control.Label;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.KometPreferencesImpl;
import dev.ikm.tinkar.common.alert.AlertObject;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.common.util.broadcast.Broadcaster;

import java.util.UUID;

public interface KometNodeFactory {

    String KOMET_NODES = "/komet-nodes/";

    default KometNode create(ObservableViewNoOverride windowView,
                             PublicIdStringKey<ActivityStream> activityStreamKey,
                             PublicIdStringKey<ActivityStreamOption> activityStreamOption,
                             PublicIdStringKey<Broadcaster<AlertObject>> parentAlertStreamKey) {
        KometPreferences nodePreferences = KometPreferencesImpl.getConfigurationRootPreferences().node(newPreferenceNodeName());
        // Add activity stream key
        if (activityStreamKey != null) {
            nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_KEY, activityStreamKey);
        }
        if (activityStreamOption != null) {
            nodePreferences.putObject(KometNode.PreferenceKey.ACTIVITY_STREAM_OPTION_KEY, activityStreamOption);
        }
        // add parent alertStream key
        nodePreferences.putObject(KometNode.PreferenceKey.PARENT_ALERT_STREAM_KEY, parentAlertStreamKey);
        addDefaultNodePreferences(nodePreferences);
        return create(windowView, nodePreferences);
    }

    default String newPreferenceNodeName() {
        return KOMET_NODES + kometNodeClass().getSimpleName() + "_" + UUID.randomUUID();
    }

    void addDefaultNodePreferences(KometPreferences nodePreferences);

    KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences);

    Class<? extends KometNode> kometNodeClass();

    ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices();

    ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey);

    String getMenuText();

    default Label getMenuIconGraphic() {
        return Icon.makeIcon(getStyleId());
    }

    String getStyleId();

}
