package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.List;

/**
 * Represents a field whose value is a list of entity components.
 *
 * This interface extends the KlField interface, parameterized with a list of entities
 * and their corresponding versions.
 *
 * @param <E> The type of the entities in the list.
 * @param <V> The type of the entity versions.
 *
 * @TODO should we have separate list and set types, or should they be combined into one component that can be constrained to prohibit addition of duplicates?
 */
@RegularName("Component List")
@ParentConcept(KlField.class)
public non-sealed interface KlComponentListField<E extends Entity<V>, V extends EntityVersion> extends KlField<List<E>> {
}
