package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlBooleanField;
import javafx.beans.property.ObjectProperty;

import javafx.scene.layout.Region;

public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {

    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, boolean isEditable) {
        Region klWidget = switch (isEditable) {
            case true -> new KLBooleanControl();
            case false -> new KLReadOnlyDataTypeControl<>(Boolean.class);
        };
        super(observableBooleanField, observableView, isEditable, klWidget);
        switch (klWidget) {
            case KLBooleanControl klBooleanControl -> {
                klBooleanControl.valueProperty().bindBidirectional(observableBooleanField.valueProperty());
                klBooleanControl.setTitle(getTitle());
            }
            case KLReadOnlyDataTypeControl klReadOnlyBooleanControl -> {
                klReadOnlyBooleanControl.valueProperty().bind(observableBooleanField.valueProperty());
                klReadOnlyBooleanControl.setTitle(getTitle());
            }
            default -> throw new IllegalStateException("Unexpected value: " + klWidget);
        }
    }
}
