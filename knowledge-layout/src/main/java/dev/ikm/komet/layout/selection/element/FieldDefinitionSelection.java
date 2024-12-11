package dev.ikm.komet.layout.selection.element;

import dev.ikm.komet.layout.selection.PatternVersionSelection;
import dev.ikm.komet.layout.selection.SelectableIndexedElement;
import dev.ikm.tinkar.terms.ConceptFacade;

/**
 * The FieldDefinitionSelection record represents a selectedAttribute definition for a specific field
 * in the context of a pattern version. It includes an index indicating the position of the field
 * and a set of attributes describing the field's characteristics.
 *
 * @param index     the index of the field in the pattern version.
 * @param selectedAttribute an immutable set of selected field definition attributes.
 */
public record FieldDefinitionSelection(int index, PatternVersionSelection.FieldAttribute selectedAttribute)
        implements SelectableIndexedElement {
    public ConceptFacade conceptForEnum() {
        return this.selectedAttribute.conceptForEnum();
    }
}
