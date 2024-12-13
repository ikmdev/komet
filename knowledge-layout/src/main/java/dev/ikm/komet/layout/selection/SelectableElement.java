package dev.ikm.komet.layout.selection;

import dev.ikm.komet.layout.selection.element.PatternAttributeSelection;
import dev.ikm.komet.layout.selection.element.StampElementSelection;
import dev.ikm.tinkar.common.id.PublicIdStringKey;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;
import org.eclipse.collections.api.list.ImmutableList;

public sealed interface SelectableElement
    permits SelectableIndexedElement, PatternAttributeSelection, StampElementSelection {

    ConceptFacade conceptForEnum();

}
