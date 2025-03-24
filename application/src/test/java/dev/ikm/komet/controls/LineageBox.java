package dev.ikm.komet.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static dev.ikm.komet.controls.ConceptNavigatorUtils.getAllParents;
import static dev.ikm.komet.controls.ConceptNavigatorUtils.getFartherLevel;
import static dev.ikm.komet.controls.ConceptNavigatorUtils.getSecondaryParents;

public class LineageBox extends ScrollPane {

    private static final PseudoClass ROOT_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("root-lineage");
    private static final PseudoClass COLLAPSED_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed-lineage");
    private static final PseudoClass HAS_SIBLINGS_PSEUDO_CLASS = PseudoClass.getPseudoClass("siblings-lineage");
    private static final int INDENTATION_PER_LEVEL = 8; // pixels
    private static final int MIN_INDENTATION = INDENTATION_PER_LEVEL * 5; // the most top-left item will have 5 * 8 = 40 pixels

    private int invertedTreeMaxDepth;

    private final VBox root;

    public LineageBox() {

        root = new VBox();
        root.getStyleClass().add("lineage-box");

        setFitToWidth(true);
        setContent(root);
        getStyleClass().add("lineage-pane");
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
        invertedTreeMaxDepth = -1;
        if (childItem != null && childItem.getValue() != null) {
            Navigator navigator = getNavigator();
            invertedTreeMaxDepth = getFartherLevel(childItem.getValue().nid(), navigator);
            // primary parent under current tree lineage
            ConceptNavigatorTreeItem primaryParentItem = (ConceptNavigatorTreeItem) childItem.getParent();
            if (primaryParentItem != null) {
                // list of secondary parents for the concept child, different from the primary parent item
                List<InvertedTree.ConceptItem> secondaryParents =
                        getSecondaryParents(childItem.getValue().nid(), primaryParentItem.getValue().nid(), navigator);
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
            int currentLevel = invertedTree.getLevel();
            int spacerWidth = INDENTATION_PER_LEVEL * (invertedTreeMaxDepth - currentLevel) + MIN_INDENTATION;
            spacer.setMinSize(spacerWidth, 1);
            spacer.setPrefSize(spacerWidth, 1);
            spacer.setMaxSize(spacerWidth, 1);

            IconRegion iconRegion = new IconRegion("icon");
            Pane regionPane = new Pane(iconRegion);
            regionPane.getStyleClass().add("pane");

            Label label = getConceptLabel(treeItem);
            label.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (label.getSkin() != null) {
                        String description = label.getText();
                        Text textNode = (Text) label.lookup(".text");
                        textNode.textProperty().subscribe((_, t) ->
                                label.setTooltip(t != null && !description.equals(t) ? new Tooltip(description) : null));
                        label.skinProperty().removeListener(this);
                    }
                }
            });
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
                if (currentLevel == 1 && !allSiblings.isEmpty()) {
                    allSiblings.removeFirst(); // remove primary parent
                }
                boolean hasMultipleParents = allParents.size() > 1;
                boolean hasSiblings = allSiblings.size() > 1;
                int currentIndex = allSiblings.indexOf(treeItem);
                boolean firstSibling = currentIndex == 0;
                boolean lastSibling = currentIndex == allSiblings.size() - 1;
                String style1 = "", style2 = "", style3 = "";
                if (currentLevel == 1) {
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
                pseudoClassStateChanged(HAS_SIBLINGS_PSEUDO_CLASS, hasSiblings);

                if (hasSiblings) {
                    int counter = 0;
                    for (InvertedTree.ConceptItem sibling : allSiblings) {
                        if (sibling.equals(treeItem)) {
                            if (counter > 0) {
                                label.setText(label.getText() + " (#" + counter + ")");
                            }
                            break;
                        } else {
                            if (sibling.description().equals(treeItem.description())) {
                                counter++;
                            }
                        }
                    }
                }
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
