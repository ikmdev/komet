package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeCellSkin;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class KLConceptNavigatorTreeCell extends TreeCell<String> {
    private static final int MAX_LEVEL = 10;
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass LEAF_PSEUDO_CLASS = PseudoClass.getPseudoClass("leaf");

    private static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("line-long-hover");
    private static final PseudoClass LINE_CURVED_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("line-curved-selected");
    public static final PseudoClass[] LINE_SELECTED_PSEUDO_CLASS;
    static {
        LINE_SELECTED_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        for (int i = 0; i < LINE_SELECTED_PSEUDO_CLASS.length; i++) {
            LINE_SELECTED_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("line-selected-" + i);
        }
    }

    private final Label label;
    private final HBox box;
    private final KLConceptNavigatorControl treeView;
    private KLConceptNavigatorTreeCellSkin myTreeCellSkin;

    private PauseTransition hoverTransition;
    private Subscription subscription;

    public KLConceptNavigatorTreeCell(KLConceptNavigatorControl treeView) {
        this.treeView = treeView;
        IconRegion disclosureIconRegion = new IconRegion();
        StackPane disclosurePane = new StackPane(disclosureIconRegion);
        disclosurePane.getStyleClass().add("region");
        disclosurePane.setOnMouseClicked(e -> {
            if (getTreeItem() != null) {
                getTreeItem().setExpanded(!getTreeItem().isExpanded());
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

        treeItemProperty().subscribe(tree -> {
            if (tree != null) {
                disclosureIconRegion.getStyleClass().setAll("icon", tree.isLeaf() ? "leaf" : "disclosure");
                subscription = tree.expandedProperty().subscribe(e -> disclosurePane.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, e));
                subscription = subscription.and(tree.leafProperty().subscribe(l -> disclosurePane.pseudoClassStateChanged(LEAF_PSEUDO_CLASS, l)));
                subscription = subscription.and(box.hoverProperty().subscribe(h -> {
                    unselectAllItems();
                    if (h) {
                        hoverTransition = new PauseTransition(Duration.seconds(0.5));
                        hoverTransition.setOnFinished(e -> selectAllAncestors());
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

    private void unselectItem() {
        if (hoverTransition != null) {
            hoverTransition.stop();
            hoverTransition = null;
        }
    }

    private void cleanup() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        unselectAllItems();
        getChildren().removeIf(Path.class::isInstance);
    }

    @Override
    public void updateIndex(int newIndex) {
        super.updateIndex(newIndex);
        if (newIndex == -1) {
            cleanup();
        }
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            label.setText(item);
            setGraphic(box);
        } else {
            setGraphic(null);
        }
        updateConnections();
    }

    private void updateConnections() {
        List<Path> oldPaths = getChildren().stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .toList();
        TreeItem<String> treeItem = getTreeItem();
        if (treeView == null || myTreeCellSkin == null || treeItem == null) return;
        int level = treeView.getTreeItemLevel(treeItem) - 1;
        double indent = myTreeCellSkin.getIndent();
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < level; i++) {
            double x = 10.5 + indent * i;
            if (i < level - 1) {
                TreeItem<String> ancestor = getAncestor(treeItem, i + 1);
                if (ancestor.nextSibling() != null) {
                    paths.add(getLine(x, "line"));
                    paths.add(getLine(x, "selected-line-" + i));
                }
            } else {
                paths.add(getCurvedLine(x, treeItem.nextSibling() == null, "line"));
                paths.add(getLine(x, "selected-line-" + i));
                paths.add(getCurvedLine(x, true, "selected-curved-line"));
            }
        }
        if (!oldPaths.equals(paths)) {
            getChildren().removeAll(oldPaths);
            getChildren().addAll(paths);
        }
    }

    private TreeItem<String> getAncestor(TreeItem<String> treeItem, int level) {
        if (treeItem == null) {
            return null;
        }
        TreeItem<String> ancestor = treeItem.getParent();
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

    private void unselectAllItems() {
        treeView.getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .forEach(c -> {
                    c.getPseudoClassStates().stream()
                            .filter(p -> p.getPseudoClassName().startsWith("line-"))
                            .forEach(p -> c.pseudoClassStateChanged(p, false));
                    c.unselectItem();
                });
    }

    private void selectAllAncestors() {
        TreeItem<String> parent = getTreeItem(); // item that was long-hovered
        while (parent != null) {
            getCellForTreeItem(parent)
                    .ifPresent(c -> {
                        int level = treeView.getTreeItemLevel(c.getTreeItem()) - 2;
                        // for each ancestor
                        c.pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, true); // mark as long-hovered
                        c.pseudoClassStateChanged(LINE_CURVED_SELECTED_PSEUDO_CLASS, true); // show curved-line
                        if (level >= 0) {
                            // for each previous sibling of this ancestor:
                            TreeItem<String> previousSibling = c.getTreeItem().previousSibling();
                            AtomicReference<KLConceptNavigatorTreeCell> currentSibling = new AtomicReference<>(getCellForTreeItem(c.getTreeItem()).orElse(null));
                            while (previousSibling != null) {
                                getCellForTreeItem(previousSibling)
                                        .ifPresentOrElse(p -> {
                                            currentSibling.set(p);
                                            // and all expanded descendants if these are expanded
                                            applyPseudoClassState(p, level);
                                        }, () -> {
                                            // the ancestor is outside the viewport, we just get all the cells above the current cell until first one visible
                                            if (currentSibling.get() != null) {
                                                treeView.getConceptNavigatorVirtualFlow().applyToAllVisibleCellsBefore(currentSibling.get(),
                                                        cell -> cell.pseudoClassStateChanged(LINE_SELECTED_PSEUDO_CLASS[level], true));
                                            }
                                        });
                                previousSibling = previousSibling.previousSibling();
                            }
                        }
                    });
            parent = parent.getParent();
        }
        treeView.getConceptNavigatorVirtualFlow().requestLayout();
    }

    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(TreeItem<String> treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return treeView.getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    private void applyPseudoClassState(KLConceptNavigatorTreeCell treeCell, int level) {
        treeCell.pseudoClassStateChanged(LINE_SELECTED_PSEUDO_CLASS[level], true);
        if (treeCell.getTreeItem().isExpanded()) {
            treeCell.getTreeItem().getChildren().forEach(i ->
                    getCellForTreeItem(i).ifPresent(t -> applyPseudoClassState(t, level)));
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        myTreeCellSkin = new KLConceptNavigatorTreeCellSkin(this);
        return myTreeCellSkin;
    }

}
