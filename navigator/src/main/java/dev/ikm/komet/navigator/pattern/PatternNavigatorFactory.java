package dev.ikm.komet.navigator.pattern;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(KometNodeFactory.class)
public class PatternNavigatorFactory
        implements KometNodeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PatternNavigatorFactory.class);
    protected static final String STYLE_ID = PatternNavigatorNode.STYLE_ID;
    protected static final String TITLE = PatternNavigatorNode.TITLE;

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public PatternNavigatorNode create(ObservableViewNoOverride windowViewReference, KometPreferences nodePreferences) {
        return reconstructor(windowViewReference, nodePreferences);
    }

    @Reconstructor
    public static PatternNavigatorNode reconstructor(ObservableViewNoOverride windowViewReference, KometPreferences nodePreferences) {
        return new PatternNavigatorNode(windowViewReference.makeOverridableViewProperties(), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return PatternNavigatorNode.class;
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.of(ActivityStreams.NAVIGATION);
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (defaultActivityStreamChoices().contains(streamKey)) {
            return Lists.immutable.of(ActivityStreamOption.PUBLISH.keyForOption());
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
