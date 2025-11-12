package dev.ikm.komet.kview.klauthoring.readonly.componentlistfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.version.field.KlField;
import dev.ikm.tinkar.common.id.IntIdList;

import java.util.*;

public class KlReadOnlyComponentListFieldFactory {

    public KlField<IntIdList> create(ObservableField<IntIdList> observableField, ObservableView observableView, ObservableStamp stamp4field, UUID journalTopic) {
        return new KlReadOnlyComponentListField(observableField, observableView, stamp4field, journalTopic);
    }

    public Class<? extends KlField<IntIdList>> getFieldInterface() {
        return null;
    }

    public Class<? extends KlField<IntIdList>> getFieldImplementation() {
        return KlReadOnlyComponentListField.class;
    }

    public String getName() {
        return "Component list field factory";
    }

    public String getDescription() {
        return "A Component list field";
    }
}