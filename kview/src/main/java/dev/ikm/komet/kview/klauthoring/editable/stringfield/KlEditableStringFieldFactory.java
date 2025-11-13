package dev.ikm.komet.kview.klauthoring.editable.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlStringField;

/**
 * A factory class for creating instances of {@link KlStringField}.
 */
public class KlEditableStringFieldFactory implements KlFieldFactory<String> {

    /**
     * Creates an instance of KlEditableStringField.
     * @param observableField The observable field containing String data
     * @param observableView The observable view context
     * @param stamp4field The observable stamp providing versioning information
     * @return An instance of KlField<String>
     */
    @Override
    public KlField<String> create(ObservableField<String> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableStringField(observableField, observableView, stamp4field);
    }

    @Override
    public KlField<String> create(ObservableField.Editable<String> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableStringField(observableFieldEditable, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<String>> getFieldInterface() {
        return KlStringField.class;
    }

    @Override
    public Class<? extends KlField<String>> getFieldImplementation() {
        return KlEditableStringField.class;
    }

    @Override
    public String getName() {
        return "Editable String Field Factory";
    }

    @Override
    public String getDescription() {
        return "An editable String field";
    }
}