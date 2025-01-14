package dev.ikm.komet.kview.klfields.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;
import dev.ikm.komet.layout.component.version.field.KlStringField;

public class KlStringFieldFactory implements KlFieldFactory<String> {

    @Override
    public KlField<String> create(ObservableField<String> observableField, ObservableView observableView, boolean editable) {
        return new DefaultKlStringField(observableField, observableView, editable);
    }

    @Override
    public Class<? extends KlField<String>> getFieldInterface() {
        return KlStringField.class;
    }

    @Override
    public Class<? extends KlField<String>> getFieldImplementation() {
        return DefaultKlStringField.class;
    }

    @Override
    public String getName() {
        return "String Field Factory";
    }

    @Override
    public String getDescription() {
        return "A String field";
    }
}