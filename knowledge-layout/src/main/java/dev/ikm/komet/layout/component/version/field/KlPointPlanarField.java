package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.component.location.PlanarPoint;

/**
 * Represents a field that holds a PlanarPoint value.
 *
 * This interface extends KlField parameterized with a PlanarPoint type.
 */
@RegularName("Planar Point Field")
@ParentConcept(KlField.class)
public interface KlPointPlanarField extends KlField<PlanarPoint> {
}
