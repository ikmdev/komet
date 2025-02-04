package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.IntIdList;

/**
 * Represents a field that holds a list of component entities.
 *
 * This interface extends KlListField, parameterized with a list of component entity types,
 * where the entities themselves are parameterized with their respective version types. It serves
 * as a specialization designed to manage and interact specifically with component entities in
 * list-based collections.
 *
 *
 */
@RegularName("Component List Field")
@ParentConcept(KlListField.class)
public interface KlComponentListField extends KlField<IntIdList> {
}
