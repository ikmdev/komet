package dev.ikm.komet.layout.component.version.field;

import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.SemanticEntityVersion;

/**
 * Represents a field that holds a semantic entity.
 *
 * This interface extends KlField and is parameterized with a semantic entity type
 * and its corresponding version type.
 *
 * @param <S> The type of the semantic entity.
 * @param <V> The type of the semantic entity version.
 */
public interface KlSemanticField<S extends SemanticEntity<V>, V extends SemanticEntityVersion> extends KlField<S> {
}
