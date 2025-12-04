package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

import java.util.Set;

/**
 * Represents a field that holds a set collection as its value.
 *
 * This interface extends KlCollectionField, parameterized with a set-based
 * collection type. It serves as a specialization of KlCollectionField to
 * handle set-specific behavior and operations.
 *
 * @param <S> The type of set held by the field.
 */
@RegularName("Set Field")
@ParentConcept(KlCollectionField.class)
public interface KlSetField<S extends Set<?>> extends KlCollectionField<S> {
}
