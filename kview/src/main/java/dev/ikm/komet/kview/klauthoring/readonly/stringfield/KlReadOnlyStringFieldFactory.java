package dev.ikm.komet.kview.klauthoring.readonly.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlStringField;
/**
 * A factory class for creating instances of {@link KlStringField}.
 */
public class KlReadOnlyStringFieldFactory implements KlFieldFactory<String> {

    /**
     * Creates an instance of KlReadOnlyStringField.
     * @param observableField The observable field containing String data
     * @param observableView The observable view context
     * @param stamp4field The observable stamp providing versioning information
     * @return An instance of KlField<String>
     */
    @Override
    public KlField<String> create(ObservableField<String> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlReadOnlyStringField(observableField, observableView, stamp4field);
    }


    @Override
    public Class<? extends KlField<String>> getFieldInterface() {
        return KlStringField.class;
    }

    @Override
    public Class<? extends KlField<String>> getFieldImplementation() {
        return KlReadOnlyStringField.class;
    }

    @Override
    public String getName() {
        return "Read-Only String Field Factory";
    }

    @Override
    public String getDescription() {
        return "A read-only String field factory for displaying non-editable text.";
    }
}