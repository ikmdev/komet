package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.graph.EntityVertex;

/**
 * Represents a field that holds an EntityVertex. This field type might be used when
 * a graph is not held within a single semantic but is distributed across multiple schematics. A
 * graph that might represent a navigation tree of all of SNOMED, or a graph that represents a
 * business process where nodes are individually edited may make use of this Field type.
 *
 * This interface extends the KlField interface, parameterized with an EntityVertex type.
 */
@ParentConcept(KlField.class)
@RegularName("Graph Vertex Field")
public non-sealed interface KlVertexField extends KlField<EntityVertex> {
}
