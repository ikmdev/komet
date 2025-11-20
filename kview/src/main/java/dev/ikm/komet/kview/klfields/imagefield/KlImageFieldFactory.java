package dev.ikm.komet.kview.klfields.imagefield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;

public class KlImageFieldFactory implements KlFieldFactory<byte[]> {

    @Override
    public KlField<byte[]> create(ObservableField<byte[]> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new DefaultKlImageField(observableField, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<byte[]>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<byte[]>> getFieldImplementation() {
        return DefaultKlImageField.class;
    }

    @Override
    public String getName() {
        return "Image Field Factory";
    }

    @Override
    public String getDescription() {
        return "An Image field";
    }
}