package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableEntity;
import javafx.scene.layout.Pane;

/**
 * Represents a generic component pane that supports observable entities and is associated
 * with a JavaFX {@code Pane}.
 *
 * KlGenericComponentPane is a specialized non-sealed interface that extends {@code KlComponentPane},
 * providing the ability to handle generic observable entities. It serves as a flexible extension
 * for managing and presenting various types of observable entities within JavaFX pane structures.
 *
 * The `P` generic parameter defines the type of JavaFX {@code Pane} used for visualization.
 *
 * @param <FX> the type of JavaFX {@code Pane} associated with this component pane.
 * @see KlComponentArea
 * @see Pane
 */
public non-sealed interface KlGenericComponentArea<FX extends Pane> extends KlComponentArea<ObservableEntity, FX> {
}
