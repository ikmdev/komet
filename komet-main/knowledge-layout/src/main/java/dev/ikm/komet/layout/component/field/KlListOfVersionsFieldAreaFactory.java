package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.EntityVersion;
import javafx.scene.layout.Region;

import java.util.List;

/**
 * Defines a factory interface for creating and managing instances of
 * {@code KlListOfVersionsFieldPane}, which is a specialized type of list field pane
 * in the Knowledge Layout framework designed for handling lists of {@code EntityVersion} objects.
 * <p>
 * This interface builds upon the {@code KlListFieldPaneFactory}, providing additional
 * type constraints specific to managing versions of entities. It facilitates the creation
 * and setup of UI components that represent and manipulate entities with versioning,
 * enabling integration with parent UI elements.
 *
 * @param <DT> the data type representing a single version in the list, extending {@code EntityVersion}
 * @param <L>  the list type containing elements of type {@code DT}, extending {@code List<DT>}
 * @param <FX> the type of the parent UI element associated with this pane, extending {@code Region}
 */
public interface KlListOfVersionsFieldAreaFactory<DT extends EntityVersion, L extends List<DT>, FX extends Region>
        extends KlListFieldPaneAreaFactory<DT, L, FX> {
}
