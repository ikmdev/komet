package dev.ikm.komet.kview.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.coordinate.stamp.StateSet;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.coordinate.view.calculator.ViewCalculator;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.State;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TreeItem;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Scale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.controls.KLConceptNavigatorControl.MAX_LEVEL;

/**
 * Utility class for the {@link KLConceptNavigatorControl} and related controls and components.
 */
public class ConceptNavigatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptNavigatorUtils.class);

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
     * @param tile the {@link ConceptTile}
     * @return a {@link WritableImage}
     */
    public static WritableImage getTileSnapshot(ConceptTile tile) {
        SnapshotParameters p = new SnapshotParameters();
        if (tile.getScene() == null || tile.getScene().getWindow() == null) {
            // in case the tile is not yet attached to a scene or window
            // or was removed from the scene
            return null;
        }
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
     * @param childNid the nid of a given child
     * @param primaryNid the nid of the primary parent, or -1 if not set
     * @param navigator the {@link Navigator} that holds the dataset
     * @return a list of {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     */
    static List<InvertedTree.ConceptItem> getSecondaryParents(int childNid, int primaryNid, Navigator navigator) {
        return new ArrayList<>(Arrays.stream(getParentNids(navigator, childNid)).boxed()
                .filter(nid -> nid != primaryNid)
                .map(nid -> new InvertedTree.ConceptItem(nid, childNid, Entity.getFast(nid).description()))
                .toList());
    }

    /**
     * <p>Utility method that builds and {@link InvertedTree} starting from
     * a given nid and a given {@link Navigator}, and finds its maximum depth.
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
     * @param nid the nid of the concept
     * @param navigator the {@link Navigator} that holds the dataset
     * @return an {@link InvertedTree}
     */
    public static InvertedTree buildInvertedTree(int nid, Navigator navigator) {
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
     * @param nid the nid of the concept
     * @param navigator the {@link Navigator} that holds the dataset
     */
    private static void printInvertedTree(int nid, Navigator navigator) {
        InvertedTree tree = buildInvertedTree(nid, navigator);
        tree.printTree();
    }

    /**
     * <p>Recursive method that prints out the tree hierarchy starting from a given {@link ConceptNavigatorTreeItem},
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
     * such item, unless the nid is -1, in which case, it takes the first route.
     * @param conceptItem a given {@link dev.ikm.komet.kview.controls.InvertedTree.ConceptItem}
     * @param navigator the {@link Navigator} that holds the dataset
     * @return a {@link List<InvertedTree.ConceptItem>} with the shorter lineage that matches nid and child nid of
     * such item.
     */
    public static List<InvertedTree.ConceptItem> findShorterLineage(InvertedTree.ConceptItem conceptItem, Navigator navigator) {
        InvertedTree tree = buildInvertedTree(conceptItem.childNid(), navigator);
        Map<Integer, List<InvertedTree.ConceptItem>> lineageMap = tree.getLineageMap();

        return lineageMap.values().stream()
                .filter(list -> conceptItem.nid() == -1 || conceptItem.equals(list.getLast()))
                .findFirst()
                .orElse(List.of());
    }

    /**
     * <p>Outcome of investigating why a concept could not be displayed in the
     * {@link KLConceptNavigatorControl}.
     * @param summary a short, user-facing explanation suitable for a notification
     * @param detail a fuller, log-oriented diagnostic of the concept's state
     */
    public record ConceptDisplayDiagnosis(String summary, String detail) {}

    /**
     * <p>Investigates why a concept has no lineage that can be expanded in a {@link Navigator}, and
     * therefore could not be shown in the {@link KLConceptNavigatorControl}.
     * <p>{@link #findShorterLineage(InvertedTree.ConceptItem, Navigator)} returns an empty lineage whenever
     * the concept cannot be traced from a root of this navigator down to itself. This is expected for concepts
     * that fall outside the navigator's view, most commonly:
     * <ul>
     *     <li>a retired (inactive) concept shown to a navigator whose vertex states include only active
     *     concepts &mdash; some navigators include retired concepts, others do not;</li>
     *     <li>a concept in a module or path that the navigator's view coordinate excludes;</li>
     *     <li>a concept that is itself a root of the navigator (already shown at the top level);</li>
     *     <li>a concept that does not exist in the database.</li>
     * </ul>
     * <p>The investigation never throws: any failure to resolve a piece of state is captured in the
     * returned diagnostic rather than propagated.
     * @param nid the nid of the concept that failed to display
     * @param navigator the {@link Navigator} that backs the control
     * @return a {@link ConceptDisplayDiagnosis} with a user-facing summary and a log-oriented detail
     */
    public static ConceptDisplayDiagnosis investigateUndisplayableConcept(int nid, Navigator navigator) {
        try {
            ViewCalculator viewCalculator = navigator.getViewCalculator();
            String name = viewCalculator.getDescriptionTextOrNid(nid);

            if (EntityHandle.get(nid).isAbsent()) {
                String summary = "“" + name + "” could not be shown: no concept with that identifier "
                        + "exists in the database.";
                return new ConceptDisplayDiagnosis(summary, summary + " (nid=" + nid + ")");
            }

            Latest<EntityVersion> latest = viewCalculator.latest(nid);
            State state = latest.isPresent() ? latest.get().stamp().state() : null;
            // The navigation coordinate is the source of truth for which vertex (concept) states this navigator
            // includes; the navigation/view calculator derives allowedVertexStates() from it. Read it directly.
            StateSet vertexStates = viewCalculator.navigationCalculator().navigationCoordinate().vertexStates();
            int[] parentNids = getParentNids(navigator, nid);
            boolean isRoot = isRoot(navigator, nid);

            String summary;
            if (isRoot) {
                summary = "“" + name + "” is a root concept of this navigator and is already shown "
                        + "at the top of the tree.";
            } else if (state != null && vertexStates != null && !vertexStates.contains(state)) {
                // Most common case: the concept's status is excluded by this navigator's vertex states.
                summary = "“" + name + "” has status " + state + ", which this navigator does not "
                        + "display (it shows " + vertexStates.toUserString() + " concepts only). Open it in a "
                        + "navigator that includes " + state + " concepts to see it in context.";
            } else if (parentNids.length == 0) {
                summary = "“" + name + "” has no parent concepts in this navigator’s view"
                        + (state != null ? " (status " + state + ")" : "")
                        + ", so it cannot be placed in the tree. It may belong to a module or path this navigator "
                        + "does not include.";
            } else {
                summary = "“" + name + "” could not be traced to a root concept visible in this "
                        + "navigator, although it reports " + parentNids.length + " parent"
                        + (parentNids.length == 1 ? "" : "s")
                        + ". Its lineage may pass through concepts this navigator’s view excludes.";
            }

            String detail = "Concept “" + name + "” (nid=" + nid + ") is not displayable in this "
                    + "navigator. latestState=" + (state != null ? state : "<no version visible in view>")
                    + ", navigationCoordinateVertexStates=" + (vertexStates != null ? vertexStates.toUserString() : "<unknown>")
                    + ", isRoot=" + isRoot
                    + ", parentCountInView=" + parentNids.length
                    + ", parentsInView=" + describeNids(parentNids, viewCalculator);
            return new ConceptDisplayDiagnosis(summary, detail);
        } catch (RuntimeException e) {
            LOG.error("Failed to investigate undisplayable concept (nid={})", nid, e);
            String summary = "This concept could not be shown in the navigator, and its state could not be "
                    + "determined (nid=" + nid + ").";
            return new ConceptDisplayDiagnosis(summary, summary);
        }
    }

    /**
     * <p>Determines whether the given nid is a root of the {@link Navigator}.
     * @param navigator the {@link Navigator} that holds the dataset
     * @param nid the nid of the concept
     * @return true if the nid is one of the navigator's root nids
     */
    private static boolean isRoot(Navigator navigator, int nid) {
        try {
            return Arrays.stream(navigator.getRootNids()).anyMatch(rootNid -> rootNid == nid);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return false;
        }
    }

    /**
     * <p>Renders an array of nids as a human-readable list of {@code description (nid)} entries.
     * @param nids the nids to describe
     * @param viewCalculator the {@link ViewCalculator} used to resolve descriptions
     * @return a bracketed, comma-separated description of the nids
     */
    private static String describeNids(int[] nids, ViewCalculator viewCalculator) {
        return Arrays.stream(nids)
                .mapToObj(parentNid -> viewCalculator.getDescriptionTextOrNid(parentNid) + " (" + parentNid + ")")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * <p>Resets selection, highlighted and expanded states of every concept TreeItem of the TreeView,
     * removing also all items, starting from the second generation
     * @param treeView the {@link KLConceptNavigatorControl}
     */
    public static void resetConceptNavigator(KLConceptNavigatorControl treeView) {
        treeView.getSelectionModel().clearSelection();
        treeView.getRoot().getChildren().forEach(item -> {
            ((ConceptNavigatorTreeItem) item).setHighlighted(false);
            if (!item.isLeaf()) {
                item.setExpanded(false);
                item.getChildren().clear();
            }
        });
    }

    /**
     * <p>Recursive method that traverses the children of a {@link ConceptNavigatorTreeItem}, applying a certain
     * function to each of them.
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @param consumer a {@code Consumer<ConceptNavigatorTreeItem>} to apply to each tree item
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

    public static boolean isDefined(ViewCalculator viewCalculator, ConceptFacade facade) {
        try {
            return viewCalculator.isDefined(facade);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
        }
        return false;
    }

    public static boolean isLeaf(Navigator navigator, int nid) {
        try {
            return navigator.isLeaf(nid);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
        }
        return false;
    }

    public static int[] getParentNids(Navigator navigator, int nid) {
        try {
            return navigator.getParentNids(nid);
        } catch (Exception e) {
            LOG.error("Exception occurred", e);
            return new int[0];
        }
    }


}
