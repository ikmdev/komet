package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLReadOnlyComponentControlSkin;
import javafx.geometry.Orientation;
import javafx.scene.control.Skin;

public class KLReadOnlyComponentControl extends KLReadOnlyBaseSingleValueControl<ComponentItem> {

    public KLReadOnlyComponentControl() {
        getStyleClass().add("read-only-component-control");
    }

    /**
     * The component's description is rendered in a wrapping node, so this control's height depends
     * on the width it is given. Neither {@code Control} nor {@code SkinBase} reports a content bias,
     * so without this override parents would size the control with {@code prefHeight(-1)} and only
     * ever make room for a single line of text.
     *
     * @return {@link Orientation#HORIZONTAL}
     */
    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLReadOnlyComponentControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLReadOnlyComponentControl.class.getResource("read-only-component-control.css").toExternalForm();
    }
}