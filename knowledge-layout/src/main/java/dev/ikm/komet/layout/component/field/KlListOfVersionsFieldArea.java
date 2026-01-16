package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.EntityVersion;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a specialized field pane in the Knowledge Layout framework designed
 * to manage and interact with fields containing a list of entity versions. This
 * interface extends {@code KlListFieldPane}, parameterized with {@code List<DT>},
 * where {@code DT} is a subtype of {@code EntityVersion}.
 * <p>
 * The {@code KlListOfVersionsFieldPane} interface defines the contract for field
 * panes that handle collections of entity versions, offering support for type-safe
 * operations and integration with the parent UI components. This interface enables
 * the manipulation of lists containing versions of entities, facilitating their
 * display, management, and observation within the user interface.
 *
 * @param <L>  the type of the list managed within the field pane, containing elements of type {@code DT}
 * @param <DT> the data type of the elements in the list managed within the field pane, extending {@code EntityVersion}
 * @param <FX> the type of the parent UI element associated with this pane, extending {@code Region}
 */
public non-sealed interface KlListOfVersionsFieldArea<L extends List<DT>,
        DT extends EntityVersion,
        FX extends Region>
        extends KlListFieldArea<L, DT, FX> {
}
