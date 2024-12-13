package dev.ikm.komet.layout.selection;

import dev.ikm.komet.layout.selection.element.FieldDefinitionSelection;
import dev.ikm.komet.layout.selection.element.FieldValueSelection;

public sealed interface SelectableIndexedElement extends SelectableElement
    permits FieldDefinitionSelection, FieldValueSelection {
    int index();
}
