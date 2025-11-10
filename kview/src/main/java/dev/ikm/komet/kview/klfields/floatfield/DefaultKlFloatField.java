package dev.ikm.komet.kview.klfields.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlFloatField;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

public class DefaultKlFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public DefaultKlFloatField(ObservableField<Float> observableFloatField, ObservableView observableView, ObservableStamp stamp4field) {
        final Region node = switch (stamp4field.lastVersion().uncommitted()) {
            case true -> new KLFloatControl();
            case false -> new KLReadOnlyDataTypeControl<>(Float.class);     
        };
        
        super(observableFloatField, observableView, stamp4field, node);
        switch (node) {
            case KLFloatControl floatControl -> {
                floatControl.valueProperty().bindBidirectional(observableFloatField.editableValueProperty());
                floatControl.setTitle(getTitle());
            }
            case KLReadOnlyDataTypeControl readOnlyFloatControl -> {
                readOnlyFloatControl.valueProperty().bind(observableFloatField.valueProperty());
                readOnlyFloatControl.setTitle(getTitle());
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }
}
