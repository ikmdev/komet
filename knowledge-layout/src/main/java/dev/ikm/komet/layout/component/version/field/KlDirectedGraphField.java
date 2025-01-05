package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.graph.DiGraphEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;

/**
 * Represents a field that holds a directed graph entity.
 *
 * This interface extends KlField, and it is parameterized with a directed graph entity type
 * and its corresponding vertex type.
 *
 * @param <DG> The type of the directed graph entity.
 * @param <V> The type of the entity vertex.
 */
@RegularName("Directed Graph Field")
@ParentConcept(KlField.class)
public non-sealed interface KlDirectedGraphField<DG extends DiGraphEntity<V>, V extends EntityVertex> extends KlField<DG> {
}
