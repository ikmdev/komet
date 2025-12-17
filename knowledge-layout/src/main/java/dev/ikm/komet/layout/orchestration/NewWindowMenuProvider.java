package dev.ikm.komet.layout.orchestration;

import dev.ikm.komet.layout.preferences.KlProfiles;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout_engine.component.window.ComponentVersionsListDetailsAreaWindowFactory;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class NewWindowMenuProvider implements WindowCreateProvider {

    @Override
    public ImmutableList<Action> createWindowActions() {
        Action componentVersionsGridEmbeddedDetailsGridTest = new Action("Component Versions List test",
                event -> {
                    ComponentVersionsListDetailsAreaWindowFactory windowFactory = new ComponentVersionsListDetailsAreaWindowFactory();
                    KlFxWindow klWindow = windowFactory.create(KlProfiles.sharedWindowPreferenceFactory(ComponentVersionsListDetailsAreaWindowFactory.class));
                    klWindow.setTitle("List for Versions: " + DateTimeUtil.timeNowSimple());
                    klWindow.context().subscribeDependentContexts();
                    klWindow.show();
                });
        return Lists.immutable.of(componentVersionsGridEmbeddedDetailsGridTest);
    }
}
