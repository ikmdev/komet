package dev.ikm.komet.kview.klfields.componentlistfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.IntIdList;

import java.util.UUID;

public class KlComponentListFieldFactory{

    public KlField<IntIdList> create(ObservableField<IntIdList> observableField, ObservableView observableView, boolean editable, UUID journalTopic) {
        return new DefaultKlComponentListField(observableField, observableView, editable, journalTopic);
    }

    public Class<? extends KlField<IntIdList>> getFieldInterface() {
        return null;
    }

    public Class<? extends KlField<IntIdList>> getFieldImplementation() {
        return DefaultKlComponentListField.class;
    }

    public String getName() {
        return "Component list field factory";
    }

    public String getDescription() {
        return "A Component list field";
    }
}