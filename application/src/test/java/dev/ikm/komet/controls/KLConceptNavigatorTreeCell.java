package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeCellSkin;
import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static dev.ikm.komet.controls.ConceptNavigatorModel.PS_STATE;
import static dev.ikm.komet.controls.ConceptNavigatorModel.MAX_LEVEL;

public class KLConceptNavigatorTreeCell extends TreeCell<ConceptNavigatorModel> {

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass LEAF_PSEUDO_CLASS = PseudoClass.getPseudoClass("leaf");

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

    private final Label label;
    private final HBox box;
    private final KLConceptNavigatorControl treeView;
    private KLConceptNavigatorTreeViewSkin treeViewSkin;
    private KLConceptNavigatorTreeCellSkin myTreeCellSkin;

    private PauseTransition hoverTransition;
    private Subscription subscription;

    public KLConceptNavigatorTreeCell(KLConceptNavigatorControl treeView) {
        this.treeView = treeView;

        IconRegion disclosureIconRegion = new IconRegion();
        StackPane disclosurePane = new StackPane(disclosureIconRegion);
        disclosurePane.getStyleClass().add("region");
        disclosurePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (getTreeItem() != null) {
                getTreeItem().setExpanded(!getTreeItem().isExpanded());
                e.consume();
            }
        });
        IconRegion selectIconRegion = new IconRegion("icon", "select");
        StackPane selectPane = new StackPane(selectIconRegion);
        selectPane.getStyleClass().addAll("region", "select");

        IconRegion treeIconRegion = new IconRegion("icon", "tree");
        StackPane treePane = new StackPane(treeIconRegion);
        treePane.getStyleClass().addAll("region", "tree");

        label = new Label();
        label.getStyleClass().add("concept-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box = new HBox(disclosurePane, label, spacer, selectPane, treePane);
        box.getStyleClass().add("cell-box");

        this.treeView.skinProperty().subscribe(skin -> this.treeViewSkin = (KLConceptNavigatorTreeViewSkin) skin);
        treeItemProperty().subscribe(tree -> {
            if (tree != null) {
                disclosureIconRegion.getStyleClass().setAll("icon", tree.isLeaf() ? "leaf" : "disclosure");
                subscription = tree.expandedProperty().subscribe(e -> disclosurePane.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, e));
                subscription = subscription.and(tree.leafProperty().subscribe(l -> disclosurePane.pseudoClassStateChanged(LEAF_PSEUDO_CLASS, l)));
                subscription = subscription.and(box.hoverProperty().subscribe(h -> {
                    if (h) {
                        treeViewSkin.unhoverAllItems();
                        hoverTransition = new PauseTransition(Duration.seconds(0.5));
                        hoverTransition.setOnFinished(e -> treeViewSkin.hoverAllAncestors(getTreeItem()));
                        hoverTransition.playFromStart();
                    }
                }));
            } else {
                cleanup();
            }
        });

        disclosureNodeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (getDisclosureNode() != null) {
                    setDisclosureNode(null);
                    disclosureNodeProperty().removeListener(this);
                }
            }
        });
        setText(null);
    }

    public void unselectItem() {
        if (hoverTransition != null) {
            hoverTransition.stop();
            hoverTransition = null;
        }
    }

    public void cleanup() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        unselectItem();
        getChildren().removeIf(Path.class::isInstance);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
    }

    @Override
    public void updateIndex(int newIndex) {
        super.updateIndex(newIndex);
        if (newIndex == -1) {
            cleanup();
        }
    }

    private boolean itemDirty = false;
    public final void markCellDirty() {
        itemDirty = true;
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        if (itemDirty) {
            updateItem(getItem(), isEmpty());
            itemDirty = false;
        }
        super.layoutChildren();
    }

    @Override
    protected void updateItem(ConceptNavigatorModel item, boolean empty) {
        super.updateItem(item, empty);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
        if (item != null && !empty) {
            label.setText(item.getText());
            setGraphic(box);
            updateConnections();
            updateState(item.getBitSet());
        } else {
            setGraphic(null);
            cleanup();
        }
    }

    private void updateState(BitSet bitSet) {
        pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_SELECTED.getBit()));
        pseudoClassStateChanged(CURVED_LINE_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_LONG_HOVER.getBit()));
        pseudoClassStateChanged(CURVED_LINE_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_SELECTED.getBit()));
        for (int i = 0; i < Math.min(treeView.getTreeItemLevel(getTreeItem()) - 1, MAX_LEVEL); i++) {
            pseudoClassStateChanged(LINE_I_LONG_HOVER_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_LONG_HOVER.getBit() + i));
            pseudoClassStateChanged(LINE_I_SELECTED_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_SELECTED.getBit() + i));
        }
    }

    private void updateConnections() {
        TreeItem<ConceptNavigatorModel> treeItem = getTreeItem();
        if (treeView == null || myTreeCellSkin == null || treeItem == null) {
            return;
        }

        List<Path> oldPaths = getChildren().stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .toList();
        int level = treeView.getTreeItemLevel(treeItem) - 1;
        double indent = myTreeCellSkin.getIndent();
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < level; i++) {
            double x = 10.5 + indent * i;
            if (i < level - 1) {
                TreeItem<ConceptNavigatorModel> ancestor = getAncestor(treeItem, i + 1);
                if (ancestor.nextSibling() != null) {
                    paths.add(getLine(x, "dashed-line"));
                    paths.add(getLine(x, "solid-line-" + i));
                }
            } else {
                paths.add(getCurvedLine(x, treeItem.nextSibling() == null, "dashed-curved-line"));
                paths.add(getLine(x, "solid-line-" + i));
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
            if (treeView.getTreeItemLevel(ancestor) - 1 == level) {
                return ancestor;
            }
            ancestor = ancestor.getParent();
        }
        return null;
    }

    private Path getLine(double x, String styleClass) {
        Path line = new Path();
        line.getElements().addAll(new MoveTo(x, 0), new LineTo(x, 24));
        line.getStyleClass().add(styleClass);
        return line;
    }

    private Path getCurvedLine(double x, boolean isLastSibling, String styleClass) {
        Path curvedLine = new Path();
        if (isLastSibling) {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, 9),
                    new ArcTo(3, 3, 90, x + 3, 12, false, false)
            );
        } else {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, 24), new MoveTo(x, 9),
                    new ArcTo(3, 3, 90, x + 3, 12, false, false)
            );
        }
        curvedLine.getStyleClass().add(styleClass);
        return curvedLine;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        myTreeCellSkin = new KLConceptNavigatorTreeCellSkin(this);
        return myTreeCellSkin;
    }

}
