package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.ConceptNavigatorTooltipSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.Objects;
import java.util.ResourceBundle;

import static dev.ikm.komet.kview.controls.ConceptTile.DEFINED_PSEUDO_CLASS;

/**
 * <p>A custom {@link Tooltip} class that applies to the {@link ConceptTile concepts} of the {@link KLConceptNavigatorControl},
 * in such a way that when the {@link dev.ikm.tinkar.terms.ConceptFacade#description() concept's text} is truncated, the tooltip
 * shows it in full length, but when it is not, the tooltip doesn't show it at all.
 * </p>
 * <p>The tooltip shows, in any case, a visual indication of whether the concept is a
 * {@link ConceptNavigatorTreeItem#definedProperty() defined or primitive} concept.
 * </p>
 * <p>The tooltip is shown with the delay set in {@link KLConceptNavigatorControl#activationProperty()}, and its hidden
 * immediately after the mouse exits the associated node.
 * </p>
 * <p>For instance, this can be used to create a tooltip and installed into a label node:
 * <pre>  Label conceptLabel = new Label(entity.description());
 * ConceptNavigatorTooltip conceptNavigatorTooltip = new ConceptNavigatorTooltip(conceptLabel, new SimpleDoubleProperty(500));
 * </pre>
 */
public class ConceptNavigatorTooltip extends Tooltip {

    private final Node parent;
    private final Label typeTooltipLabel;
    private final String tooltipStylesheet = ConceptNavigatorTooltip.class.getResource("concept-navigator.css").toExternalForm();
    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

    /**
     * <p>Creates a new custom tooltip for an associated JavaFX node, with a given delay
     * </p>
     * @param node the JavaFX node that will be associated with this tooltip
     * @param delayProperty the {@link DoubleProperty} that specifies the delay between the mouse entering the associated
     *                      node and when this tooltip will be shown
     */
    public ConceptNavigatorTooltip(Node node, DoubleProperty delayProperty) {
        this.parent = Objects.requireNonNull(node).getParent();

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

        delayProperty.subscribe(d -> setShowDelay(new Duration(d.doubleValue())));
        setHideDelay(Duration.ZERO);
        Tooltip.install(node, this);
    }

    /**
     * <p>Update this tooltip with new values from the {@link ConceptTile} that holds
     * the associated node to this tooltip.
     * </p>
     * @param lookupText the real rendered text or null
     * @param description the full text
     * @param isDefined if the concept is defined or not
     */
    void updateTooltip(String lookupText, String description, boolean isDefined) {
        if (lookupText != null && !lookupText.equals(description)) {
            setText(description);
        } else {
            setText(null);
        }
        typeTooltipLabel.setText(isDefined ? resources.getString("defined.concept") : resources.getString("primitive.concept"));
        getGraphic().pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, isDefined);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void show() {
        final Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());
        Point2D anchor = new Point2D(bounds.getMinX() + 18, bounds.getMaxY());
        setAnchorX(anchor.getX());
        setAnchorY(anchor.getY());
        if (!getScene().getStylesheets().contains(tooltipStylesheet)) {
            getScene().getStylesheets().add(tooltipStylesheet);
        }
        super.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new ConceptNavigatorTooltipSkin(this);
    }
}
