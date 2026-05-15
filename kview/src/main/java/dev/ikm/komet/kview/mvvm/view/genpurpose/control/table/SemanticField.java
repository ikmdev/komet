package dev.ikm.komet.kview.mvvm.view.genpurpose.control.table;

import dev.ikm.komet.framework.observable.ObservableField;
import javafx.beans.property.ObjectProperty;

public record SemanticField<T>(
        ObservableField<T> observableField,
        int dataType,
        String fieldTitle,
        String fieldPurpose
) {
    public ObjectProperty<T> observableFieldProperty() {
        return observableField.editableValueProperty();
    }
}