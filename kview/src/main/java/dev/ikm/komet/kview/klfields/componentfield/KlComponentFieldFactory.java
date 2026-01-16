package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityProxy;

public class KlComponentFieldFactory implements KlFieldFactory<EntityProxy> {

    @Override
    public KlField<EntityProxy> create(ObservableField<EntityProxy> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlComponentField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<EntityProxy>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<EntityProxy>> getFieldImplementation() {
        return DefaultKlComponentField.class;
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