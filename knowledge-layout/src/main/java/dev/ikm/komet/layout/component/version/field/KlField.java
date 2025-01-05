package dev.ikm.komet.layout.component.version.field;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.KlWidget;

/**
 * Represents an observable field in the Komet framework.
 *
 * This interface is parameterized with the type of the value. It extends the
 * KlWidget interface, providing a method to access the underlying field.
 *
 * @param <T> The type of the field's value.
 */
public interface KlField<T> extends KlWidget {
    ObservableField<T> field();
}
