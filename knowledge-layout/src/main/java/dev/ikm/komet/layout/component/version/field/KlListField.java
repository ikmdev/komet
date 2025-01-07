package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

import java.util.List;

/**
 * Represents a field that holds a list collection as its value.
 *
 * This interface extends KlCollectionField, parameterized with a list-based
 * collection type. It serves as a specialization of KlCollectionField to
 * handle list-specific behavior and operations.
 *
 * @param <L> The type of list held by the field.
 */
@RegularName( "List Field")
@ParentConcept( KlCollectionField.class)
public interface KlListField<L extends List<?>> extends KlCollectionField<L> {
}
