package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TreeItem;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.controls.KLConceptNavigatorControl.MAX_LEVEL;

/**
 * Utility class for the {@link KLConceptNavigatorControl} and related controls and components.
 */
public class ConceptNavigatorUtils {

    private static final String STYLE1 = """
            data:text/css,
            """;
    private static final String STYLE2 = ".tree-cell > .solid-line-#,";
    private static final String STYLE3 = """
            .tree-cell > .solid-curved-line {
                 -fx-stroke-width: 1;
                 visibility: hidden;
                 -fx-managed: false;
            }
            """;
    private static final String STYLE4 = ".tree-cell:cn-line-selected-# > .solid-line-#,";
    private static final String STYLE5 = """
            .tree-cell:cn-curved-line-selected > .solid-curved-line {
                 -fx-stroke: #5E5E5E;
                 -fx-stroke-width: 1;
                 visibility: visible;
                 -fx-managed: true;
            }
            """;
    private static final String STYLE6 = ".tree-cell:cn-line-long-hover-# > .solid-line-#,";
    private static final String STYLE7 = """
            .tree-cell:cn-curved-line-long-hover > .solid-curved-line {
                 -fx-stroke: #5896F6;
                 -fx-stroke-width: 2;
                 visibility: visible;
                 -fx-managed: true;
            }
            """;

    /**
     * <p>This is the content of a CSS stylesheet created on memory at runtime, with all the styles needed for the
     * connecting-lines that decorate the {@link KLConceptNavigatorControl}, which depend on
     * the {@link KLConceptNavigatorControl#MAX_LEVEL} value.
     * </p>
     */
    public static final String STYLE;
    static {
        StringBuilder builder = new StringBuilder(STYLE1);
        for (int i = 0; i < MAX_LEVEL; i++) {
            builder.append(STYLE2.replaceAll("#", "" + i)).append("\n");
        }
        builder.append(STYLE3);
        for (int i = 0; i < MAX_LEVEL; i++) {
            builder.append(STYLE4.replaceAll("#", "" + i)).append("\n");
        }
        builder.append(STYLE5);
        for (int i = 0; i < MAX_LEVEL; i++) {
            builder.append(STYLE6.replaceAll("#", "" + i)).append("\n");
        }
        builder.append(STYLE7);
        STYLE = builder.toString();
    }

    private ConceptNavigatorUtils() {}

    /**
     * <p>Creates a snapshot of a {@link ConceptTile} that can be used in a
     * drag and drop gesture.
     * </p>
     * @param tile the {@link ConceptTile}
     * @return a {@link WritableImage}
     */
    public static WritableImage getTileSnapshot(ConceptTile tile) {
        SnapshotParameters p = new SnapshotParameters();
        double scale = tile.getScene().getWindow().getOutputScaleY();
        p.setTransform(new Scale(scale, scale));
        return tile.snapshot(p, null);
    }

    private static int levelCounter = 0;
    private static int deepestNid;

    /**
     * <p>Recursive method that for a given nid, finds all of its parents, and for
     * each parent nid, repeats the process, until all ancestors are discovered. As
     * a result, the starting {@link InvertedTree} gets completed.
     * </p>
     * @param nid the nid of the concept
     * @param tree the {@link InvertedTree}
     * @param navigator the {@link Navigator} that holds the dataset
     */
    private static void addAllAncestors(int nid, InvertedTree tree, Navigator navigator) {
        getAllParents(nid, navigator).forEach(i -> addAllAncestors(i.nid(), tree.addChild(i), navigator));
    }

    /**
     * <p>Given a child nid, and a {@link Navigator}, this method finds all the possible
     * parents of this child.
     * </p>
     * @param childNid the nid of a given child
     * @param navigator the {@link Navigator} that holds the dataset
     * @return a list of {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     */
    static List<InvertedTree.ConceptItem> getAllParents(int childNid, Navigator navigator) {
        return getSecondaryParents(childNid, -1, navigator);
    }

