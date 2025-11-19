package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlBooleanField;

import javafx.scene.layout.Region;

public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {

    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, ObservableStamp stamp4field) {
        Region klWidget = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> new KLBooleanControl();
            case false -> new KLReadOnlyDataTypeControl<>(Boolean.class);
        };
        super(observableBooleanField, observableView, stamp4field, klWidget);
        switch (klWidget) {
            case KLBooleanControl klBooleanControl -> {
                klBooleanControl.valueProperty().bindBidirectional(observableBooleanField.editableValueProperty());
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
