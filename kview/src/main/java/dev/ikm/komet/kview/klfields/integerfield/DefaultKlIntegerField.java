package dev.ikm.komet.kview.klfields.integerfield;

import static dev.ikm.komet.kview.klfields.KlFieldHelper.createTimeline;
import dev.ikm.komet.framework.observable.ObservableField;
import dev.ikm.komet.framework.view.ObservableView;
import dev.ikm.komet.kview.controls.KLIntegerControl;
import dev.ikm.komet.kview.controls.KLReadOnlyDataTypeControl;
import dev.ikm.komet.kview.klfields.BaseDefaultKlField;
import dev.ikm.komet.layout.component.version.field.KlIntegerField;
import javafx.animation.Timeline;
import javafx.scene.Parent;

public class DefaultKlIntegerField extends BaseDefaultKlField<Integer> implements KlIntegerField {

    public DefaultKlIntegerField(ObservableField<Integer> observableIntegerField, ObservableView observableView, boolean isEditable) {
        super(observableIntegerField, observableView, isEditable);

        Parent node;
        if (isEditable) {
            KLIntegerControl integerControl = new KLIntegerControl();
            integerControl.setTitle(getTitle());
            integerControl.valueProperty().set(observableIntegerField.valueProperty().getValue());
            Timeline timeline = createTimeline();
            timeline.setOnFinished((evt) -> {
                observableIntegerField.valueProperty().setValue(integerControl.getValue());
            });
            integerControl.valueProperty().subscribe( s -> {
                timeline.playFromStart();
            });
            node = integerControl;
        } else {
            KLReadOnlyDataTypeControl<Integer> readOnlyIntegerControl = new KLReadOnlyDataTypeControl<>(Integer.class);
            readOnlyIntegerControl.valueProperty().bindBidirectional(observableIntegerField.valueProperty());
            readOnlyIntegerControl.setTitle(getTitle());
            node = readOnlyIntegerControl;
        }
        setKlWidget(node);
    }
}