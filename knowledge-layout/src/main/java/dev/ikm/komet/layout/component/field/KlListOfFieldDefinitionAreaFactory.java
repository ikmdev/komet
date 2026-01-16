package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.component.FieldDefinition;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Represents a factory interface for creating instances of {@link KlListFieldArea},
 * specifically designed for handling fields defined as a list of {@link FieldDefinition}
 * elements. This interface supports the Knowledge Layout framework's specialized pane
 * creation and management for scenarios where a collection of field definitions needs
 * to be represented and interacted with in the form of a list.
 * <p>
 * This interface extends {@link KlListFieldPaneAreaFactory}, inheriting its type-safe
 * operations and capabilities for managing list-based field panes. Implementations
 * of this factory interface may provide additional functionality or customization
 * for dealing with specific types of field definitions within a list structure.
 *
 * @param <DT> the data type representing the field definition elements in the list
 * @param <L>  the type of the list containing elements of type {@code DT}
 * @param <FX> the type of the parent UI element associated with the pane, extending {@link Region}
 */
public interface KlListOfFieldDefinitionAreaFactory<DT extends FieldDefinition, L extends List<DT>, FX extends Region>
        extends KlListFieldPaneAreaFactory<DT, L, FX> {
}
