package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTooltip;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

/**
 * <p>CSS based skin for {@link ConceptNavigatorTooltip}, which has the same implementation
 * as the default {@link javafx.scene.control.Tooltip}, except that it relocates the graphic node of the {@link Label}
 * node to the bottom-left, instead of the default bottom-center.
 * </p>
 */
public class ConceptNavigatorTooltipSkin implements Skin<ConceptNavigatorTooltip> {

    private Label tipLabel;
    private ConceptNavigatorTooltip tooltip;

    /**
     * Creates a new ConceptNavigatorTooltipSkin instance for the given {@link ConceptNavigatorTooltip}.
     * @param t the tooltip
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ConceptNavigatorTooltip getSkinnable() {
        return tooltip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNode() {
        return tipLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tooltip = null;
        tipLabel = null;
    }
}
