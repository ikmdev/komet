package dev.ikm.komet.layout.selection;

import dev.ikm.komet.layout.selection.element.StampElementSelection;
import dev.ikm.tinkar.common.id.PublicId;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Represents the selectedAttribute of a specific version of a concept identified by a stamp.
 * This record includes a PublicId for the version's stamp and an immutable list of selected stamp fields.
 * It implements the VersionSelection interface, ensuring compliance with version selectedAttribute criteria.
 *
 * @param stampPublicId the public identifier of the version's stamp.
 * @param selectedStampElements an immutable list of fields selected for the version's stamp.
 */
public record ConceptVersionSelection(PublicId componentPublicId,
                                      PublicId stampPublicId,
                                      ImmutableList<StampElement> selectedStampElements)
        implements VersionSelection {

    @Override
    public ImmutableList<SelectableElement> dataElements() {
        return getElements(Lists.immutable.of(StampElement.values()));
    }

    @Override
    public ImmutableList<SelectableElement> selectedDataElements() {
        return getElements(selectedStampElements);
    }

    private ImmutableList<SelectableElement> getElements(ImmutableList<StampElement> selectedStampElements) {
        MutableList<SelectableElement> selectedDataElements = Lists.mutable.of();
        selectedStampElements.forEach(stampElement ->
                selectedDataElements.add(new StampElementSelection(stampElement)));
        return selectedDataElements.toImmutable();
    }
}
