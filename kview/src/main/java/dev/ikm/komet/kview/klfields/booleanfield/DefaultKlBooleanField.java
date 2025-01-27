package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlBooleanField;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;

import java.io.IOException;


public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {
    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, boolean isEditable) {
        super(observableBooleanField, observableView, isEditable);

        Node klWidget;

        if (isEditable) {
            // TODO temporary control... will be replace
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/ikm/komet/kview/controls/kl-boolean-control.fxml"));

            Node node = null;
            try {
                node = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Label title = (Label) node.lookup("#title");
            title.setText(getTitle());

            RadioButton radio1 = (RadioButton) node.lookup("#radio1");
            RadioButton radio2 = (RadioButton) node.lookup("#radio2");

            boolean initialValue = observableBooleanField.value();
            radio1.setSelected(initialValue);
            radio2.setSelected(!initialValue);

            radio1.selectedProperty().bindBidirectional(observableBooleanField.valueProperty());

            klWidget = node;

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
