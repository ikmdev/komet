package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityFacade;

public class KlEditableComponentFieldFactory implements KlFieldFactory<EntityFacade> {
    @Override
    public KlField<EntityFacade> create(ObservableField.Editable<EntityFacade> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableComponentField(observableFieldEditable, observableView, stamp4field);
    }
    @Override
    public Class<? extends KlField<EntityFacade>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<EntityFacade>> getFieldImplementation() {
        return KlEditableComponentField.class;
    }

    @Override
    public String getName() {
        return "Editable Component Field Factory";
    }

    @Override
    public String getDescription() {
        return "An editable component field";
    }
}