    /**
     * <p>Given a child nid, and a {@link Navigator}, this method finds all the possible
     * parents of this child, but excludes the parent with primary nid, if set.
     * </p>
     * @param childNid the nid of a given child
     * @param primaryNid the nid of the primary parent, or -1 if not set
     * @param navigator the {@link Navigator} that holds the dataset
     * @return a list of {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     */
    static List<InvertedTree.ConceptItem> getSecondaryParents(int childNid, int primaryNid, Navigator navigator) {
        return new ArrayList<>(Arrays.stream(navigator.getParentNids(childNid)).boxed()
                .filter(nid -> nid != primaryNid)
                .map(nid -> new InvertedTree.ConceptItem(nid, childNid, Entity.getFast(nid).description()))
                .toList());
    }

    /**
     * <p>Utility method that builds and {@link InvertedTree} starting from
     * a given nid and a given {@link Navigator}, and finds its maximum depth.
     * </p>
     * @param nid the nid of the concept
     * @param navigator the {@link Navigator} that holds the dataset
     * @return the depth of the {@link InvertedTree}
     */
    static int getFartherLevel(int nid, Navigator navigator) {
        InvertedTree tree = buildInvertedTree(nid, navigator);
        return tree.getTreeDepth();
    }

    /**
     * <p>Builds an {@link InvertedTree} from a given nid and a {@link Navigator} with
     * a dataset
     * </p>
     * @param nid the nid of the concept
     * @param navigator the {@link Navigator} that holds the dataset
     * @return an {@link InvertedTree}
     */
    static InvertedTree buildInvertedTree(int nid, Navigator navigator) {
        ConceptFacade facade = Entity.getFast(nid);
        InvertedTree.ConceptItem item = new InvertedTree.ConceptItem(facade.nid(), facade.nid(), facade.description());
        InvertedTree tree = new InvertedTree(item);
        addAllAncestors(facade.nid(), tree, navigator);
        return tree;
    }

    /**
     * <p>Utility method that can be use for debugging purposes, as it prints out the inverted tree,
     * starting from the root, which is set at the farthest nid possible from the dataset,
     * and prints out the maximum level, the deepest concept (nid, and description).
     * </p>
     * <p> For instance, the following is the output of a call to this method from a given dataset:
     * <pre>
     * Tree =======
     * GrandChild_1(0)
     * - Child_1(1)
     * -- ConceptA(2)
     * --- Parent_1(3)
     * ---- GrandParent_1(4)
     * ----- GGrandParent_1(5)
     * ------ Scenarios(6)
     * ...
     * ---------- Tinkar Model concept(10)
     * ----------- Model concept(11)
     * ------------ Tinkar root concept(12)
     * ...
     * ----- GGrandParent_3(5)
     * ------ GGGrandParent_3(6)
     * ------- Scenarios(7)
     * -------- Tinkar Model concept(8)
     * --------- Model concept(9)
     * ---------- Tinkar root concept(10)
     * End Tree =======
     *
     *
     * Max level = 12, nid = -2147479877, description = GrandChild_1
     * </pre>
     * </p>
     *
     * @param rootNid the nid of the root
     * @param navigator the {@link Navigator} that holds the dataset
     */
    public static void getConceptNavigatorDepth(int rootNid, Navigator navigator) {
        levelCounter = 0;
        deepestNid = 0;
        getChildrenNid(rootNid, 0, navigator);

        System.out.println("\n\nTree =======");
        printInvertedTree(deepestNid, navigator);
        System.out.println("\n\nEnd Tree =======");
        System.out.println("\n\nMax level = " + levelCounter + ", nid = " + deepestNid + ", description = " + Entity.getFast(deepestNid).description());
    }

    /**
     * <p>Iterate recursively the dataset from top to bottom to find the deepest nid and
     * its depth level.
     * </p>
     * @param nid The nid of the concept
     * @param level The level of the concept
     * @param navigator The {@link Navigator} that holds the dataset
     */
    private static void getChildrenNid(int nid, int level, Navigator navigator) {
        if (level > levelCounter) {
            deepestNid = nid;
        }
        levelCounter = Math.max(levelCounter, level++);
        for (Edge edge : navigator.getChildEdges(nid)) {
            getChildrenNid(edge.destinationNid(), level, navigator);
        }
    }

    /**
     * <p>Prints out the inverted tree, starting from a given nid
     * </p>
     * @param nid the nid of the concept
     * @param navigator the {@link Navigator} that holds the dataset
     */
    private static void printInvertedTree(int nid, Navigator navigator) {
        InvertedTree tree = buildInvertedTree(nid, navigator);
        tree.printTree();
    }

