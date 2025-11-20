package dev.ikm.komet.layout.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

/**
 * Represents a attribute that holds an Integer value.
 *
 * This interface extends KlField parameterized with an Integer type.
 */
@RegularName("Integer Field")
@ParentConcept(KlField.class)
public interface KlIntegerField extends KlField<Integer> {
}
