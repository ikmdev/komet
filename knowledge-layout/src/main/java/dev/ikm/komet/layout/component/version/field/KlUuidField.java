package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

import java.util.UUID;

/**
 * Represents a field that holds a UUID value.
 *
 * This interface extends the KlField interface, parameterized with a UUID type.
 *
 * Some implementations of this interface may show the UUID string, and others
 * may display different types of identicon.
 */
@RegularName( "UUID Field")
@ParentConcept( KlField.class)
public interface KlUuidField extends KlField<UUID> {
}
