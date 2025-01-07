package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.common.id.PublicId;

/**
 *
 */
@RegularName( "Public Id Field")
@ParentConcept( KlField.class)
public interface KlPublicIdField extends KlField<PublicId>{
}
