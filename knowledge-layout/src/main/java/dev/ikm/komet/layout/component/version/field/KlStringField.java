package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * Represents a field that holds a String value.
 *
 * This interface extends KlField parameterized with a String type.
 */
@ParentConcept(KlField.class)
@RegularName("String Field")
public non-sealed interface KlStringField extends KlField<String> {
    /**
     * Provides the data type of the field.
     *
     * @return A {@link ConceptFacade} representing the data type of the field,
     *         specifically {@code TinkarTerm.STRING_FIELD} for a string field.
     */
    @Override
    default ConceptFacade fieldDataType() {
        return TinkarTerm.STRING;
    }
}
