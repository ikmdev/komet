package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

/**
 * Represents a field that holds a Float value.
 *
 * This interface extends KlField parameterized with a Float type.
 */
@RegularName("Float Field")
@ParentConcept(KlField.class)
public non-sealed interface KlFloatField extends KlField<Float> {
}
