package dev.ikm.komet.layout.component.field;

import javafx.scene.layout.Region;

/**
 * A factory interface for creating instances of {@link KlBooleanFieldArea}. This interface
 * specializes {@link KlFieldPaneArea} for working with boolean data types, enabling the
 * generation and management of {@code KlBooleanFieldPane} implementations to facilitate
 * layout and interaction with boolean fields in the Knowledge Layout framework.
 *
 * @param <FX> The JavaFX {@link Region} subclass associated with the boolean field pane.
 */
public interface KlBooleanFieldAreaFactory<FX extends Region> extends KlFieldPaneArea<Boolean, FX, KlBooleanFieldArea<FX>> {

}
