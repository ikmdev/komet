package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.Field;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a specialized field pane in the Knowledge Layout framework that operates
 * with fields containing a list of objects. This interface is a subtype of {@code KlListFieldPane},
 * parameterized to manage and interact with a list structure ({@code List<DT>}) and a parent UI element.
 * <p>
 * The {@code KlListOfFieldPane} interface defines the contract for field panes that handle
 * lists of individual fields, making it possible to manage collections of fields represented as
 * objects conforming to the {@code Field<DT>} interface. The design enables type-safe interactions
 * with fields and their corresponding data values, along with their integration into parent UI components.
 *
 * @param <L>  the list type managed within the field pane, containing elements of type {@code DT}
 * @param <DT> the data type of the elements in the list managed within the field pane
 *             and a subtype of {@code Field<DT>}
 * @param <FX> the type of the parent UI element associated with this pane, extending {@code Region}
 */
public non-sealed interface KlListOfFieldArea<L extends List<DT>, DT extends Field<DT>, FX extends Region>
        extends KlListFieldArea<L, DT, FX> {
}
