package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.IconRegion;
import dev.ikm.komet.kview.controls.LineageBox;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * <p>Custom skin implementation for the {@link LineageBox} control.
 * It just extends the {@link ScrollPaneSkin} by adding a close icon
 * to the top right side of the lineage box, that allows closing it.
 * </p>
 */
public class LineageBoxSkin extends ScrollPaneSkin {
    private final StackPane closePane;

    /**
     * <p>Create a {@link LineageBoxSkin} instance</p>
     * @param lineageBox The control that this skin should be installed onto.
     */
    public LineageBoxSkin(LineageBox lineageBox) {
        super(lineageBox);

        IconRegion closeIconRegion = new IconRegion("icon", "close");
        closePane = new StackPane(closeIconRegion);
        closePane.getStyleClass().addAll("region", "close");
        closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                ConceptNavigatorTreeItem concept = lineageBox.getConcept();
                if (concept != null) {
                    concept.setViewLineage(false);
                    concept.getInvertedTree().reset();
                    lineageBox.setConcept(null);
                }
            }
            e.consume();
        });
        closePane.setManaged(false);
        getChildren().add(closePane);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefHeight = getSkinnable().getContent().prefHeight(width);
        if (getHorizontalScrollBar().isVisible()) {
            prefHeight += getHorizontalScrollBar().prefHeight(width);
        }
        return prefHeight + snappedTopInset() + snappedBottomInset();
    }

    /** {@inheritDoc} **/
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        double w2 = closePane.prefWidth(w);
        double h2 = closePane.prefHeight(h);
        closePane.resizeRelocate(w - w2 - 4, 4, w2, h2);
    }
}
