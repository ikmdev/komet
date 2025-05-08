package dev.ikm.komet.kview.klfields.stringfield;

import static dev.ikm.komet.kview.klfields.KlFieldHelper.createTimeline;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.controls.KLStringControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlStringField;
import javafx.animation.Timeline;
import javafx.scene.Parent;

public class DefaultKlStringField extends BaseDefaultKlField<String> implements KlStringField {

    public DefaultKlStringField(ObservableField<String> observableStringField, ObservableView observableView, boolean isEditable) {
        super(observableStringField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLStringControl stringControl = new KLStringControl();
            stringControl.textProperty().set(observableStringField.valueProperty().getValue());
            Timeline timeline = createTimeline();
            timeline.setOnFinished((evt) -> {
                observableStringField.valueProperty().setValue(stringControl.getText());
            });
            stringControl.textProperty().subscribe( s -> {
                timeline.playFromStart();
            });
            node = stringControl;
        } else {
            KLReadOnlyDataTypeControl<String> readOnlyStringControl = new KLReadOnlyDataTypeControl<>(String.class);

            readOnlyStringControl.valueProperty().bindBidirectional(observableStringField.valueProperty());
            readOnlyStringControl.setTitle(getTitle());

            node = readOnlyStringControl;
        }

        setKlWidget(node);
    }
}