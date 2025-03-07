package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.component.FieldDefinition;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a specialized field pane in the Knowledge Layout framework for managing
 * and interacting with fields that are defined as a list of {@link FieldDefinition}.
 * This interface extends {@link KlListFieldArea}, parameterized to support a list of
 * field definitions, enabling type-safe operations and integration with parent UI elements.
 *<p>
 * The {@code KlListOfFieldDefinitionPane} is designed to provide functionality required
 * for handling structured field definitions within a list, supporting the definition,
 * observation, and manipulation of these fields in the context of a user interface.
 *
 * @param <L>  the list type managed within the field pane, containing elements of type {@code DT}
 * @param <DT> the data type of the field definitions managed within the list; must extend {@link FieldDefinition}
 * @param <FX> the type of the parent UI element associated with this pane; must extend {@link Region}
 */
public non-sealed interface KlListOfFieldDefinitionArea<L extends List<DT>, DT extends FieldDefinition, FX extends Region>
        extends KlListFieldArea<L, DT, FX> {
}
