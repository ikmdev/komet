package dev.ikm.komet.layout.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

/**
 * Represents a attribute that holds a String value.
 *
 * This interface extends KlField parameterized with a String type.
 */
@RegularName("String Field")
@ParentConcept(KlField.class)
public interface KlStringField extends KlField<String> {
}
