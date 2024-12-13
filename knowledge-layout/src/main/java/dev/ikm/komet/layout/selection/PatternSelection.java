package dev.ikm.komet.layout.selection;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Represents the selectedAttribute of a pattern component along with its selected versions.
 * This class implements the {@link ComponentSelection} interface for handling pattern versions.
 *
 * @param componentPublicId   the public identifier of the pattern component.
 * @param selectedVersions    an immutable list of selected versions of the pattern.
 */
public record PatternSelection(PublicId componentPublicId,
                               ImmutableList<PatternVersionSelection> selectedVersions)
        implements ComponentSelection<PatternVersionSelection> {

}
