package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell;
import dev.ikm.komet.kview.controls.LineageBox;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.layout.HBox;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Custom skin implementation for the {@link KLConceptNavigatorTreeCell} control.
 * Uses a {@link ConceptFacade} as the type of the value contained within the
 * {@link ConceptNavigatorTreeItem}
 * </p>
 * <p>Besides rendering as usual the graphic node of the cell (a {@link dev.ikm.komet.kview.controls.ConceptTile}),
 * this implementation also takes care of adding the connecting lines that belong to
 * the cell, the {@link LineageBox}, and the tags, which are added to the cell as extra nodes,
 * and rendered accordingly during the {@link #layoutChildren(double, double, double, double)} pass.
 * </p>
 */
public class KLConceptNavigatorTreeCellSkin extends TreeCellSkin<ConceptFacade> {

    private static final PseudoClass SHOW_TAGS_PSEUDO_CLASS = PseudoClass.getPseudoClass("show-tags");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

    private ConceptNavigatorTreeItem treeItem;
    private final KLConceptNavigatorTreeCell treeCell;
    private final KLConceptNavigatorControl treeView;
    private final LineageBox lineageBox;
    private final HBox tagBox;
    private double cellHeight, expandedHeight;

    /**
     * <p>Creates a new KLConceptNavigatorTreeCellSkin instance.
     * </p>
     * <p>Creates also a {@link LineageBox} instance and an {@link HBox} instance for the tags,
     * which will be added as children to the cell only when needed.
     * </p>
     * @param treeCell The control that this skin should be installed onto.
     * @see KLConceptNavigatorTreeCell#viewLineageProperty()
     * @see KLConceptNavigatorTreeCell#tagProperty()
     */
    public KLConceptNavigatorTreeCellSkin(KLConceptNavigatorTreeCell treeCell) {
        super(treeCell);
        this.treeCell = treeCell;
        treeItem = (ConceptNavigatorTreeItem) treeCell.getTreeItem();
        treeView = (KLConceptNavigatorControl) treeCell.getTreeView();
        registerChangeListener(treeCell.treeItemProperty(), _ -> {
            treeItem = (ConceptNavigatorTreeItem) treeCell.getTreeItem();
            getSkinnable().requestLayout();
        });

        lineageBox = new LineageBox();
        lineageBox.setNavigator(treeView.getNavigator());
        registerChangeListener(treeView.navigatorProperty(), _ -> lineageBox.setNavigator(treeView.getNavigator()));
        registerChangeListener(treeCell.viewLineageProperty(), _ -> {
            boolean viewLineage = treeCell.isViewLineage();
            if (viewLineage && !getChildren().contains(lineageBox)) {
                getChildren().add(lineageBox);
            }
            lineageBox.setVisible(viewLineage);
            lineageBox.setConcept(viewLineage ? treeItem : null);
            getSkinnable().requestLayout();
        });

        Label tagLabel = new Label();
        tagLabel.getStyleClass().add("tag-label");
        tagBox = new HBox(tagLabel);
        tagBox.getStyleClass().add("tag-box");
        tagBox.managedProperty().bind(tagBox.visibleProperty());
        tagBox.setVisible(treeView.isShowTags());
        registerChangeListener(treeView.showTagsProperty(), _ -> {
            tagBox.setVisible(treeView.isShowTags());
            getSkinnable().requestLayout();
        });
        registerChangeListener(treeCell.tagProperty(), _ -> {
            if (!getChildren().contains(tagBox)) {
                getChildren().add(tagBox);
            }
            tagLabel.setText(resources.getString("tag." + treeCell.getTag().toString().toLowerCase(Locale.ROOT)));
            getSkinnable().requestLayout();
        });

        registerChangeListener(treeView.showTagsProperty(), _ ->
                treeCell.pseudoClassStateChanged(SHOW_TAGS_PSEUDO_CLASS, treeView.isShowTags()));

    }

    /**
     * <p>Calculates the preferred height of this skin, taking into account the height needed for the {@link LineageBox}
     * if it is visible</p>
     * @param width the width that should be used if preferred height depends on it
     * @param topInset the pixel snapped top inset
     * @param rightInset the pixel snapped right inset
     * @param bottomInset the pixel snapped bottom inset
     * @param leftInset  the pixel snapped left inset
     * @return
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelHeight = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);

        if (treeCell.isViewLineage()) {
            labelHeight += Math.min(lineageBox.prefHeight(treeCell.getWidth()), lineageBox.maxHeight(treeCell.getWidth()));
        }

        return labelHeight;
    }

    /**
     * <p>Calculates the preferred width of this skin, removing the disclosure node width.
     * </p>
     * @param height the height that should be used if preferred width depends on it
     * @param topInset the pixel snapped top inset
     * @param rightInset the pixel snapped right inset
     * @param bottomInset the pixel snapped bottom inset
     * @param leftInset  the pixel snapped left inset
     * @return
     */
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelWidth = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);

        double pw = snappedLeftInset() + snappedRightInset();

        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) return pw;

        if (treeItem == null) return pw;

        pw = labelWidth;

        int level = tree.getTreeItemLevel(treeItem);
        if (!tree.isShowRoot()) level--;
        pw += getIndent() * level;

        return pw;
    }

    /**
     * <p>Layout pass that removes the disclosure node, and adds the connecting lines, and
     * the {@link LineageBox} and tags box, when visible</p>
     * @param x the x position
     * @param y the y position
     * @param w the width
     * @param h the height
     */
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) return;

        int level = tree.getTreeItemLevel(treeItem);
        if (!tree.isShowRoot()) level--;
        double leftMargin = getIndent() * level;
        x += leftMargin;

        final int padding = treeItem != null && treeItem.getGraphic() == null ? 0 : 3;
        x += padding;
        w -= (leftMargin + padding);

        Node graphic = getSkinnable().getGraphic();
        if (graphic != null && !getChildren().contains(graphic)) {
            getChildren().add(graphic);
        }

        boolean expanded = treeItem != null && treeItem.isViewLineage();
        expandedHeight = expanded ? Math.min(lineageBox.prefHeight(w), lineageBox.maxHeight(w)) : 0;
        if (expanded) {
            y += expandedHeight;
            h -= expandedHeight;
        }

        layoutLabelInArea(x, y, w, h);

        cellHeight = h + snappedTopInset() + snappedBottomInset();
        updateConnections();

        if (expanded) {
            if (!getChildren().contains(lineageBox)) {
                getChildren().add(lineageBox);
            }
            double cellPadding = treeCell.getInsets().getLeft();
            lineageBox.resizeRelocate(cellPadding, y - expandedHeight, getSkinnable().getWidth() - cellPadding, expandedHeight);
            lineageBox.toFront();
        }

        if (!getChildren().contains(tagBox)) {
            getChildren().add(tagBox);
        }
        tagBox.toBack();
        tagBox.resizeRelocate(0, y, w, h);

    }

    private void updateConnections() {
        ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) getSkinnable().getTreeItem();
        if (treeView == null || model == null) {
            return;
        }
        List<Path> oldPaths = getChildren().stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .toList();
        int level = getLevel(model);
        double padding = treeCell.getInsets().getLeft() - 1;
        double indent = getIndent();
        List<Path> paths = new ArrayList<>();
        int start = treeView.isShowRoot() ? 1 : 0;
        for (int i = start; i < level; i++) {
            double x = 10 + indent * i + padding;
            if (i < level - 1) {
                ConceptNavigatorTreeItem ancestor = getAncestor(model, i + 1);
                if (ancestor.nextSibling() != null) {
                    paths.add(getLine(x, "dashed-line"));
                    paths.add(getLine(x, "solid-line-" + (i - start)));
                }
            } else {
                paths.add(getCurvedLine(x, model.nextSibling() == null, "dashed-curved-line"));
                paths.add(getLine(x, "solid-line-" + (i - start)));
                paths.add(getCurvedLine(x, true, "solid-curved-line"));
            }
        }
        if (!oldPaths.equals(paths)) {
            getChildren().removeAll(oldPaths);
            getChildren().addAll(paths);
        }
    }

    private ConceptNavigatorTreeItem getAncestor(ConceptNavigatorTreeItem treeItem, int level) {
        if (treeItem == null) {
            return null;
        }
        ConceptNavigatorTreeItem ancestor = (ConceptNavigatorTreeItem) treeItem.getParent();
        while (ancestor != null) {
            if (getLevel(ancestor) == level) {
                return ancestor;
            }
            ancestor = (ConceptNavigatorTreeItem) ancestor.getParent();
        }
        return null;
    }

    private int getLevel(ConceptNavigatorTreeItem model) {
        return treeView.getTreeItemLevel(model) - (treeView.isShowRoot() ? 0 : 1);
    }

    /**
     * <p>Create a vertical line, spanning the cell height, at a given indentation level,
     * with a given style class.
     * </p>
     * @param x the x coordinate based on the indentation level for this line
     * @param styleClass the style class to be applied to this line
     * @return a {@link Path}
     */
    private Path getLine(double x, String styleClass) {
        Path line = new Path();
        double conceptTileHeight = treeCell.getHeight() - expandedHeight;
        line.getElements().addAll(new MoveTo(x, 0), new LineTo(x, expandedHeight + conceptTileHeight));
        line.getStyleClass().add(styleClass);
        return line;
    }

    /**
     * <p>Create a curved line, that goes from the top of the cell to the center, at a given indentation level,
     * with a given style class, if the cell is the last sibling, or else, add a vertical line to the bottom
     * of the cell.
     * </p>
     * @param x the x coordinate based on the indentation level for this line
     * @param isLastSibling if the treeItem for this cell is the last sibling
     * @param styleClass the style class to be applied to this line
     * @return a {@link Path}
     */
    private Path getCurvedLine(double x, boolean isLastSibling, String styleClass) {
        Path curvedLine = new Path();
        double conceptTileHeight = treeCell.getHeight() - expandedHeight;
        if (isLastSibling) {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, expandedHeight + conceptTileHeight / 2 - 5),
                    new ArcTo(5, 5, 90, x + 5, expandedHeight + conceptTileHeight / 2, false, false)
            );
        } else {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, expandedHeight + conceptTileHeight), new MoveTo(x, expandedHeight + conceptTileHeight / 2 - 5),
                    new ArcTo(5, 5, 90, x + 5, expandedHeight + conceptTileHeight / 2, false, false)
            );
        }
        curvedLine.getStyleClass().add(styleClass);
        return curvedLine;
    }
}
