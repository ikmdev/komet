package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.NodeUtils;
import dev.ikm.komet.kview.controls.ComponentItemNode;
import dev.ikm.komet.kview.controls.KLReadOnlyComponentControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class KLReadOnlyComponentControlSkin extends KLReadOnlyBaseControlSkin<KLReadOnlyComponentControl> {

    private final HBox textContainer = new HBox();
    private final ComponentItemNode componentItemNode = new ComponentItemNode();

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

        // CSS
        textContainer.getStyleClass().add("text-container");
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