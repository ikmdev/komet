package dev.ikm.komet.details;

import com.google.auto.service.AutoService;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.activity.ActivityStreams;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;

@AutoService(KometNodeFactory.class)
public class DetailsNodeFactory implements KometNodeFactory {
    protected static final String STYLE_ID = DetailsNode.STYLE_ID;
    protected static final String TITLE = DetailsNode.TITLE;

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {
        DetailsNode.addDefaultNodePreferences(nodePreferences);
    }

    @Override
    public DetailsNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Reconstructor
    public static DetailsNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new DetailsNode(windowView.makeOverridableViewProperties(), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return DetailsNode.class;
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.of(ActivityStreams.SEARCH, ActivityStreams.NAVIGATION, ActivityStreams.REASONER,
                ActivityStreams.UNLINKED, ActivityStreams.BUILDER, ActivityStreams.CORRELATION, ActivityStreams.LIST);
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (defaultActivityStreamChoices().contains(streamKey)) {
            return Lists.immutable.of(ActivityStreamOption.SUBSCRIBE.keyForOption());
        }
        return Lists.immutable.empty();
    }

    @Override
    public String getMenuText() {
        return TITLE;
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }
}