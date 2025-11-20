package dev.ikm.komet.layout.window;

import dev.ikm.komet.layout.KlStateCommands;
import dev.ikm.komet.layout.KlTopView;
import dev.ikm.komet.layout.KlView;
import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;


/**
 * Placeholder for now.
 */
public non-sealed interface KlJournalWindow
        extends KlStateCommands, KlContextProvider, KlTopView<Pane> {
    @Override
    default KlContext context() {
        return KlView.context(fxObject());
    }

    non-sealed interface Factory<KL extends KlTopView<Region>> extends KlTopView.Factory<Region, KL> {

    }

}
