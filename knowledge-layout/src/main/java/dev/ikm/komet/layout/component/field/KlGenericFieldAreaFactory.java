package dev.ikm.komet.layout.component.field;

import javafx.scene.layout.Region;

/**
 * A factory interface for creating and managing instances of {@link KlGenericFieldArea},
 * which represents a generic field pane in the Knowledge Layout framework.
 * This factory specializes in handling field panes parameterized with {@code Object} as the
 * data type and a JavaFX {@link Region} as the parent UI component type.
 *
 * This interface extends {@link KlFieldPaneArea}, specifying the data type as {@code Object},
 * offering support for creating and managing generic field panes without restricting the type
 * of data associated with those panes.
 *
 * @param <FX> the type of the JavaFX {@link Region} subclass associated with the field pane
 */
public interface KlGenericFieldAreaFactory<FX extends Region> extends KlFieldPaneArea<Object, FX, KlGenericFieldArea<FX>> {

}
