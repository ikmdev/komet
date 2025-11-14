package dev.ikm.komet.kview.klauthoring.editable.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlBooleanField;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;

public class KlEditableBooleanFieldFactory implements KlFieldFactory<Boolean> {
    @Override
    public KlField<Boolean> create(ObservableField observableField, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableBooleanField(observableField, observableView, stamp4field);
    }

    @Override
    public KlField<Boolean> create(ObservableField.Editable<Boolean> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableBooleanField(observableFieldEditable, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<Boolean>> getFieldInterface() {
        return KlBooleanField.class;
    }

    @Override
    public Class<? extends KlField<Boolean>> getFieldImplementation() {
        return KlEditableBooleanField.class;
    }

    @Override
    public String getName() {
        return "Boolean Field Factory";
    }

    @Override
    public String getDescription() {
        return "A Radio button group";
    }
}
