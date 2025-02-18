package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeCellSkin;

public class KLConceptNavigatorTreeCellSkin extends TreeCellSkin<ConceptNavigatorModel> {

    private TreeItem<?> treeItem;

    public KLConceptNavigatorTreeCellSkin(TreeCell<ConceptNavigatorModel> treeCell) {
        super(treeCell);
        treeItem = treeCell.getTreeItem();
        registerChangeListener(treeCell.treeItemProperty(), e -> {
            treeItem = treeCell.getTreeItem();
            getSkinnable().requestLayout();
        });
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelWidth = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);

        double pw = snappedLeftInset() + snappedRightInset();

        TreeView<ConceptNavigatorModel> tree = getSkinnable().getTreeView();
        if (tree == null) return pw;

        if (treeItem == null) return pw;

        pw = labelWidth;

        int level = tree.getTreeItemLevel(treeItem);
        if (!tree.isShowRoot()) level--;
        pw += getIndent() * level;

        return pw;
    }

    @Override
    protected void layoutChildren(double x, final double y, double w, final double h) {
        TreeView<ConceptNavigatorModel> tree = getSkinnable().getTreeView();
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

        layoutLabelInArea(x, y, w, h);
    }
}
