package dev.ikm.komet.kview.klfields.stringfield;

import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.version.field.KlStringField;
import javafx.scene.layout.Region;


public class DefaultKlStringField extends BaseDefaultKlField<String> implements KlStringField {

    public DefaultKlStringField(ObservableField<String> observableStringField, ObservableView observableView, boolean isEditable) {
        final Region node = switch (isEditable) {
            case true -> new KLStringControl();
            case false -> new KLReadOnlyDataTypeControl<>(String.class);
        };
        super(observableStringField, observableView, isEditable, node);
        switch (node) {
            case KLStringControl stringControl -> {
                stringControl.textProperty().bindBidirectional(observableStringField.editableValueProperty());
                stringControl.setTitle(getTitle());
            }
            case KLReadOnlyDataTypeControl readOnlyStringControl -> {
                readOnlyStringControl.valueProperty().bind(observableStringField.valueProperty());
                readOnlyStringControl.setTitle(getTitle());
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }
}