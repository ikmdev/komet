package dev.ikm.komet.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.ikm.komet.controls.KLConceptNavigatorControl.MAX_LEVEL;

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

    private static int levelCounter = 0;
    private static int deepestNid;

    public static void getConceptNavigatorDepth(int rootNid, Navigator navigator) {
        levelCounter = 0;
        deepestNid = 0;
        getChildrenNid(rootNid, 0, navigator);

        System.out.println("\n\nTree =======");
        printInvertedTree(deepestNid, navigator);
        System.out.println("\n\nEnd Tree =======");
        System.out.println("\n\nMax level = " + levelCounter + ", nid = " + deepestNid + ", description = " + Entity.getFast(deepestNid).description());
    }

    private static void getChildrenNid(int nid, int level, Navigator navigator) {
        if (level > levelCounter) {
            deepestNid = nid;
        }
        levelCounter = Math.max(levelCounter, level++);
        for (Edge edge : navigator.getChildEdges(nid)) {
            getChildrenNid(edge.destinationNid(), level, navigator);
        }
    }

    private static void printInvertedTree(int nid, Navigator navigator) {
        ConceptFacade facade = Entity.getFast(nid);
        InvertedTree.ConceptItem item = new InvertedTree.ConceptItem(facade.nid(), facade.nid(), facade.description());
        InvertedTree tree = new InvertedTree(item);
        addAll(facade.nid(), tree, navigator);
        tree.printTree();
    }

    private static void addAll(int nid, InvertedTree tree, Navigator navigator) {
        getAllParents(nid, navigator).forEach(i -> addAll(i.nid(), tree.addChild(i), navigator));
    }

    private static List<InvertedTree.ConceptItem> getAllParents(int childNid, Navigator navigator) {
        return getSecondaryParents(childNid, -1, navigator);
    }

    private static List<InvertedTree.ConceptItem> getSecondaryParents(int childNid, int primaryNid, Navigator navigator) {
        return new ArrayList<>(Arrays.stream(navigator.getParentNids(childNid)).boxed()
                .filter(nid -> nid != primaryNid)
                .map(nid -> new InvertedTree.ConceptItem(nid, childNid, Entity.getFast(nid).description()))
                .toList());
    }

}
