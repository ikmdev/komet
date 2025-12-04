package dev.ikm.komet.layout.selection;

import dev.ikm.tinkar.common.id.PublicId;
import org.eclipse.collections.api.list.ImmutableList;


/**
 * Represents the selectedAttribute of a concept component along with its selected versions.
 * This class implements the {@link ComponentSelection} interface for handling concept versions.
 *
 * @param componentPublicId   the public identifier of the concept component.
 * @param selectedVersions    an immutable list of selected versions of the concept.
 */
public record ConceptSelection(PublicId componentPublicId,
                               ImmutableList<ConceptVersionSelection> selectedVersions)
        implements ComponentSelection<ConceptVersionSelection> {
}
