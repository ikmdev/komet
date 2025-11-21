package dev.ikm.komet.layout;

import dev.ikm.komet.layout.window.KlFxWindow;
import dev.ikm.komet.layout.window.KlJournalWindow;
import dev.ikm.komet.layout.window.KlRenderView;
import dev.ikm.komet.preferences.KometPreferences;

public sealed interface KlTopView<FX>
        extends KlView<FX>
        permits KlFxWindow, KlJournalWindow {

    void setKlRenderView(KlRenderView renderView);

    KlRenderView getKlRenderView();

    static <KL extends KlTopView> KL restore(KometPreferences preferences) {
        return KlView.restore(preferences);
    }
    sealed interface Factory<FX, KL extends KlTopView<FX>> extends KlView.Factory<FX, KL> permits KlFxWindow.Factory, KlJournalWindow.Factory {

    }

}
