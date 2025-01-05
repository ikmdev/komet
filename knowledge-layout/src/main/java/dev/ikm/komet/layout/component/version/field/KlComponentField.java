package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
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
@RegularName("Component Field")
@ParentConcept(KlField.class)
public non-sealed interface KlComponentField<E extends Entity<V>, V extends EntityVersion> extends KlField<E> {

}
