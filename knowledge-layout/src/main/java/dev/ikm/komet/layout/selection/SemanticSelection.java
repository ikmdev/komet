package dev.ikm.komet.layout.selection;

import dev.ikm.tinkar.common.id.PublicId;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Represents the selectedAttribute of a semantic component along with its selected versions.
 * This class implements the {@link ComponentSelection} interface for handling semantic versions.
 *
 * @param componentPublicId   the public identifier of the semantic component.
 * @param selectedVersions    an immutable list of selected versions of the semantic component.
 */
public record SemanticSelection(PublicId componentPublicId,
                                ImmutableList<SemanticVersionSelection> selectedVersions)
        implements ComponentSelection<SemanticVersionSelection> {
}
