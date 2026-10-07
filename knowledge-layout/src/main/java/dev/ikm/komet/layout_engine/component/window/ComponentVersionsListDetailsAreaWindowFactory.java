package dev.ikm.komet.layout_engine.component.window;

import dev.ikm.komet.layout.area.AreaGridSettings;
import dev.ikm.komet.layout.preferences.KlPreferencesFactory;
import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout_engine.component.area.ChronologyDetailsArea;
import dev.ikm.komet.layout_engine.component.area.LeftToolbarArea;
import dev.ikm.komet.layout_engine.component.area.MenuArea;
import dev.ikm.komet.layout_engine.component.menu.ViewContextMenuButtonArea;
import dev.ikm.komet.layout_engine.layout.SimpleKnowledgeLayout;
import dev.ikm.komet.preferences.KometPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentVersionsListDetailsAreaWindowFactory implements KlFxWindow.Factory {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentVersionsListDetailsAreaWindowFactory.class);
    @Override
    public KlFxWindow restore(KometPreferences preferences) {
        return FxWindow.restore(preferences);
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

}