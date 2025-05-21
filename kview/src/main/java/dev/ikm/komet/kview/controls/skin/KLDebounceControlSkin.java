package dev.ikm.komet.kview.controls.skin;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.util.Duration;

public class KLDebounceControlSkin<C extends Control> extends SkinBase<C> {


    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    protected KLDebounceControlSkin(C control) {
        super(control);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(3000), (evt) -> {});

    }
}
