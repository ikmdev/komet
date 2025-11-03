package dev.ikm.komet.kview.klfields.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlIntegerField;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

public class DefaultKlIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public DefaultKlIntegerField(ObservableField<Integer> observableIntegerField, ObservableView observableView, ObservableStamp stamp4field) {
        final Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> new KLIntegerControl();
            case false -> new KLReadOnlyDataTypeControl<>(Integer.class);
        };
        super(observableIntegerField, observableView, stamp4field, node);

        switch (node) {
            case KLIntegerControl integerControl -> {
                integerControl.valueProperty().bindBidirectional(observableIntegerField.editableValueProperty());
                integerControl.setTitle(getTitle());
            }
            case KLReadOnlyDataTypeControl readOnlyIntegerControl -> {
                readOnlyIntegerControl.valueProperty().bind(observableIntegerField.valueProperty());
                readOnlyIntegerControl.setTitle(getTitle());
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }

    }
}