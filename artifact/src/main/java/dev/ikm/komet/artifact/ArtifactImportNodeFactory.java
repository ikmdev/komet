package dev.ikm.komet.artifact;

import com.google.auto.service.AutoService;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

@AutoService(KometNodeFactory.class)
public class ArtifactImportNodeFactory implements KometNodeFactory {
    protected static final String STYLE_ID = ArtifactImportNode.STYLE_ID;
    protected static final String TITLE = ArtifactImportNode.TITLE;

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.empty();
    }
    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        return Lists.immutable.empty();
    }
    @Reconstructor
    public static ArtifactImportNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new ArtifactImportNode(windowView.makeOverridableViewProperties(), nodePreferences);
    }
    @Override
    public KometNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Override
    public String getMenuText() {
        return TITLE;
    }

    @Override
    public String getStyleId() {
        return STYLE_ID;
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return ArtifactImportNode.class;
    }
}

