package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.ComponentItemNodeFactory;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class KLReadOnlyComponentControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyComponentControl> {

    private final HBox textContainer = new HBox();
    private final ComponentItemNode componentItemNode = ComponentItemNodeFactory.create();

    /**
     * @param control The control for which this Skin should attach to.
     */
    public KLReadOnlyComponentControlSkin(KLReadOnlyComponentControl control) {
        super(control);

        mainContainer.getChildren().addAll(textContainer);

        textContainer.getChildren().addAll(promptTextLabel, componentItemNode);

        HBox.setHgrow(promptTextLabel, Priority.ALWAYS);
        promptTextLabel.setMaxWidth(Double.MAX_VALUE);

        if (control.getValue() != null) {
            componentItemNode.setComponentItem(control.getValue());
        }
        control.valueProperty().subscribe(componentItemNode::setComponentItem);

        control.setContextMenu(null);

        componentItemNode.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(componentItemNode, Priority.ALWAYS);

        initTexts(control);

        // A component's description can be arbitrarily long (e.g. a LOINC test name), so it wraps
        // onto as many lines as it needs instead of running past the field's — and the card's — edge.
        componentItemNode.setWrapText(true);

        // CSS
        textContainer.getStyleClass().add("text-container");
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@code SkinBase} sizes children with {@code prefHeight(-1)}, which for the wrapping component
     * node always reports a single line. The height is computed from the width actually available
     * instead, so all of the wrapped lines are shown.
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double contentWidth = width == -1 ? -1 : width - leftInset - rightInset;
        return topInset + mainContainer.prefHeight(contentWidth) + bottomInset;
    }

    /** {@inheritDoc} */
    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    private void initTexts(KLReadOnlyComponentControl control) {
        updatePromptTextAndTextLabelVisibility(control);
        control.valueProperty().addListener(observable -> updatePromptTextAndTextLabelVisibility(control));
    }

    private void updatePromptTextAndTextLabelVisibility(KLReadOnlyComponentControl control) {
        boolean showPromptText = control.getValue() == null;

        NodeUtils.setShowing(promptTextLabel, showPromptText);
        NodeUtils.setShowing(componentItemNode, !showPromptText);
    }
}