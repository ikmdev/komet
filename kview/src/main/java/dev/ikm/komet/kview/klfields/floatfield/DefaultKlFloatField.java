package dev.ikm.komet.kview.klfields.floatfield;

import static dev.ikm.komet.kview.klfields.KlFieldHelper.createTimeline;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLFloatControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlFloatField;
import javafx.animation.Timeline;
import javafx.scene.Parent;

public class DefaultKlFloatField extends BaseDefaultKlField<Float> implements KlFloatField {

    public DefaultKlFloatField(ObservableField<Float> observableFloatField, ObservableView observableView, boolean isEditable) {
        super(observableFloatField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLFloatControl floatControl = new KLFloatControl();
            floatControl.setTitle(getTitle());
            floatControl.valueProperty().set(observableFloatField.valueProperty().getValue());
            Timeline timeline = createTimeline();
            timeline.setOnFinished((evt) -> {
                observableFloatField.valueProperty().setValue(floatControl.getValue());
            });
            floatControl.valueProperty().subscribe( s -> {
                timeline.playFromStart();
            });
            node = floatControl;
        } else {
            KLReadOnlyDataTypeControl<Float> readOnlyStringControl = new KLReadOnlyDataTypeControl<>(Float.class);

            readOnlyStringControl.valueProperty().bindBidirectional(observableFloatField.valueProperty());
            readOnlyStringControl.setTitle(getTitle());

            node = readOnlyStringControl;
        }
        setKlWidget(node);
    }
}
