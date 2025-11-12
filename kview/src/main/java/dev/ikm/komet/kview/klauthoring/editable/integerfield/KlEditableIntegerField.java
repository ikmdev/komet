package dev.ikm.komet.kview.klauthoring.editable.integerfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlIntegerField;
import javafx.scene.layout.Region;

public class KlEditableIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public KlEditableIntegerField(ObservableField<Integer> observableIntegerField, ObservableView observableView, ObservableStamp stamp4field) {
        final KLIntegerControl node = new KLIntegerControl();
        super(observableIntegerField, observableView, stamp4field, node);
        node.valueProperty().bindBidirectional(observableIntegerField.editableValueProperty());
        node.setTitle(getTitle());
    }
}