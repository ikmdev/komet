package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorControl;
import dev.ikm.komet.controls.KLConceptNavigatorTreeCell;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.ikm.komet.controls.ConceptNavigatorModel.STATE;
import static dev.ikm.komet.controls.ConceptNavigatorModel.PS_STATE;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<ConceptNavigatorModel> {

    private final Label header;
    private ConceptNavigatorVirtualFlow virtualFlow;
    private Group sheet;

    public KLConceptNavigatorTreeViewSkin(TreeView<ConceptNavigatorModel> treeView) {
        super(treeView);
        header = new Label();
        header.getStyleClass().add("concept-header");
        header.textProperty().bind(((KLConceptNavigatorControl) treeView).headerProperty());
        header.visibleProperty().bind(header.textProperty().isNotEmpty());
        header.managedProperty().bind(header.visibleProperty());

        getChildren().add(header);
    }

    @Override
    protected VirtualFlow<TreeCell<ConceptNavigatorModel>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }


    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        if (header.isVisible()) {
            double titleHeight = header.prefHeight(contentWidth);
            super.layoutChildren(contentX, contentY + titleHeight, contentWidth, contentHeight - titleHeight);
            header.resizeRelocate(contentX, contentY, contentWidth, titleHeight);
        } else {
            super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        }
    }

    private Group getSheet() {
        if (sheet == null) {
            sheet = (Group) virtualFlow.lookup(".sheet");
        }
        return sheet;
    }

    public void unselectAllItems() {
        unmarkAllItems(STATE.SELECTED);
    }

    public void unhoverAllItems() {
        unmarkAllItems(STATE.LONG_HOVER);
    }

    private void unmarkAllItems(STATE state) {
        getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .forEach(KLConceptNavigatorTreeCell::unselectItem);
        List<Integer> statesBits = PS_STATE.getStatesBitRange(state);
        iterateTree(getSkinnable().getRoot(), item -> {
            item.getValue().getBitSet().clear(statesBits.getFirst(), statesBits.getLast());
            markCellDirty(item);
        });
        virtualFlow.requestLayout();
    }

    public void selectAllAncestors(TreeItem<ConceptNavigatorModel> child) {
        markAllAncestors(child, true);
    }

    public void hoverAllAncestors(TreeItem<ConceptNavigatorModel> child) {
        markAllAncestors(child, false);
    }

    private void markAllAncestors(TreeItem<ConceptNavigatorModel> child, boolean select) {
        TreeItem<ConceptNavigatorModel> treeItem = child;
        while (treeItem != null) {
            // for each ancestor (including starting one)
            if (treeItem == child) {
                if (!select) {
                    // mark current item as long-hovered
                    treeItem.getValue().getBitSet().set(PS_STATE.LONG_HOVER.getBit());
                }
            } else {
                // mark ancestor as selected/long-hovered
                treeItem.getValue().getBitSet().set(select ? PS_STATE.BORDER_SELECTED.getBit() : PS_STATE.BORDER_LONG_HOVER.getBit());
            }
            // show curved-line
            treeItem.getValue().getBitSet().set(select ? PS_STATE.CURVED_LINE_SELECTED.getBit() : PS_STATE.CURVED_LINE_LONG_HOVER.getBit());
            markCellDirty(treeItem);

            int level = getSkinnable().getTreeItemLevel(treeItem) - 2;
            if (level >= 0) {
                // for each previous sibling of this ancestor:
                TreeItem<ConceptNavigatorModel> previousSibling = treeItem.previousSibling();
                while (previousSibling != null) {
                    // and all expanded descendants if these are expanded
                    applyPseudoClassState(previousSibling, select, level);
                    previousSibling = previousSibling.previousSibling();
                }
            }
            treeItem = treeItem.getParent();
        }
        virtualFlow.requestLayout();

        // debug:
//        printTree(getSkinnable().getRoot(), false);

    }

    private void applyPseudoClassState(TreeItem<ConceptNavigatorModel> treeItem, boolean select, int level) {
        treeItem.getValue().getBitSet().set(select ? PS_STATE.LINE_I_SELECTED.getBit() + level : PS_STATE.LINE_I_LONG_HOVER.getBit() + level);
        markCellDirty(treeItem);
        if (treeItem.isExpanded()) {
            treeItem.getChildren().forEach(i -> applyPseudoClassState(i, select, level));
        }
    }

    private void markCellDirty(TreeItem<ConceptNavigatorModel> treeItem) {
        getCellForTreeItem(treeItem).ifPresent(KLConceptNavigatorTreeCell::markCellDirty);
    }

    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(TreeItem<ConceptNavigatorModel> treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    private void iterateTree(TreeItem<ConceptNavigatorModel> treeItem, Consumer<TreeItem<ConceptNavigatorModel>> consumer){
        for (TreeItem<ConceptNavigatorModel> child : treeItem.getChildren()) {
            consumer.accept(child);
            if (!child.isLeaf()) {
                iterateTree(child, consumer);
            }
        }
    }

    private void printTree(TreeItem<ConceptNavigatorModel> treeItem, boolean printAll) {
        for (TreeItem<ConceptNavigatorModel> child : treeItem.getChildren()) {
            if (child.isLeaf()) {
                if (printAll || !child.getValue().getBitSet().isEmpty()) {
                    System.out.println("-".repeat(getSkinnable().getTreeItemLevel(child)) + " " + child.getValue());
                }
            } else {
                if (printAll || !child.getValue().getBitSet().isEmpty()) {
                    System.out.println("+".repeat(getSkinnable().getTreeItemLevel(child)) + " " + child.getValue());
                }
                printTree(child, printAll);
            }
        }
    }

}
