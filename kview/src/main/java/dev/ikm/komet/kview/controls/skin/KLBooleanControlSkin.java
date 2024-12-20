package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.KLBooleanControl;
import javafx.scene.control.skin.RadioButtonSkin;
import javafx.scene.layout.StackPane;

/**
 * Default skin implementation for the {@link KLBooleanControl} control,
 * based on the {@link RadioButtonSkin}, but with a customized layout, where
 * the visual indication of the selected state is placed to the right.
 *
 * @see KLBooleanControl
 * @see javafx.scene.control.RadioButton
 */
public class KLBooleanControlSkin extends RadioButtonSkin {

    private final StackPane radio;

    /**
     * Creates a new KLBooleanControlSkin instance, installing the necessary child
     * nodes into the Control {@link javafx.scene.control.Control#getChildrenUnmodifiable() children} list, as
     * well as the necessary input mappings for handling key, mouse, etc. events.
     *
     * @param control The control that this skin should be installed onto.
     */
    public KLBooleanControlSkin(KLBooleanControl control) {
        super(control);
        radio = getChildren().stream()
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .findFirst()
                .orElseThrow();
    }

    /** {@inheritDoc} */
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        final KLBooleanControl control = (KLBooleanControl) getSkinnable();
        final double radioWidth = radio.prefWidth(-1);
        final double radioHeight = radio.prefHeight(-1);
        final double computeWidth = Math.max(control.prefWidth(-1), control.minWidth(-1));
        final double labelWidth = Math.min(computeWidth - radioWidth, computeWidth - snapSizeX(radioWidth));
        final double labelHeight = Math.min(control.prefHeight(labelWidth), contentHeight);
        final double maxHeight = Math.max(radioHeight, labelHeight);
        final double y = contentY + (contentHeight - maxHeight) / 2;

        layoutLabelInArea(contentX, y, labelWidth, maxHeight, control.getAlignment());
        radio.resize(snapSizeX(radioWidth), snapSizeY(radioHeight));
        positionInArea(radio, contentWidth - radioWidth, y, radioWidth, maxHeight, 0,
                control.getAlignment().getHpos(), control.getAlignment().getVpos());
    }

}
