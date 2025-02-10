package dev.ikm.komet.kview.klfields.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.komet.layout.component.version.field.KlFloatField;

public class KlFloatFieldFactory implements KlFieldFactory<Float> {

    @Override
    public KlField<Float> create(ObservableField<Float> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlFloatField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<Float>> getFieldInterface() {
        return KlFloatField.class;
    }

    @Override
    public Class<? extends KlField<Float>> getFieldImplementation() {
        return DefaultKlFloatField.class;
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
