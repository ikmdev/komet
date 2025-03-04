package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.AlternateView;
import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorTreeCell;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

public class KLConceptNavigatorTreeCellSkin extends TreeCellSkin<ConceptNavigatorModel> {

    private TreeItem<?> treeItem;
    private final KLConceptNavigatorTreeCell treeCell;
    private final TreeView<ConceptNavigatorModel> treeView;
    private final AlternateView alternateView;

    public KLConceptNavigatorTreeCellSkin(KLConceptNavigatorTreeCell treeCell) {
        super(treeCell);
        this.treeCell = treeCell;
        treeItem = treeCell.getTreeItem();
        treeView = treeCell.getTreeView();
        alternateView = new AlternateView();
        registerChangeListener(treeCell.treeItemProperty(), e -> {
            if (treeItem != null) {
                unregisterChangeListeners(treeItem.valueProperty());
            }
            treeItem = treeCell.getTreeItem();
            if (treeItem != null) {
                registerChangeListener(treeItem.valueProperty(), ev -> alternateView.setConcept((ConceptNavigatorModel) treeItem.getValue()));
                alternateView.setConcept((ConceptNavigatorModel) treeItem.getValue());
            }
            getSkinnable().requestLayout();
        });
        registerChangeListener(treeCell.expandedProperty(), e -> {
            alternateView.setVisible(treeCell.isExpanded());
            getSkinnable().requestLayout();
        });
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelHeight = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);

        if (treeCell.isExpanded()) {
            labelHeight += alternateView.prefHeight(treeCell.getWidth());
        }

        return labelHeight;
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
    protected void layoutChildren(double x, double y, double w, double h) {
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

        boolean expanded = treeItem != null && treeItem.getValue() != null && ((ConceptNavigatorModel) treeItem.getValue()).isExpanded();
        double expandedHeight = expanded ? alternateView.prefHeight(w) : 0;
        if (expanded) {
            y += expandedHeight;
            h -= expandedHeight;
        }

        layoutLabelInArea(x, y, w, h);

        updateConnections();

        if (expanded) {
            if (!getChildren().contains(alternateView)) {
                getChildren().add(alternateView);
            }
            alternateView.resizeRelocate(0, y - expandedHeight, getSkinnable().getWidth() - 1, expandedHeight);
        }
    }

    private void updateConnections() {
        TreeItem<ConceptNavigatorModel> treeItem = getSkinnable().getTreeItem();
        if (treeView == null || treeItem == null) {
            return;
        }
        List<Path> oldPaths = getChildren().stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .toList();
        int level = getLevel(treeItem);
        double indent = getIndent();
        List<Path> paths = new ArrayList<>();
        int start = treeView.isShowRoot() ? 1 : 0;
        for (int i = start; i < level; i++) {
            double x = 10 + indent * i;
            if (i < level - 1) {
                TreeItem<ConceptNavigatorModel> ancestor = getAncestor(treeItem, i + 1);
                if (ancestor.nextSibling() != null) {
                    paths.add(getLine(x, "dashed-line"));
                    paths.add(getLine(x, "solid-line-" + (i - start)));
                }
            } else {
                paths.add(getCurvedLine(x, treeItem.nextSibling() == null, "dashed-curved-line"));
                paths.add(getLine(x, "solid-line-" + (i - start)));
                paths.add(getCurvedLine(x, true, "solid-curved-line"));
            }
        }
        if (!oldPaths.equals(paths)) {
            getChildren().removeAll(oldPaths);
            getChildren().addAll(paths);
        }
    }

    private TreeItem<ConceptNavigatorModel> getAncestor(TreeItem<ConceptNavigatorModel> treeItem, int level) {
        if (treeItem == null) {
            return null;
        }
        TreeItem<ConceptNavigatorModel> ancestor = treeItem.getParent();
        while (ancestor != null) {
            if (getLevel(ancestor) == level) {
                return ancestor;
            }
            ancestor = ancestor.getParent();
        }
        return null;
    }

    private int getLevel(TreeItem<ConceptNavigatorModel> treeItem) {
        return treeView.getTreeItemLevel(treeItem) - (treeView.isShowRoot() ? 0 : 1);
    }

    private Path getLine(double x, String styleClass) {
        Path line = new Path();
        double yOrigin = treeCell.isExpanded() ? treeCell.getHeight() - 24 : 0;
        line.getElements().addAll(new MoveTo(x, yOrigin), new LineTo(x, yOrigin + 24));
        line.getStyleClass().add(styleClass);
        return line;
    }

    private Path getCurvedLine(double x, boolean isLastSibling, String styleClass) {
        Path curvedLine = new Path();
        double yOrigin = treeCell.isExpanded() ? treeCell.getHeight() - 24 : 0;
        if (isLastSibling) {
            curvedLine.getElements().addAll(
                    new MoveTo(x, yOrigin), new LineTo(x, yOrigin + 7),
                    new ArcTo(5, 5, 90, x + 5, yOrigin + 12, false, false)
            );
        } else {
            curvedLine.getElements().addAll(
                    new MoveTo(x, yOrigin), new LineTo(x, yOrigin + 24), new MoveTo(x, yOrigin + 7),
                    new ArcTo(5, 5, 90, x + 5, yOrigin + 12, false, false)
            );
        }
        curvedLine.getStyleClass().add(styleClass);
        return curvedLine;
    }
}
