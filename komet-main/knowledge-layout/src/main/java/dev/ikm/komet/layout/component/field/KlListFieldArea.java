package dev.ikm.komet.layout.component.field;

import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a specialized field pane in the Knowledge Layout framework that is designed
 * for managing and interacting with fields of type {@code List<DT>}. This interface extends
 * {@link KlFieldArea}, parameterized with a list data structure, enabling support for a
 * collection of field values.
 * <p>
 * The {@code KlListFieldPane} serves as a base structure to define field panes handling
 * lists of data, providing type-safe operations and integration with the parent UI elements.
 * Specific implementations of this interface may provide additional semantics or behavior
 * for managing various field types within a list structure.
 * <p>
 * This sealed interface has three permitted subtypes:
 *<p> - {@code KlListOfFieldDefinitionPane}: Manages fields defined as a list of definitions.
 *<p> - {@code KlListOfFieldPane}: Operates with fields representing a list of objects.
 *<p> - {@code KlListOfVersionsFieldPane}: Handles fields representing a list of entity versions.
 *
 * @param <L>  the list type managed within the field pane, containing elements of type {@code DT}
 * @param <DT> the data type of the elements in the list managed within the field pane
 * @param <FX> the type of the parent UI element associated with this pane, extending {@link Region}
 */
public sealed interface KlListFieldArea<L extends List<DT>, DT, FX extends Region> extends KlFieldArea<L, FX>
        permits KlListOfFieldDefinitionArea, KlListOfFieldArea, KlListOfVersionsFieldArea {
}
