package dev.ikm.komet.layout.component.version.field;


import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;

import java.util.Collection;

/**
 * Represents a field that holds a collection of values.
 *
 * This interface extends KlField, parameterized with a generic collection type.
 * It serves as a base for fields that handle specific types of collections, such
 * as lists, sets, or other collection implementations.
 *
 * @param <C> The type of the collection held by the field.
 */
@RegularName( "Collection Field")
@ParentConcept(KlField.class)
public interface KlCollectionField<C extends Collection<?>> extends KlField<C> {

}
