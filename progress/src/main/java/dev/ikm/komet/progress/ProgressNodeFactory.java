package dev.ikm.komet.progress;

import com.google.auto.service.AutoService;
import javafx.scene.control.Label;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import dev.ikm.komet.framework.KometNode;
import dev.ikm.komet.framework.KometNodeFactory;
import dev.ikm.komet.framework.activity.ActivityStream;
import dev.ikm.komet.framework.activity.ActivityStreamOption;
import dev.ikm.komet.framework.graphics.Icon;
import dev.ikm.komet.framework.preferences.Reconstructor;
import dev.ikm.komet.framework.view.ObservableViewNoOverride;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.common.id.PublicIdStringKey;

@AutoService(KometNodeFactory.class)
public class ProgressNodeFactory implements KometNodeFactory {

    @Override
    public void addDefaultNodePreferences(KometPreferences nodePreferences) {

    }

    @Override
    public ProgressNode create(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return reconstructor(windowView, nodePreferences);
    }

    @Reconstructor
    public static ProgressNode reconstructor(ObservableViewNoOverride windowView, KometPreferences nodePreferences) {
        return new ProgressNode(windowView.makeOverridableViewProperties(), nodePreferences);
    }

    @Override
    public Class<? extends KometNode> kometNodeClass() {
        return ProgressNode.class;
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStream>> defaultActivityStreamChoices() {
        return Lists.immutable.empty();
    }

    @Override
    public ImmutableList<PublicIdStringKey<ActivityStreamOption>> defaultOptionsForActivityStream(PublicIdStringKey<ActivityStream> streamKey) {
        return Lists.immutable.empty();
    }

    @Override
    public String getMenuText() {
        return "Activity";
    }

    @Override
    public Label getMenuIconGraphic() {
        return Icon.makeIcon("activity-node");
    }

    @Override
    public String getStyleId() {
        return null;
    }

    public KometNode create() {
        return new ProgressNode();
    }


}
