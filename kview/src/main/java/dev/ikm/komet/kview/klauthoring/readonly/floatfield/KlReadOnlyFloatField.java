package dev.ikm.komet.kview.klauthoring.readonly.floatfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlFloatField;
import javafx.scene.layout.Region;

public class KlReadOnlyFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public KlReadOnlyFloatField(ObservableField<Float> observableFloatField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLReadOnlyDataTypeControl node = new KLReadOnlyDataTypeControl<>(Float.class);
        super(observableFloatField, observableView, stamp4field, node);
        node.valueProperty().bind(observableFloatField.editableValueProperty());
        node.setTitle(getTitle());
    }
}
