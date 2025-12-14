package dev.ikm.komet.layout_engine.component.window;

import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.window.KlFrameFactory;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout.window.KlFxWindowFactory;
import dev.ikm.komet.layout_engine.component.area.ChronologyDetailsArea;
import dev.ikm.komet.layout_engine.component.area.LeftToolbarArea;
import dev.ikm.komet.layout_engine.component.area.MenuArea;
import dev.ikm.komet.layout_engine.component.menu.ViewContextMenuButtonArea;
import dev.ikm.komet.layout_engine.layout.SimpleKnowledgeLayout;
import dev.ikm.komet.preferences.KometPreferences;
import org.controlsfx.control.action.Action;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentVersionsListDetailsAreaWindowFactory implements KlFxWindowFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentVersionsListDetailsAreaWindowFactory.class);
    @Override
    public KlFxWindow restore(KometPreferences preferences) {
        return FxWindow.restore(preferences);
    }

    @Override
    public ImmutableList<Action> createRestoreWindowActions() {
        return Lists.immutable.empty();
    }

    @Override
    public KlFxWindow create(KlPreferencesFactory preferencesFactory) {
        LOG.info("Creating window with ChronologyDetailsArea embedded within a ViewContextMenuButtonArea.");
        FxWindow simpleWindow = FxWindow.factory().create(preferencesFactory);

        RenderView renderView = new RenderView.Factory().createAndAddToParent(simpleWindow);

        MenuArea menuArea = MenuArea.factory().createAndAddToParent(renderView);

        LeftToolbarArea leftToolbarArea = LeftToolbarArea.factory().createAndAddToParent(menuArea);

        ViewContextMenuButtonArea viewContextMenuButtonArea = ViewContextMenuButtonArea.factory()
                .createAndAddToParent(leftToolbarArea);

        AreaGridSettings componentVersionsSettings = AreaGridSettings.DEFAULT.with(ChronologyDetailsArea.Factory.class)
                .withLayoutKeyForArea(renderView.getMasterLayout().rootLayoutKey());

        ChronologyDetailsArea componentVersionsArea =
                ChronologyDetailsArea.factory().createAndAddToParent(componentVersionsSettings, viewContextMenuButtonArea);

        leftToolbarArea.setMasterLayout(new SimpleKnowledgeLayout(componentVersionsArea));


        return simpleWindow;
    }

    @Override
    public ImmutableList<Action> createNewWindowActions(KlPreferencesFactory preferencesFactory, KlFrameFactory... frameFactories) {
        MutableList<Action> actions = Lists.mutable.empty();
        for (KlFrameFactory windowPaneFactory : frameFactories) {
            actions.add(new Action("New " + windowPaneFactory.name(), event -> {
                KlFxWindow window = create(preferencesFactory);
                window.show();
            }));
        }
        return actions.toImmutable();
    }
}