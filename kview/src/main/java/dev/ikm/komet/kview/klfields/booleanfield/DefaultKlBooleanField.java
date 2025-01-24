package dev.ikm.komet.kview.klfields.booleanfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLBooleanControl;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlBooleanField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.converter.BooleanStringConverter;

import java.io.IOException;
import java.util.Map;

public class DefaultKlBooleanField extends BaseDefaultKlField<Boolean> implements KlBooleanField {
    public DefaultKlBooleanField(ObservableField<Boolean> observableBooleanField, ObservableView observableView, boolean isEditable) {
        super(observableBooleanField, observableView, isEditable);

        Node klWidget;

        if (isEditable) {
//            KLBooleanControl booleanControl = new KLBooleanControl();
//            booleanControl.getValueProperty().bindBidirectional(observableBooleanField.valueProperty());
//            booleanControl.setTitle(getTitle());
//            klWidget = booleanControl;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dev/ikm/komet/kview/controls/kl-boolean-control.fxml"));

            Node node = null;
            try {
                node = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Label title = (Label) node.lookup("#title");
            title.setText(getTitle());


            Map<String, Object> fxmlNamespace = loader.getNamespace();
            ToggleGroup toggleGroup = (ToggleGroup) fxmlNamespace.get("klBooleanToggleGroup");

            RadioButton radio1 = (RadioButton) node.lookup("#radio1");
            RadioButton radio2 = (RadioButton) node.lookup("#radio2");

            //TODO get both toggles by their IDs and compare to the selected toggle
            //inside the toggle group it will tell us the selection, but not true|false

            //FIXME set up the callable
//            BooleanBinding booleanBinding = Bindings.createBooleanBinding(
//                    //TODO callable
//                    , observableBooleanField.valueProperty()
//            );

//            if (toggleGroup.getSelectedToggle().equals(radio1)) { // True
//                observableBooleanField.valueProperty().
//            } else { // False
//
//            }

            // selected toggle property is read only...???
            //observableBooleanField.valueProperty().bindBidirectional();
//            toggleGroup.selectedToggleProperty().addListener((observable, oldVal, newVal)
//                    -> System.out.println(newVal + " was selected"));

            klWidget = node;

        } else {
            // use string control for read only klboolean ???
            KLReadOnlyStringControl klReadOnlyStringControl = new KLReadOnlyStringControl();
            klReadOnlyStringControl.setTitle(getTitle());

            // bind bidirectional
            ObjectProperty<Boolean> booleanProperty = observableBooleanField.valueProperty();

            klReadOnlyStringControl.textProperty().bind(booleanProperty.asString());

            klWidget = klReadOnlyStringControl;
        }
        setKlWidget(klWidget);
    }
}
