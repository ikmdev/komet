package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.Entity;
import javafx.scene.layout.Region;

/**
 * Defines a pane for managing field components in the Knowledge Layout framework.
 * This non-sealed interface builds upon the functionality provided by {@code KlFieldPane},
 * specializing it for fields associated with components and entities.
 *
 * The {@code KlComponentFieldPane} supports interaction with specific entity field types
 * and their associated UI representation. It uses generics to ensure type safety for
 * both the entity type and the parent UI node type.
 *
 * @param <E>  the type of entity managed within this field pane, extending {@code Entity}
 * @param <FX> the type of parent UI node associated with this field pane, extending {@code Parent}
 */
public non-sealed interface KlComponentFieldArea<E extends Entity, FX extends Region>
        extends KlFieldArea<E, FX> {
}
