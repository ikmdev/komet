package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlBooleanField;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {
    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, boolean isEditable) {
        super(observableBooleanField, observableView, isEditable);

        Node klWidget;
        if (isEditable) {
            KLBooleanControl klBooleanControl = new KLBooleanControl();

            klBooleanControl.setTitle(getTitle());
            klBooleanControl.valueProperty().bindBidirectional(observableBooleanField.valueProperty());

            klWidget = klBooleanControl;
        } else {
            KLReadOnlyDataTypeControl<Boolean> klReadOnlyBooleanControl = new KLReadOnlyDataTypeControl<>();
            klReadOnlyBooleanControl.setTitle(getTitle());

            ObjectProperty<Boolean> booleanProperty = observableBooleanField.valueProperty();

            klReadOnlyBooleanControl.valueProperty().bind(booleanProperty);

            klWidget = klReadOnlyBooleanControl;
        }
        setKlWidget(klWidget);
    }
}
