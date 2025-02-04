package dev.ikm.komet.kview.klfields.componentfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.IntIdList;

public class KlComponentListFieldFactory implements KlFieldFactory<IntIdList> {

    @Override
    public KlField<IntIdList> create(ObservableField<IntIdList> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlComponentListField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<IntIdList>> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField<IntIdList>> getFieldImplementation() {
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