    /**
     * <p>Recursive method that prints out the tree hierarchy starting from a given {@link ConceptNavigatorTreeItem},
     * </p>
     * @param treeView the {@link KLConceptNavigatorControl}
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @param printAll if set to true, prints all children, else only those that have a non empty
     * {@link java.util.BitSet}
     * @see ConceptNavigatorTreeItem#getBitSet()
     */
    public static void printTree(KLConceptNavigatorControl treeView, ConceptNavigatorTreeItem treeItem, boolean printAll) {
        for (TreeItem<ConceptFacade> child : treeItem.getChildren()) {
            ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) child;
            if (printAll || !model.getBitSet().isEmpty()) {
                System.out.println((model.isLeaf() ? "-" : "+").repeat(treeView.getTreeItemLevel(model)) + " " + model);
            }
            if (!model.isLeaf()) {
                printTree(treeView, model, printAll);
            }
        }
    }

    /**
     * <p>Builds the {@link InvertedTree} for a given {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem},
     * finds the sorted map of lineages, and takes the shorter lineage that matches the nid and child nid of
     * such item.
     * </p>
     * @param conceptItem a given {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     * @param navigator the {@link Navigator} that holds the dataset
     * @return a {@link List<InvertedTree.ConceptItem>} with the shorter lineage that matches nid and child nid of
     * such item.
     */
    public static List<InvertedTree.ConceptItem> findShorterLineage(InvertedTree.ConceptItem conceptItem, Navigator navigator) {
        InvertedTree tree = buildInvertedTree(conceptItem.childNid(), navigator);
        Map<Integer, List<InvertedTree.ConceptItem>> lineageMap = tree.getLineageMap();

        return lineageMap.values().stream()
                .filter(list -> conceptItem.equals(list.getLast()))
                .findFirst()
                .orElse(List.of());
    }

    /**
     * <p>Resets selection, highlighted and expanded states of every concept TreeItem of the TreeView
     * </p>
     * @param treeView the {@link KLConceptNavigatorControl}
     */
    public static void resetConceptNavigator(KLConceptNavigatorControl treeView) {
        treeView.getSelectionModel().clearSelection();
        iterateTree((ConceptNavigatorTreeItem) treeView.getRoot(), item -> {
            item.setHighlighted(false);
            item.setExpanded(false);
        });
    }

    /**
     * <p>Expands and highlights the concept in the treeView, matching both its nid and parent nid.
     * </p>
     * @param treeView the {@link KLConceptNavigatorControl}
     * @param conceptItem a {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     */
    public static void expandAndHighlightConcept(KLConceptNavigatorControl treeView, InvertedTree.ConceptItem conceptItem) {
        resetConceptNavigator(treeView);

        List<InvertedTree.ConceptItem> lineage = ConceptNavigatorUtils.findShorterLineage(conceptItem, treeView.getNavigator());
        ConceptNavigatorTreeItem parent = (ConceptNavigatorTreeItem) treeView.getRoot();
        for (int i = 0; i < lineage.size(); i++) {
            int parentNid = lineage.get(i).nid();
            int nid = lineage.get(i).childNid();
            ConceptNavigatorTreeItem item = (ConceptNavigatorTreeItem) parent.getChildren().stream()
                    .filter(c -> c.getValue().nid() == nid)
                    .findFirst()
                    .orElse(treeView.getConceptNavigatorTreeItem(nid, parentNid));
            item.setExpanded(true);
            parent = item;
            if (i == lineage.size() - 1) {
                treeView.getSelectionModel().select(item);
                treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
                treeView.getSelectionModel().clearSelection();
                item.setHighlighted(true);
            }
        }
    }

    /**
     * <p>Recursive method that traverses the children of a {@link ConceptNavigatorTreeItem}, applying a certain
     * function to each of them.
     * </p>
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @param consumer a {@link Consumer <ConceptNavigatorTreeItem>} to apply to each tree item
     */
    public static void iterateTree(ConceptNavigatorTreeItem treeItem, Consumer<ConceptNavigatorTreeItem> consumer) {
        for (TreeItem<ConceptFacade> child : treeItem.getChildren()) {
            ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) child;
            consumer.accept(model);
            if (!child.isLeaf()) {
                iterateTree(model, consumer);
            }
        }
    }
}
