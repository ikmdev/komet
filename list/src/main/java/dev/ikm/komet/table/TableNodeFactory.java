package dev.ikm.komet.table;

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
public class TableNodeFactory implements KometNodeFactory {
    protected static final String STYLE_ID = TableNode.STYLE_ID;
    protected static final String TITLE = TableNode.TITLE;

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Reconstructor
    public static KometNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new TableNode(windowView.makeOverridableViewProperties(), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return TableNode.class;

    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.of(ActivityStreams.LIST);
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        if (ActivityStreams.LIST.equals(streamKey)) {
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