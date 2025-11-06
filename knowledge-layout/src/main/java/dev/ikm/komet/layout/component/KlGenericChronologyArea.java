package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import dev.ikm.komet.framework.observable.ObservableVersion;
import javafx.scene.layout.Pane;

/**
 * Represents a generic, non-sealed component area in a JavaFX application that manages observable entities
 * and their corresponding versions within a specific JavaFX {@code Pane}.
 *
 * This interface extends the functionality of the {@code KlComponentArea} interface by introducing a more
 * generic approach to handling component areas and observable entities. It allows implementations to define
 * custom logic for interacting with a generic pane type and its associated entities.
 *
 * The type parameter {@code FX} ensures that implementations can work with a specified type of JavaFX {@code Pane}
 * while maintaining compatibility with observable entities and versions.
 *
 * @param <FX> the type of JavaFX {@code Pane} associated with this component area
 * @see KlChronologyArea
 */
public non-sealed interface KlGenericChronologyArea<FX extends Pane, OE extends ObservableEntity<ObservableVersion<OE, ?>>>
        extends KlChronologyArea<OE, ObservableVersion<OE, ?>, FX> {

    non-sealed interface Factory<FX extends Pane, OE extends ObservableEntity<ObservableVersion<OE, ?>>, KL extends KlGenericChronologyArea<FX, OE>>
            extends KlChronologyArea.Factory<FX, OE, ObservableVersion<OE, ?>, KL> {
    }
 }
