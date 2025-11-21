package dev.ikm.komet.kview.klauthoring.readonly.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlFloatField;

public class KlReadOnlyFloatFieldFactory implements KlFieldFactory<Float> {

    @Override
    public KlField<Float> create(ObservableField<Float> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlReadOnlyFloatField(observableField, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<Float>> getFieldInterface() {
        return KlFloatField.class;
    }

    @Override
    public Class<? extends KlField<Float>> getFieldImplementation() {
        return KlReadOnlyFloatField.class;
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
