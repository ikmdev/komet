package dev.ikm.komet.kview.klauthoring.editable.componentlistfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.IntIdList;

public class KlEditableComponentListFieldFactory implements KlFieldFactory<IntIdList> {

    @Override
    public KlField<IntIdList> create(ObservableField.Editable<IntIdList> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableComponentListField(observableFieldEditable, observableView, stamp4field);
    }
    public Class<? extends KlField<IntIdList>> getFieldInterface() {
        return null;
    }

    public Class<? extends KlField<IntIdList>> getFieldImplementation() {
        return KlEditableComponentListField.class;
    }

    public String getName() {
        return "Component list field factory";
    }

    public String getDescription() {
        return "A Component list field";
    }
}