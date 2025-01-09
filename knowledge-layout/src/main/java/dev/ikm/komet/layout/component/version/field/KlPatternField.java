package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.common.bind.annotations.axioms.ParentConcept;
import dev.ikm.tinkar.common.bind.annotations.names.RegularName;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.PatternEntityVersion;

/**
 * Represents a field that holds a pattern entity.
 *
 * This interface extends KlField and is parameterized with a pattern entity type
 * and its corresponding version type.
 *
 * @param <P> The type of the pattern entity.
 * @param <V> The type of the pattern entity version.
 */
@RegularName("Pattern Field")
@ParentConcept(KlComponentField.class)
public interface KlPatternField<P extends PatternEntity<V>, V extends PatternEntityVersion> extends KlField<P> {
}
