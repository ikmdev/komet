package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

/**
 * Represents a field that holds a String value.
 *
 * This interface extends KlField parameterized with a String type.
 */
@ParentConcept(KlField.class)
@RegularName("String Field")
public non-sealed interface KlStringField extends KlField<String> {
}
