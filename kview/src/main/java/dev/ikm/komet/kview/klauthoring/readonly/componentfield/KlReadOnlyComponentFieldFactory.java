package dev.ikm.komet.kview.klauthoring.readonly.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityFacade;

public class KlReadOnlyComponentFieldFactory implements KlFieldFactory<EntityFacade> {

    @Override
    public KlField<EntityFacade> create(ObservableField<EntityFacade> observableField, ObservableView observableView, ObservableStamp stamp4field) {
        // Create a read-only string field
        return new KlReadOnlyComponentField(observableField, observableView, stamp4field);
    }

    @Override
    public Class<? extends KlField<EntityFacade>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<EntityFacade>> getFieldImplementation() {
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