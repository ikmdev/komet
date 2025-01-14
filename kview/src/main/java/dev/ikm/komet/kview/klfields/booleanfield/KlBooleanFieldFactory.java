package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.layout.component.version.field.KlField;
import dev.ikm.komet.layout.component.version.field.KlFieldFactory;

public class KlBooleanFieldFactory implements KlFieldFactory {
    @Override
    public KlField create(ObservableField observableField, ObservableView observableView) {
        return null;
    }

    @Override
    public Class<? extends KlField> getFieldInterface() {
        return null;
    }

    @Override
    public Class<? extends KlField> getFieldImplementation() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }
}
