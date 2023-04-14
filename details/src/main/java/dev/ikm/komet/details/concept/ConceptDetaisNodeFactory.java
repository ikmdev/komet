package dev.ikm.komet.details.concept;

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

/**
 * TODO consider removing and replace with only details node...
 */
//@AutoService(KometNodeFactory.class)
public class ConceptDetaisNodeFactory implements KometNodeFactory {
    protected static final String STYLE_ID = ConceptDetailsNode.STYLE_ID;
    protected static final String TITLE = ConceptDetailsNode.TITLE;

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {
        ConceptDetailsNode.addDefaultNodePreferences(nodePreferences);
    }

    @Override
    public ConceptDetailsNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Reconstructor
    public static ConceptDetailsNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new ConceptDetailsNode(windowView.makeOverridableViewProperties(), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return ConceptDetailsNode.class;
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