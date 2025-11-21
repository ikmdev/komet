package dev.ikm.komet.kview.klauthoring.editable.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlFloatField;

public class KlEditableFloatFieldFactory implements KlFieldFactory<Float> {

    @Override
    public KlField<Float> create(ObservableField.Editable<Float> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableFloatField(observableFieldEditable, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<Float>> getFieldInterface() {
        return KlFloatField.class;
    }

    @Override
    public Class<? extends KlField<Float>> getFieldImplementation() {
        return KlEditableFloatField.class;
    }

    @Override
    public String getName() {
        return "Float Field Factory";
    }

    @Override
    public String getDescription() {
        return "A Float field";
    }
}
