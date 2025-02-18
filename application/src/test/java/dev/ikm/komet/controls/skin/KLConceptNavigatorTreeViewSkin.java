package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.KLConceptNavigatorControl;
import dev.ikm.komet.controls.KLConceptNavigatorTreeCell;
import javafx.animation.PauseTransition;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<String> {

    private static final int MAX_LEVEL = 10;

    private static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-long-hover");
    private static final PseudoClass BORDER_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-long-hover");
    private static final PseudoClass BORDER_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-selected");

    private static final PseudoClass CURVED_LINE_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-long-hover");
    private static final PseudoClass CURVED_LINE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-selected");
    public static final PseudoClass[] LINE_I_LONG_HOVER_PSEUDO_CLASS;
    public static final PseudoClass[] LINE_I_SELECTED_PSEUDO_CLASS;
    static {
        LINE_I_LONG_HOVER_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        LINE_I_SELECTED_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; i++) {
            LINE_I_LONG_HOVER_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-long-hover-" + i);
            LINE_I_SELECTED_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-selected-" + i);
        }
    }

    private final Label header;
    private ConceptNavigatorVirtualFlow virtualFlow;
    private Group sheet;

    public KLConceptNavigatorTreeViewSkin(TreeView<String> treeView) {
        super(treeView);
        header = new Label();
        header.getStyleClass().add("concept-header");
        header.textProperty().bind(((KLConceptNavigatorControl) treeView).headerProperty());

        getChildren().add(header);
    }

    @Override
    protected VirtualFlow<TreeCell<String>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        layoutInArea(header, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.TOP);
    }

    private Group getSheet() {
        if (sheet == null) {
            sheet = (Group) virtualFlow.lookup(".sheet");
        }
        return sheet;
    }

    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(TreeItem<String> treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    public void selectItem(TreeItem<String> selectedItem) {
        PauseTransition p = new PauseTransition(Duration.seconds(0.05));
        p.setOnFinished(f -> {
            // restore selection
            selectAllAncestors(selectedItem);
        });
        p.playFromStart();
    }

    public void unselectAllItems() {
        unmarkAllItems("selected");
    }

    public void unhoverAllItems() {
        unmarkAllItems("long-hover");
    }

    private void unmarkAllItems(String text) {
        getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .forEach(c -> {
                    c.getPseudoClassStates().stream()
                            .filter(p -> p.getPseudoClassName().startsWith("cn-") &&
                                    p.getPseudoClassName().contains(text))
                            .forEach(p -> c.pseudoClassStateChanged(p, false));
                    c.unselectItem();
                });
    }

    public void selectAllAncestors(TreeItem<String> child) {
        markAllAncestors(child, true);
    }

    public void hoverAllAncestors(TreeItem<String> child) {
        if (getSkinnable().getSelectionModel().getSelectedItem() != null) {
            // bail out if there is a selection
            return;
        }
        markAllAncestors(child, false);
    }

    private void markAllAncestors(TreeItem<String> child, boolean select) {
        TreeItem<String> parent = child;
        while (parent != null) {
            getCellForTreeItem(parent)
                    .ifPresent(c -> {
                        int level = getSkinnable().getTreeItemLevel(c.getTreeItem()) - 2;
                        // for each ancestor (including current cell)
                        if (c.getTreeItem() == child) {
                            if (!select) {
                                c.pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, true); // mark current cell as long-hovered
                            }
                        } else {
                            c.pseudoClassStateChanged(select ? BORDER_SELECTED_PSEUDO_CLASS : BORDER_LONG_HOVER_PSEUDO_CLASS, true); // mark ancestor as selected/long-hovered
                        }
                        c.pseudoClassStateChanged(select ? CURVED_LINE_SELECTED_PSEUDO_CLASS : CURVED_LINE_LONG_HOVER_PSEUDO_CLASS, true); // show curved-line
                        if (level >= 0) {
                            // for each previous sibling of this ancestor:
                            TreeItem<String> previousSibling = c.getTreeItem().previousSibling();
                            AtomicReference<KLConceptNavigatorTreeCell> currentSibling = new AtomicReference<>(getCellForTreeItem(c.getTreeItem()).orElse(null));
                            while (previousSibling != null) {
                                getCellForTreeItem(previousSibling)
                                        .ifPresentOrElse(p -> {
                                            currentSibling.set(p);
                                            // and all expanded descendants if these are expanded
                                            applyPseudoClassState(p, select, level);
                                        }, () -> {
                                            // the ancestor is outside the viewport, we just get all the cells above the current cell until first one visible
                                            if (currentSibling.get() != null) {
                                                virtualFlow.applyToAllVisibleCellsBefore(currentSibling.get(),
                                                        cell -> cell.pseudoClassStateChanged(select ?
                                                                LINE_I_SELECTED_PSEUDO_CLASS[level] : LINE_I_LONG_HOVER_PSEUDO_CLASS[level], true));
                                            }
                                        });
                                previousSibling = previousSibling.previousSibling();
                            }
                        }
                    });
            parent = parent.getParent();
        }
        virtualFlow.requestLayout();
    }

    private void applyPseudoClassState(KLConceptNavigatorTreeCell treeCell, boolean select, int level) {
        treeCell.pseudoClassStateChanged(select ? LINE_I_SELECTED_PSEUDO_CLASS[level] : LINE_I_LONG_HOVER_PSEUDO_CLASS[level], true);
        if (treeCell.getTreeItem().isExpanded()) {
            treeCell.getTreeItem().getChildren().forEach(i ->
                    getCellForTreeItem(i).ifPresent(t -> applyPseudoClassState(t, select, level)));
        }
    }

}
