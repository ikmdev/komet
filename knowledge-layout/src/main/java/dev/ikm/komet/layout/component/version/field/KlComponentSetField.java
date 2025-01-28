package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.Set;

/**
 * Represents a field that holds a set of component entities.
 *
 * This interface is a specialization of KlSetField, parameterized with
 * a set of entities and their corresponding versions. It is designed to
 * manage and interact with a collection of versioned component entities
 * in the context of knowledge layout.
 *
 * @TODO should we have separate list and set types, or should they be combined into one component that can be constrained to prohibit addition of duplicates?
 *
 */
@RegularName("Component Set Field")
public interface KlComponentSetField extends KlField<Set<EntityProxy>> {
}
