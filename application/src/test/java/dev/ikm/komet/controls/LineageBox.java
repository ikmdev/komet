package dev.ikm.komet.controls;

import com.pixelduke.control.skin.ConsciousScrollPaneSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static dev.ikm.komet.controls.ConceptNavigatorUtils.getAllParents;
import static dev.ikm.komet.controls.ConceptNavigatorUtils.getSecondaryParents;

public class LineageBox extends ScrollPane {

    private static final PseudoClass ROOT_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("root-lineage");
    private static final PseudoClass COLLAPSED_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed-lineage");
    private final VBox root;

    public LineageBox() {

        root = new VBox();
        root.getStyleClass().add("lineage-box");

        setFitToWidth(true);
        setContent(root);
        getStyleClass().add("lineage-pane");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LineageBoxSkin(this);
    }

    private class LineageBoxSkin extends ConsciousScrollPaneSkin {
        private final StackPane closePane;

        public LineageBoxSkin(LineageBox box) {
            super(box);

            IconRegion closeIconRegion = new IconRegion("icon", "close");
            closePane = new StackPane(closeIconRegion);
            closePane.getStyleClass().addAll("region", "close");
            closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                ConceptNavigatorTreeItem concept = getConcept();
                if (concept != null) {
                    concept.setViewLineage(false);
                    concept.getInvertedTree().reset();
                    setConcept(null);
                }
                e.consume();
            });
            closePane.setManaged(false);
            getChildren().add(closePane);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);

            double w2 = closePane.prefWidth(w);
            double h2 = closePane.prefHeight(h);
            closePane.resizeRelocate(w - w2 - 4, 4, w2, h2);
            closePane.toFront();
        }
    }

    // conceptProperty
    private final ObjectProperty<ConceptNavigatorTreeItem> conceptProperty = new SimpleObjectProperty<>(this, "concept") {

        @Override
        protected void invalidated() {
            initialize();
        }
    };
    public final ObjectProperty<ConceptNavigatorTreeItem> conceptProperty() {
       return conceptProperty;
    }
    public final ConceptNavigatorTreeItem getConcept() {
       return conceptProperty.get();
    }
    public final void setConcept(ConceptNavigatorTreeItem value) {
        conceptProperty.set(value);
    }

    // navigatorProperty
    private final ObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>(this, "navigator");
    public final ObjectProperty<Navigator> navigatorProperty() {
       return navigatorProperty;
    }
    public final Navigator getNavigator() {
       return navigatorProperty.get();
    }
    public final void setNavigator(Navigator value) {
        navigatorProperty.set(value);
    }

    private void initialize() {
        root.getChildren().removeIf(HBox.class::isInstance);
        ConceptNavigatorTreeItem childItem = getConcept();
        if (childItem != null && childItem.getValue() != null) {
            // primary parent under current tree lineage
            ConceptNavigatorTreeItem primaryParentItem = (ConceptNavigatorTreeItem) childItem.getParent();
            if (primaryParentItem != null) {
                // list of secondary parents for the concept child, different from the primary parent item
                List<InvertedTree.ConceptItem> secondaryParents =
                        getSecondaryParents(childItem.getValue().nid(), primaryParentItem.getValue().nid(), getNavigator());
                // for the concept child, add all its direct secondary parents, all collapsed initially
                InvertedTree invertedTree = getConcept().getInvertedTree();
                for (InvertedTree.ConceptItem extraParentItem : secondaryParents) {
                    root.getChildren().addFirst(new ParentHBox(root, invertedTree, extraParentItem));
                    // restore existing inverted tree, needed after scrolling and cell reuse, for instance
                    invertedTree.iterateTree(invertedTree.getInvertedTree(extraParentItem),
                            tree -> {
                                // iteration goes from parent to last child, then back to next parent, so
                                // it is safe to insert the boxes at position 0
                                ParentHBox parentHBox = new ParentHBox(root, tree, tree.item);
                                root.getChildren().addFirst(parentHBox);
                            });
                }
                requestLayout();
//                invertedTree.printTree();
            }
        }
    }

    private class ParentHBox extends HBox {

        private final VBox lineageBoxRoot;
        private final InvertedTree invertedTree;

        public ParentHBox(VBox lineageBoxRoot, InvertedTree parentTree, InvertedTree.ConceptItem treeItem) {
            this.lineageBoxRoot = lineageBoxRoot;
            this.invertedTree = parentTree.contains(treeItem) ? parentTree.getInvertedTree(treeItem) : parentTree.addChild(treeItem);

            Region spacer = new Region();
            int level = invertedTree.getLevel();
            int spacerWidth = 128 - 8 * level;
            spacer.setMinSize(spacerWidth, 1);
            spacer.setPrefSize(spacerWidth, 1);
            spacer.setMaxSize(spacerWidth, 1);

            IconRegion iconRegion = new IconRegion("icon");
            StackPane regionPane = new StackPane(iconRegion);
            regionPane.getStyleClass().add("region");

            Label label = getConceptLabel(treeItem);
            HBox.setHgrow(label, Priority.ALWAYS);

            getChildren().addAll(spacer, regionPane, label);

            List<InvertedTree.ConceptItem> allParents = getAllParents(treeItem.nid(), getNavigator());
            boolean isRoot = allParents.isEmpty();
            pseudoClassStateChanged(ROOT_LINEAGE_PSEUDO_CLASS, isRoot);
            if (isRoot) { // item is root
                iconRegion.getStyleClass().add("root-angle");
            } else {
                List<InvertedTree.ConceptItem> allSiblings =
                        invertedTree.parent != null && invertedTree.parent.item != null ?
                                getAllParents(invertedTree.parent.item.nid(), getNavigator()) : new ArrayList<>();
                if (level == 1 && !allSiblings.isEmpty()) {
                    allSiblings.removeFirst(); // remove primary parent
                }
                boolean hasMultipleParents = allParents.size() > 1;
                boolean hasSiblings = allSiblings.size() > 1;
                int currentIndex = allSiblings.indexOf(treeItem);
                boolean firstSibling = currentIndex == 0;
                boolean lastSibling = currentIndex == allSiblings.size() - 1;
                String style1 = "", style2 = "", style3 = "";
                if (level == 1) {
                    style1 = !hasSiblings || lastSibling ? "angle" : "line";
                    style2 = "circle";
                    style3 = "line";
                } else {
                    style1 = hasSiblings && !lastSibling ? "line" : "angle";
                    style2 = hasSiblings || hasMultipleParents ? "circle" : "angle";
                    style3 = hasSiblings && !firstSibling ? "line" : "angle";
                }

                iconRegion.getStyleClass().add(style1 + "-" + style2 + "-" + style3);
                pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, invertedTree.isLeaf());
            }
            getStyleClass().add("lineage-hbox");
        }

        private Label getConceptLabel(InvertedTree.ConceptItem treeItem) {
            Label label = new Label(treeItem.description());
            label.setOnMouseClicked(e -> {
                ParentHBox currentHBox = (ParentHBox) label.getParent();
                int currentIndex = lineageBoxRoot.getChildren().indexOf(currentHBox);
                if (currentHBox.invertedTree.isLeaf()) {
                    // item is collapsed, add all ancestors and expanse
                    getAllParents(treeItem.nid(), getNavigator()).stream()
                            .filter(item -> !invertedTree.contains(item))
                            .forEach(item -> {
                                ParentHBox parentHBox = new ParentHBox(lineageBoxRoot, invertedTree, item);
                                lineageBoxRoot.getChildren().add(currentIndex, parentHBox);
                            });
                    currentHBox.pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, false);
                } else {
                    // item is expanded, remove ancestors and collapse
                    lineageBoxRoot.getChildren().remove(Math.max(0, currentIndex - invertedTree.countTotalDescendants()), currentIndex);
                    currentHBox.invertedTree.reset();
                    currentHBox.pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, true);
                }
            });
            label.getStyleClass().add("lineage-label");
            return label;
        }
    }
}
