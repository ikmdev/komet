package dev.ikm.komet.kview.klfields.editable;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.layout.component.version.field.KlField;

public interface KlFieldFactory {
    enum EditControlType {
        READ_ONLY,
        EDITABLE,
    }

    /**
     * Creates a KlField object
     * @param editControlType
     * @param observableField
     * @return Returns a KlField instance containing an ObservableField. e.g. An editable String Kl Field having a text area.
     * @param <T> The derived class representing a KlField. e.g. An editable UI control.
     */
    <T extends KlField, S> T make(String metaData, ObservableField<S> observableField);
}
