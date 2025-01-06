package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.TinkarTerm;

/**
 * Represents a field whose value is some type of entity component.
 *
 * This interface extends the KlField interface, and it is parameterized
 * with an entity type and its corresponding version type.
 *
 * @param <E> The type of the entity.
 * @param <V> The type of the entity version.
 */
@RegularName("Component Field")
@ParentConcept(KlField.class)
public non-sealed interface KlComponentField<E extends Entity<V>, V extends EntityVersion> extends KlField<E> {
    /**
     * TODO: We are inconsistent in our use of <data type>, vs <data type> field...
     * We need to clean up the starter set to manage this difference. We probably should have a set
     * of just the data types, without saying it is a field, because the data types can be used in
     * a variety of contexts, including serialization of non-class content.
     * @return
     */
    @Override
    default ConceptFacade fieldDataType() {
        return TinkarTerm.COMPONENT_FIELD;
    }
}
