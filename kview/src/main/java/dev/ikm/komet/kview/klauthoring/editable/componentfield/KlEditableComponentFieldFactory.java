package dev.ikm.komet.kview.klauthoring.editable.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityProxy;

public class KlEditableComponentFieldFactory implements KlFieldFactory<EntityProxy> {
    @Override
    public KlField<EntityProxy> create(ObservableField.Editable<EntityProxy> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableComponentField(observableFieldEditable, observableView, stamp4field);
    }
    @Override
    public Class<? extends KlField<EntityProxy>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<EntityProxy>> getFieldImplementation() {
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