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

public class KLConceptNavigatorTreeCellSkin extends TreeCellSkin<ConceptFacade> {

    private static final PseudoClass SHOW_TAGS_PSEUDO_CLASS = PseudoClass.getPseudoClass("show-tags");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

    private ConceptNavigatorTreeItem model;
    private final KLConceptNavigatorTreeCell treeCell;
    private final KLConceptNavigatorControl treeView;
    private final LineageBox lineageBox;
    private final HBox tagBox;

    public KLConceptNavigatorTreeCellSkin(KLConceptNavigatorTreeCell treeCell) {
        super(treeCell);
        this.treeCell = treeCell;
        model = (ConceptNavigatorTreeItem) treeCell.getTreeItem();
        treeView = (KLConceptNavigatorControl) treeCell.getTreeView();
        registerChangeListener(treeCell.treeItemProperty(), _ -> {
            model = (ConceptNavigatorTreeItem) treeCell.getTreeItem();
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
            lineageBox.setConcept(viewLineage ? model : null);
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

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelHeight = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);

        if (treeCell.isViewLineage()) {
            labelHeight += Math.min(lineageBox.prefHeight(treeCell.getWidth()), lineageBox.maxHeight(treeCell.getWidth()));
        }

        return labelHeight;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelWidth = super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);

        double pw = snappedLeftInset() + snappedRightInset();

        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) return pw;

        if (model == null) return pw;

        pw = labelWidth;

        int level = tree.getTreeItemLevel(model);
        if (!tree.isShowRoot()) level--;
        pw += getIndent() * level;

        return pw;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        TreeView<ConceptFacade> tree = getSkinnable().getTreeView();
        if (tree == null) return;

        int level = tree.getTreeItemLevel(model);
        if (!tree.isShowRoot()) level--;
        double leftMargin = getIndent() * level;
        x += leftMargin;

        final int padding = model != null && model.getGraphic() == null ? 0 : 3;
        x += padding;
        w -= (leftMargin + padding);

        Node graphic = getSkinnable().getGraphic();
        if (graphic != null && !getChildren().contains(graphic)) {
            getChildren().add(graphic);
        }

        boolean expanded = model != null && model.isViewLineage();
        double expandedHeight = expanded ? Math.min(lineageBox.prefHeight(w), lineageBox.maxHeight(w)) : 0;
        if (expanded) {
            y += expandedHeight;
            h -= expandedHeight;
        }

        layoutLabelInArea(x, y, w, h);

        updateConnections();

        if (expanded) {
            if (!getChildren().contains(lineageBox)) {
                getChildren().add(lineageBox);
            }
            double cellPadding = treeCell.getInsets().getLeft();
            lineageBox.resizeRelocate(cellPadding, y - expandedHeight, getSkinnable().getWidth() - cellPadding, expandedHeight);
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

    private Path getLine(double x, String styleClass) {
        Path line = new Path();
        double yOrigin = treeCell.isViewLineage() ? treeCell.getHeight() - 24 : 0;
        line.getElements().addAll(new MoveTo(x, yOrigin), new LineTo(x, yOrigin + 24));
        line.getStyleClass().add(styleClass);
        return line;
    }

    private Path getCurvedLine(double x, boolean isLastSibling, String styleClass) {
        Path curvedLine = new Path();
        double yOrigin = treeCell.isViewLineage() ? treeCell.getHeight() - 24 : 0;
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
