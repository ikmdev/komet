package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;

/**
 * Represents a field whose value is some type of entity component.
 *
 * This interface extends the KlField interface, and it is parameterized
 * with an entity type and its corresponding version type.
 *
 * @param <E> The type of the entity.
 * @param <V> The type of the entity version.
 */
public interface KlComponentField<E extends Entity<V>, V extends EntityVersion> extends KlField<E> {

}
