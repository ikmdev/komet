package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.ConceptNavigatorTooltipSkin;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class ConceptNavigatorTooltip extends Tooltip {

    private final Node parent;
    private final Label typeTooltipLabel;

    public ConceptNavigatorTooltip(Node node) {
        this.parent = node.getParent();

        Region ellipse = new Region();
        ellipse.getStyleClass().add("tooltip-ellipse");
        typeTooltipLabel = new Label();
        typeTooltipLabel.getStyleClass().add("type-tooltip-label");
        HBox box = new HBox(ellipse, typeTooltipLabel);
        box.getStyleClass().add("tooltip-box");

        setGraphic(box);
        contentDisplayProperty().bind(
                Bindings.when(textProperty().isNotEmpty())
                        .then(ContentDisplay.BOTTOM)
                        .otherwise(ContentDisplay.GRAPHIC_ONLY));
        getStyleClass().add("tooltip");

        Tooltip.install(node, this);
    }

    public void setGraphicText(String text) {
        typeTooltipLabel.setText(text);
    }

    @Override
    protected void show() {
        final Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());
        Point2D anchor = new Point2D(bounds.getMinX() + 18, bounds.getMaxY());
        setAnchorX(anchor.getX());
        setAnchorY(anchor.getY());
        super.show();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ConceptNavigatorTooltipSkin(this);
    }
}
