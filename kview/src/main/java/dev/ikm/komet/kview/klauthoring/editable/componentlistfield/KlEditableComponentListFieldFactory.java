package dev.ikm.komet.kview.klauthoring.editable.componentlistfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.komet.layout.version.field.KlFieldFactory;
import dev.ikm.tinkar.common.id.LongIdList;

public class KlEditableComponentListFieldFactory implements KlFieldFactory<LongIdList> {

    @Override
    public KlField<LongIdList> create(ObservableField.Editable<LongIdList> observableFieldEditable, ObservableView observableView, ObservableStamp stamp4field) {
        return new KlEditableComponentListField(observableFieldEditable, observableView, stamp4field);
    }
    public Class<? extends KlField<LongIdList>> getFieldInterface() {
        return null;
    }

    public Class<? extends KlField<LongIdList>> getFieldImplementation() {
        return KlEditableComponentListField.class;
    }

    public String getName() {
        return "Component list field factory";
    }

    public String getDescription() {
        return "A Component list field";
    }
}