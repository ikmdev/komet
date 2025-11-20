package dev.ikm.komet.kview.klauthoring.editable.componentsetfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.IntIdSet;

public class KlEditableComponentSetFieldFactory implements KlFieldFactory<IntIdSet> {

    @Override
    public KlField<IntIdSet> create(ObservableField.Editable<IntIdSet> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableComponentSetField(observableFieldEditable, observableView, stamp4field);
    }
    /**
     * Retrieves the class type of the field interface produced by the factory.
     * This class type extends {@link KlField} and indicates the specific
     * implementation or subinterface of {@link KlField} that the factory is designed to handle.
     *
     * @return A {@link Class} object representing the class type of the field
     * interface extending {@link KlField}.
     */
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
    public Class<? extends KlField<IntIdSet>> getFieldImplementation() {
        return KlEditableComponentSetField.class;
    }

    /**
     * Retrieves the name of the associated field widget created by this factory.
     *
     * @return A string representing the name of the field widget.
     */
    public String getName() {
        return "Editable Component set field factory";
    }

    /**
     * Retrieves a description of the field widget created by this factory.
     *
     * @return A string representing the description of the field or factory.
     */
    public String getDescription() {
        return "A Component set field";
    }
}
