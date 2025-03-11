package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import javafx.scene.layout.Pane;

/**
 * A non-sealed factory interface for creating instances of {@code KlComponentPane}
 * associated with generic {@code ObservableEntity} types. This interface specializes
 * in constructing and initializing {@code JavaFX Pane}-based UI components
 * tied to observable entities. It extends the {@code KlComponentPaneFactory}
 * with additional flexibility, allowing implementation by any class.
 *
 * The {@code KlGenericComponentPaneFactory} interface is used in scenarios where
 * generic or non-specialized {@code Pane} and {@code ObservableEntity} pairings
 * are required. It provides a means to produce component panes dynamically bound
 * to observable data models.
 *
 * @param <FX> the type of JavaFX {@code Pane} created by this factory
 */
public non-sealed interface KlGenericComponentAreaFactory<FX extends Pane>
        extends KlComponentAreaFactory<FX, KlComponentArea<ObservableEntity, FX>, ObservableEntity> {
}
