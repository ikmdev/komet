package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.ConceptNavigatorTooltip;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

public class ConceptNavigatorTooltipSkin implements Skin<ConceptNavigatorTooltip> {

    private Label tipLabel;
    private ConceptNavigatorTooltip tooltip;

    public ConceptNavigatorTooltipSkin(ConceptNavigatorTooltip t) {
        this.tooltip = t;
        tipLabel = new Label() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                // relocate graphic at bottom-left
                getGraphic().relocate(getPadding().getLeft(), getGraphic().getLayoutY());
            }
        };
        tipLabel.contentDisplayProperty().bind(t.contentDisplayProperty());
        tipLabel.fontProperty().bind(t.fontProperty());
        tipLabel.graphicProperty().bind(t.graphicProperty());
        tipLabel.graphicTextGapProperty().bind(t.graphicTextGapProperty());
        tipLabel.textAlignmentProperty().bind(t.textAlignmentProperty());
        tipLabel.textOverrunProperty().bind(t.textOverrunProperty());
        tipLabel.textProperty().bind(t.textProperty());
        tipLabel.wrapTextProperty().bind(t.wrapTextProperty());
        tipLabel.minWidthProperty().bind(t.minWidthProperty());
        tipLabel.prefWidthProperty().bind(t.prefWidthProperty());
        tipLabel.maxWidthProperty().bind(t.maxWidthProperty());
        tipLabel.minHeightProperty().bind(t.minHeightProperty());
        tipLabel.prefHeightProperty().bind(t.prefHeightProperty());
        tipLabel.maxHeightProperty().bind(t.maxHeightProperty());

        tipLabel.getStyleClass().setAll(t.getStyleClass());
        tipLabel.setStyle(t.getStyle());
        tipLabel.setId(t.getId());
    }

    @Override
    public ConceptNavigatorTooltip getSkinnable() {
        return tooltip;
    }

    @Override
    public Node getNode() {
        return tipLabel;
    }

    @Override
    public void dispose() {
        tooltip = null;
        tipLabel = null;
    }
}
