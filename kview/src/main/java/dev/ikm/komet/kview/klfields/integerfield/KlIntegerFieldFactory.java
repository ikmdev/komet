package dev.ikm.komet.kview.klfields.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlIntegerField;

public class KlIntegerFieldFactory implements KlFieldFactory<Integer> {

    @Override
    public KlField<Integer> create(ObservableField<Integer> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new DefaultKlIntegerField(observableField, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<Integer>> getFieldInterface() {
        return KlIntegerField.class;
    }

    @Override
    public Class<? extends KlField<Integer>> getFieldImplementation() {
        return DefaultKlIntegerField.class;
    }

    @Override
    public String getName() {
        return "Integer Field Factory";
    }

    @Override
    public String getDescription() {
        return "An Integer field";
    }
}