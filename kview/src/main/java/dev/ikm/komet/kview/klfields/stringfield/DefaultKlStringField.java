package dev.ikm.komet.kview.klfields.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyStringControl;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlStringField;
import javafx.scene.Node;

public class DefaultKlStringField extends BaseDefaultKlField<String> implements KlStringField {

    public DefaultKlStringField(ObservableField<String> observableStringField, ObservableView observableView, boolean isEditable) {
        super(observableStringField, observableView, isEditable);

        Node klWidget;
        if (isEditable) {
            KLStringControl stringControl = new KLStringControl();
            stringControl.textProperty().bindBidirectional(observableStringField.valueProperty());
            stringControl.setTitle(getTitle());
            klWidget = stringControl;
        } else {
            KLReadOnlyStringControl readOnlyStringControl = new KLReadOnlyStringControl();
            readOnlyStringControl.setText(observableStringField.value());
            readOnlyStringControl.setTitle(getTitle());
            klWidget = readOnlyStringControl;
        }

        setKlWidget(klWidget);
    }
}