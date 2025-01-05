package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.PublicId;

/**
 *
 */
@ParentConcept(KlField.class)
@RegularName("Public Id Field")
public non-sealed interface KlPublicIdField extends KlField<PublicId>{
}
