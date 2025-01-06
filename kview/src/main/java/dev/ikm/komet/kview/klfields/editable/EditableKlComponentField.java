package dev.ikm.komet.kview.klfields.editable;

import dev.ikm.komet.layout.component.version.field.KlComponentField;

/**
 * TODO: If you want to differentiate which implementations are editable,
 * I think you should consider a stand alone generic "EditableField" interface, rather than
 * creating a subtype of every field, one that is read-only, and one that is editable.
 *
 * In coding logic, this may require two clauses, but the approach has looser coupling, and keeps the hierarchies shallower:<br><br>
 * <pre>{@code
if (fieldWidget instanceof KlComponentField && fieldWidget instanceof KlEditable) {
// Do something interesting.
}
 * }</pre>
 * <br><br>Keep Hierarchies Shallow:
 * Aim for a shallow inheritance hierarchy to reduce complexity.
 *
 * <br><br>Consider Design Patterns:
 * Design patterns such as the Strategy pattern or the Decorator pattern can help you
 * achieve flexibility and avoid the need for deep inheritance hierarchies.
 *
 * TODO: Should we add a generic KlEditable<DataType> interface in the knowledge-layout module?
 */
public interface EditableKlComponentField extends KlComponentField {
}
