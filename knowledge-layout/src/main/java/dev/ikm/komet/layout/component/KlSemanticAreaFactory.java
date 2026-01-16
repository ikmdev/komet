package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableSemantic;
import javafx.scene.layout.Pane;

/**
 * A non-sealed factory interface for creating instances of {@code KlSemanticPane}.
 * This factory is designed to produce JavaFX panes specifically associated with
 * semantic-related observable entities of type {@code ObservableSemantic}.
 *
 * The {@code KlSemanticPaneFactory} extends the {@code KlComponentPaneFactory} interface,
 * providing specialized functionality for creating and managing panes that represent
 * semantic components in a user interface.
 *
 * Implementations of this interface can ensure the proper initialization and integration
 * of semantic components and their associated observable data.
 *
 * @param <FX> the type of JavaFX {@code Pane} created by this factory
 * @see KlComponentAreaFactory
 * @see KlSemanticArea
 * @see ObservableSemantic
 */
public non-sealed interface KlSemanticAreaFactory<FX extends Pane>
        extends KlComponentAreaFactory<FX, KlSemanticArea<FX>, ObservableSemantic> {
}
