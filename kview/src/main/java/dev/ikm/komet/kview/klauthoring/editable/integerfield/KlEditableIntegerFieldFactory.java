package dev.ikm.komet.kview.klauthoring.editable.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.komet.layout.version.field.KlIntegerField;

public class KlEditableIntegerFieldFactory implements KlFieldFactory<Integer> {

    @Override
    public KlField<Integer> create(ObservableField.Editable<Integer> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableIntegerField(observableFieldEditable, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<Integer>> getFieldInterface() {
        return KlIntegerField.class;
    }

    @Override
    public Class<? extends KlField<Integer>> getFieldImplementation() {
        return KlEditableIntegerField.class;
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