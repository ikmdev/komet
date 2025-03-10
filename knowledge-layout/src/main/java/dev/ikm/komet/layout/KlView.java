package dev.ikm.komet.layout;

import dev.ikm.komet.layout.context.KlContext;
import dev.ikm.komet.layout.context.KlContextProvider;
import dev.ikm.komet.layout.preferences.PropertyWithDefault;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.tinkar.coordinate.Coordinates;
import javafx.scene.layout.BorderPane;

/**
 * KlView is an interface that extends KlWidget specifically to provide a node to manage contextual
 * view coordinates. All descendent KlWidget items will inherit the view coordinate from a parent KlView.
 */
public non-sealed interface KlView extends KlGadget<BorderPane>, KlContextProvider {

    /**
     * Retrieves the associated {@code KlContext} for this view or layout element.
     * A {@code KlContext} represents contextual information including coordinates wrapped
     * into a view coordinate.
     *
     * @return the {@code KlContext} associated with this view.
     */
    KlContext context();

}
