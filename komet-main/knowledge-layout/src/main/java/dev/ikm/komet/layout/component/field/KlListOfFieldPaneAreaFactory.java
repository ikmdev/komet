package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.Field;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a factory interface for creating and managing instances of
 * {@code KlListOfFieldPane}, which is a specialized field pane designed to handle
 * fields containing lists of objects in the Knowledge Layout framework.
 * <p>
 * This interface extends {@link KlListFieldPaneAreaFactory}, inheriting its
 * capabilities for dealing with list-based field panes and further specializing
 * in the context of fields where each list element is a {@code Field<DT>}.
 * <p>
 * The {@code KlListOfFieldPaneFactory} interface provides a type-safe contract to
 * enable creation and integration of field panes into user interfaces where
 * list-structured field data is represented and managed.
 *
 * @param <DT> the data type of the list elements, which must extend {@code Field}
 * @param <L>  the list type managed within the field pane, containing elements of type {@code DT}
 * @param <FX> the type of the parent UI element associated with the field pane, extending {@code Region}
 */
public interface KlListOfFieldPaneAreaFactory<DT extends Field, L extends List<DT>, FX extends Region>
        extends KlListFieldPaneAreaFactory<DT, L, FX> {
}
