package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.IntIdSet;

public class KlComponentSetFieldFactory implements KlFieldFactory<IntIdSet> {

    /**
     * Creates a new instance of {@link KlField} associated with the provided observable data
     * and view context. The method uses the {@link ObservableView} to determine how to display the
     * field (compute preferred or fully qualified names for components, and similar),
     * enabling dynamic updates and integrations according to the factory's specifications.
     *
     * @param observableField the observable field that holds the data to be represented by the created {@link KlField}.
     * @param observableView  the observable view that defines the context in which the created {@link KlField} operates.
     * @param editable        flag to determine if the UI control is editable
     * @return a new {@link KlField} instance parameterized with the same type as the provided {@link ObservableField}.
     */
    @Override
    public KlField<IntIdSet> create(ObservableField<IntIdSet> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlComponentSetField(observableField, observableView, editable);
    }

    /**
     * Retrieves the class type of the field interface produced by the factory.
     * This class type extends {@link KlField} and indicates the specific
     * implementation or subinterface of {@link KlField} that the factory is designed to handle.
     *
     * @return A {@link Class} object representing the class type of the field
     * interface extending {@link KlField}.
     */
    @Override
    public Class<? extends KlField<IntIdSet>> getFieldInterface() {
        return null;
    }

    /**
     * Retrieves the class type of the specific implementation of {@link KlField}
     * that is produced by the factory.
     *
     * @return A {@link Class} object representing the class type of the implementation
     * of {@link KlField} associated with this factory.
     */
    @Override
    public Class<? extends KlField<IntIdSet>> getFieldImplementation() {
        return DefaultKlComponentSetField.class;
    }

    /**
     * Retrieves the name of the associated field widget created by this factory.
     *
     * @return A string representing the name of the field widget.
     */
    @Override
    public String getName() {
        return "Component set field factory";
    }

    /**
     * Retrieves a description of the field widget created by this factory.
     *
     * @return A string representing the description of the field or factory.
     */
    @Override
    public String getDescription() {
        return "A Component set field";
    }
}
