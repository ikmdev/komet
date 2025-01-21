package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlBooleanField;
import javafx.scene.Node;

public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {
    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, boolean isEditable) {
        super(observableBooleanField, observableView, isEditable);

        Node klWidget;
        // only editable control for boolean

        KLBooleanControl booleanControl = new KLBooleanControl();
        //TODO how do we bind the value of the radio button group to the observable boolean field?
        //booleanControl.textProperty().bindBidirectional(observableBooleanField.valueProperty());

        klWidget = booleanControl;


        setKlWidget(klWidget);
    }
}
