package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

/**
 * Represents a field that holds a Boolean value.
 *
 * This interface extends KlField parameterized with an Boolean type.
 */
@RegularName( "Boolean Field")
@ParentConcept( KlField.class)
public interface KlBooleanField extends KlField<Boolean> {
}
