package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.List;

public class KlComponentListFieldFactory implements KlFieldFactory<List<EntityProxy>> {

    @Override
    public KlField<List<EntityProxy>> create(ObservableField<List<EntityProxy>> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlComponentListField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<List<EntityProxy>>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<List<EntityProxy>>> getFieldImplementation() {
        return DefaultKlComponentListField.class;
    }

    @Override
    public String getName() {
        return "Component list field factory";
    }

    @Override
    public String getDescription() {
        return "A Component list field";
    }
}
