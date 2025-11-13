package dev.ikm.komet.layout.version.field;


import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;

/**
 * A factory interface for creating instances of {@link KlField} that are parameterized
 * with a specific type. This interface defines methods to generate fields dynamically
 * based on observable data and provide metadata about the type of attribute it produces.
 *
 * Implementations of this interface are responsible for providing logic to bind
 * observable fields and views to the fields they create, enabling dynamic updates
 * and context-aware behavior in the produced fields.
 *
 * @param <DT> The data type of value that the attribute will handle.
 * @deprecated use KlFieldPaneFactory
 */
@Deprecated
public interface KlFieldFactory<DT> {

    /**
     * Creates a new instance of {@link KlField} associated with the provided observable data
     * and view context. This method utilizes the {@link ObservableView} and {@link ObservableStamp}
     * to define how the {@link KlField} will behave and display dynamic updates
     * based on the factory's specifications.
     *
     * @param observableField the observable attribute that represents the data to be displayed or managed
     *                        by the created {@link KlField}.
     * @param observableView the observable view that provides context for how the created
     *                       {@link KlField} will be integrated and interacted with.
     * @param editStamp the observable stamp used to handle editing timestamps and changes
     *                  associated with the state of the {@link KlField}.
     * @return a new {@link KlField} instance parameterized with the same type as the provided
     *         {@link ObservableField}.
     */
    KlField<DT> create(ObservableField<DT> observableField, ObservableView observableView, ObservableStamp editStamp);
    default KlField<DT> create(ObservableField.Editable<DT> observableFieldEditable, ObservableView observableView, ObservableStamp editStamp) {
        throw new UnsupportedOperationException("Not implemented in this factory: " + this.getClass());
    }
    /**
     * Retrieves the class type of the attribute interface produced by the factory.
     * This class type extends {@link KlField} and indicates the specific
     * implementation or subinterface of {@link KlField} that the factory is designed to handle.
     *
     * @return A {@link Class} object representing the class type of the attribute
     *         interface extending {@link KlField}.
     */
    Class<? extends KlField<DT>> getFieldInterface();

    /**
     * Retrieves the class type of the specific implementation of {@link KlField}
     * that is produced by the factory.
     *
     * @return A {@link Class} object representing the class type of the implementation
     *         of {@link KlField} associated with this factory.
     */
    Class<? extends KlField<DT>> getFieldImplementation();

    /**
     * Retrieves the name of the associated attribute widget created by this factory.
     *
     * @return A string representing the name of the attribute widget.
     */
    String getName();

    /**
     * Retrieves a description of the attribute widget created by this factory.
     *
     * @return A string representing the description of the attribute or factory.
     */
    String getDescription();
}
