package dev.ikm.komet.kview.klfields.integerField;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlIntegerField;
import javafx.scene.Node;

import static dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl.DataType.INTEGER;

public class DefaultKlIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public DefaultKlIntegerField(ObservableField<Integer> observableIntegerField, ObservableView observableView, boolean isEditable) {
        super(observableIntegerField, observableView, isEditable);

        Node node;
        if (isEditable) {
            KLIntegerControl integerControl = new KLIntegerControl();

            integerControl.valueProperty().bindBidirectional(observableIntegerField.valueProperty());
            integerControl.setTitle(getTitle());

            node = integerControl;
        } else {
            KLReadOnlyDataTypeControl<Integer> readOnlyStringControl = new KLReadOnlyDataTypeControl<>();

            readOnlyStringControl.valueProperty().bindBidirectional(observableIntegerField.valueProperty());
            readOnlyStringControl.setTitle(getTitle());

            readOnlyStringControl.setType(INTEGER);

            node = readOnlyStringControl;
        }
        setKlWidget(node);
    }
}