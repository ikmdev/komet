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

        if (isEditable) {
            KLBooleanControl booleanControl = new KLBooleanControl();
            booleanControl.getValueProperty().bindBidirectional(observableBooleanField.valueProperty());
            booleanControl.setTitle(getTitle());
            klWidget = booleanControl;
        } else {
            //TODO no read only control...?
            KLBooleanControl booleanControl = new KLBooleanControl(); // load fxml instead?


            booleanControl.setTitle(getTitle());
            klWidget = booleanControl;
        }

        setKlWidget(klWidget);
    }
}
