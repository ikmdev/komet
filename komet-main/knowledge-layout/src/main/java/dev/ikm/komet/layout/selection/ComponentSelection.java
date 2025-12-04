package dev.ikm.komet.layout.selection;

import dev.ikm.tinkar.common.id.PublicId;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public sealed interface ComponentSelection<V extends VersionSelection> permits ConceptSelection, PatternSelection, SemanticSelection {
    PublicId componentPublicId();
    ImmutableList<V> selectedVersions();

    default ImmutableList<SelectableElement> selectedDataElements() {
        MutableList<SelectableElement> selectedFields = Lists.mutable.empty();
        selectedVersions().forEach(versionSelection -> {
            selectedFields.addAll(versionSelection.selectedDataElements().toList());
        });
        return selectedFields.toImmutable();
    }
}
