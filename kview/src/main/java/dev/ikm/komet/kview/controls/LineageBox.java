package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.controls.ConceptNavigatorUtils.getAllParents;
import static dev.ikm.komet.kview.controls.ConceptNavigatorUtils.getFartherLevel;
import static dev.ikm.komet.kview.controls.ConceptNavigatorUtils.getSecondaryParents;

/**
 * <p>The LineageBox is a customized {@link ScrollPane}, and provides a scrolled, clipped viewport
 * of its content.
 * </p>
 * <p>The LineageBox is the control that renders an {@link InvertedTree} for a given
 * {@link ConceptNavigatorTreeItem} that has two or more parents. This happens whenever a
 * concept appears in the regular {@link KLConceptNavigatorControl} in two or more lineages.
 * </p>
 * <p>For instance, if D is part of the lineages A-B-C-D and A-E-D, then C and E are its parents:</p>
 * <pre><code>
 *     A --- B --- C -- D
 *      \
 *       E --- D
 * </code></pre>
 * <p>From the point of view of D, if D is selected in the treeView from the lineage is A-E-D,
 * then that is the main lineage and E is its primary parent, and C is the secondary parent.
 * That means that the first element of its LineageBox will be C, the next element would be its
 * grandparent B, and finally the grand-grandparent A:
 * <pre><code>
 *    \ A
 *     \ B
 *      \ C
 *      |
 *        D
 * </code></pre>
 * </p>
 * <p>The control's content is a {@link VBox} that holds one {@link ParentHBox} per item of the
 * inverted tree, starting by the secondary parents of the selected concept, resembling a TreeView, but
 * there is no TreeView control.
 * </p>
 * <p>For a selected {@link ConceptNavigatorTreeItem}, when
 * {@link ConceptNavigatorTreeItem#viewLineageProperty()} is set to true, the LineageBox is displayed
 * as node of the {@link KLConceptNavigatorTreeCell} that holds the concept.
 * </p>
 */
public class LineageBox extends ScrollPane {

    /**
     * PseudoClasses
     */
    private static final PseudoClass ROOT_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("root-lineage");
    private static final PseudoClass COLLAPSED_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed-lineage");
    private static final PseudoClass HAS_SIBLINGS_PSEUDO_CLASS = PseudoClass.getPseudoClass("siblings-lineage");

    /**
     * Constants
     */
    private static final int INDENTATION_PER_LEVEL = 8; // pixels
    private static final int MIN_INDENTATION = INDENTATION_PER_LEVEL * 5; // the most top-left item will have 5 * 8 = 40 pixels

    private int invertedTreeMaxDepth;

    private final VBox root;

    /**
     * <p>Creates a {@link LineageBox} instance, which initially is just an
     * empty {@link VBox} as content of the {@link ScrollPane}.
     * </p>
     */
    public LineageBox() {

        root = new VBox();
        root.getStyleClass().add("lineage-box");

        setFitToWidth(true);
        setContent(root);
        getStyleClass().add("lineage-pane");
    }

    /**
     * <p>An object property that holds the {@link ConceptNavigatorTreeItem} for which the
     * {@link LineageBox} will be created.
     * </p>
     */
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

    /**
     * <p>An object property that holds the {@link Navigator}.
     * </p>
     */
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

    /**
     * <p>For a given {@link ConceptNavigatorTreeItem}, gets it primary parent, and the list of
     * secondary parents. For each of these, a {@link ParentHBox} instance is created and
     * added to the root {@link VBox#getChildren()}.
     * </p>
     * <p>Note: Since the {@link LineageBox} is part of a cell, due to scrolling and cell reuse,
     * this method can be called when the inverted tree was already showing and expanded up until
     * some level, so {@link InvertedTree#iterateTree(InvertedTree, Consumer)} is called to
     * rebuild all existing {@link ParentHBox} objects.
     * </p>
     */
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

    /**
     * <p>A ParentHBox is an {@link HBox} that contains
     * - a spacer region, with a width strictly set based on the indentation level.
     * - an icon
     * - a concept label
     * </p>
     * <p>The icon is set via style classes that are set based on the following:
     * - is root
     * - has multiple parents
     * - has siblings, is first sibling, is last sibling
     * - the current level of indentation
     * </p>
     * <p>PseudoClasses are set based on the following:
     * - is root
     * - is leaf
     * - has siblings
     * </p>
     */
    private class ParentHBox extends HBox {

        private final VBox lineageBoxRoot;
        private final InvertedTree invertedTree;

        /**
         * <p>Create a ParentHBox instance.
         * </p>
         * @param lineageBoxRoot The root {@link VBox}.
         * @param parentTree the {@link InvertedTree}
         * @param treeItem the {@link InvertedTree.ConceptItem} of the parent {@link InvertedTree}.
         */
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

        /**
         * <p>Create a {@link Label} for the concept
         * </p>
         * <p>A tooltip is set in case the label text is too long and it gets truncated.
         * </p>
         * <p>A mouse click listener is added to mimic the tree expand/collapse gesture.
         * When the item is collapsed, new {@link ParentHBox} are added, with the ancestors.
         * When the item is expanded, the existing ancestors are removed.
         * </p>
         * @param treeItem the {@link InvertedTree.ConceptItem}
         * @return a {@link Label}
         */
        private Label getConceptLabel(InvertedTree.ConceptItem treeItem) {
            Label label = new Label(treeItem.description()) {
                @Override
                protected Skin<?> createDefaultSkin() {
                    Skin<?> skin = super.createDefaultSkin();
                    String description = getText();
                    Text textNode = (Text) lookup(".text");
                    textNode.textProperty().subscribe((_, t) ->
                            setTooltip(t != null && !description.equals(t) ? new Tooltip(description) : null));
                    return skin;
                }
            };
            label.setOnMouseClicked(e -> {
                e.consume();
                if (e.getButton() != MouseButton.PRIMARY) {
                    return;
                }
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
