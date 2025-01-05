package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.component.location.SpatialPoint;

/**
 * Represents a field that holds a SpatialPoint value.
 *
 * This interface extends KlField parameterized with a SpatialPoint type.
 */
@ParentConcept(KlField.class)
@RegularName("Spatial Point Field")
public non-sealed interface KlPointSpatialField extends KlField<SpatialPoint> {
}
