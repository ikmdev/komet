package dev.ikm.komet.layout.component.version.field;


import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;

/**
 * A factory interface for creating instances of {@link KlField} that are parameterized
 * with a specific type. This interface defines methods to generate fields dynamically
 * based on observable data and provide metadata about the type of field it produces.
 *
 * Implementations of this interface are responsible for providing logic to bind
 * observable fields and views to the fields they create, enabling dynamic updates
 * and context-aware behavior in the produced fields.
 *
 * @param <DT> The data type of value that the field will handle.
 * @deprecated use KlFieldPaneFactory
 */
@Deprecated
public interface KlFieldFactory<DT> {

    /**
     * Creates a new instance of {@link KlField} associated with the provided observable data
     * and view context. The method uses the {@link ObservableView} to determine how to display the
     * field (compute preferred or fully qualified names for components, and similar),
     * enabling dynamic updates and integrations according to the factory's specifications.
     *
     * @param observableField the observable field that holds the data to be represented by the created {@link KlField}.
     * @param observableView the observable view that defines the context in which the created {@link KlField} operates.
     * @param editable flag to determine if the UI control is editable
     * @return a new {@link KlField} instance parameterized with the same type as the provided {@link ObservableField}.
     */
    KlField<DT> create(ObservableField<DT> observableField, ObservableView observableView, boolean editable);

    /**
     * Retrieves the class type of the field interface produced by the factory.
     * This class type extends {@link KlField} and indicates the specific
     * implementation or subinterface of {@link KlField} that the factory is designed to handle.
     *
     * @return A {@link Class} object representing the class type of the field
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
     * Retrieves the name of the associated field widget created by this factory.
     *
     * @return A string representing the name of the field widget.
     */
    String getName();

    /**
     * Retrieves a description of the field widget created by this factory.
     *
     * @return A string representing the description of the field or factory.
     */
    String getDescription();
}
