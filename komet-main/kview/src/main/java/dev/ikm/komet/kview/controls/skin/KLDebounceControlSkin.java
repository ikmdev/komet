package dev.ikm.komet.kview.controls.skin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public abstract class KLDebounceControlSkin<C extends Control> extends SkinBase<C> {

    protected final TextField textField;
    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected KLDebounceControlSkin(C control) {
        super(control);
        control.setFocusTraversable(false);

        this.textField = new TextField();
        Timeline timeline = new Timeline();
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(3000), (evt) -> {});

        timeline.getKeyFrames().addAll(keyFrame1);

        timeline.setOnFinished((evt) -> {
            updateValueProperty();
        });

        textField.setOnKeyPressed( _ -> {
            timeline.playFromStart();
        });

        textField.focusedProperty().subscribe( () -> {
            if (!textField.isFocused()) {
                timeline.stop();
                updateValueProperty();
            }
        });

    }

     protected abstract void updateValueProperty();
}
