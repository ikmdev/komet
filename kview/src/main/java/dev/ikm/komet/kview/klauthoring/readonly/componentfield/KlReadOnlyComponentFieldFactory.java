package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityProxy;

public class KlReadOnlyComponentFieldFactory implements KlFieldFactory<EntityProxy> {

    @Override
    public KlField<EntityProxy> create(ObservableField<EntityProxy> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        // Create a read-only string field
        return new KlReadOnlyComponentField(observableField, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<EntityProxy>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<EntityProxy>> getFieldImplementation() {
        return KlReadOnlyComponentField.class;
    }

    @Override
    public String getName() {
        return "Component Field Factory";
    }

    @Override
    public String getDescription() {
        return "A Component field";
    }
}