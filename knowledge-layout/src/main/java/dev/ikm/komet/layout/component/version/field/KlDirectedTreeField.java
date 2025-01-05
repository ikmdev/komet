package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;

/**
 * Represents a field whose value is a directed tree entity.
 *
 * This interface extends the KlField interface, parameterized with a directed tree entity type.
 *
 * @param <DT> The type of the directed tree entity.
 */
@RegularName("Directed Tree Field")
@ParentConcept(KlDirectedGraphField.class)
public non-sealed interface KlDirectedTreeField<DT extends DiTreeEntity> extends KlField<DT> {
}
