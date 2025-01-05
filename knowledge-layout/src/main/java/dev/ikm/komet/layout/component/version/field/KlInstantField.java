package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

import java.time.Instant;

/**
 * Represents a field that holds an Instant value.
 *
 * This interface extends the KlField interface, parameterized with an Instant type.
 */
@RegularName("Instant Field")
@ParentConcept(KlField.class)

public non-sealed interface KlInstantField extends KlField<Instant> {
}
