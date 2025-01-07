package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.List;

/**
 * Represents a field that holds a list of component entities.
 *
 * This interface extends KlListField, parameterized with a list of component entity types,
 * where the entities themselves are parameterized with their respective version types. It serves
 * as a specialization designed to manage and interact specifically with component entities in
 * list-based collections.
 *
 * @param <E> The type of the component entity in the list.
 * @param <V> The type of the version associated with the component entity.
 */
@RegularName("Component List Field")
@ParentConcept(KlListField.class)
public interface KlComponentListField<E extends Entity<V>, V extends EntityVersion> extends KlListField<List<E>> {
}
