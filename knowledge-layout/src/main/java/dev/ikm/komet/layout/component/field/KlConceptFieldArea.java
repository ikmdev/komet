package dev.ikm.komet.layout.component.field;

import dev.ikm.tinkar.entity.ConceptEntity;
import javafx.scene.layout.Region;

/**
 * Represents a pane in the Knowledge Layout framework designed to manage and
 * interact with fields associated with concept entities. This interface extends
 * {@code KlFieldPane}, parameterized with {@code ConceptEntity} and a parent type
 * {@code P}, providing specialized support for concept-related field panes.
 *
 * This interface serves as a specialization of {@code KlFieldPane<E, P>} where
 * {@code E} is constrained to {@code ConceptEntity}. It enables handling and
 * observing field values tied to concept-specific data in layouts.
 *
 * @param <E> the type representing the concept entity managed by this field pane
 * @param <P> the type of the parent node associated with this pane
 */
public non-sealed interface KlConceptFieldArea<E extends ConceptEntity, P extends Region>
        extends KlFieldArea<E, P> {
}