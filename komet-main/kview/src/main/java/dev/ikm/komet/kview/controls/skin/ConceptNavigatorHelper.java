package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell;

import java.util.concurrent.Future;

/**
 * Helper class to access internal methods of controls package.
 */
public class ConceptNavigatorHelper {

    private static ConceptNavigatorAccessor accessor;

    public interface ConceptNavigatorAccessor {
        Future<Boolean> fetchChildrenTask(KLConceptNavigatorControl treeView, ConceptNavigatorTreeItem item);
        ConceptNavigatorTreeItem getConceptNavigatorTreeItem(KLConceptNavigatorControl treeView, int nid, int parentNid);
    }

    public static Future<Boolean> fetchChildrenTask(KLConceptNavigatorControl treeView, ConceptNavigatorTreeItem item) {
        return accessor.fetchChildrenTask(treeView, item);
    }

    public static ConceptNavigatorTreeItem getConceptNavigatorTreeItem(KLConceptNavigatorControl treeView, int nid, int parentNid) {
        return accessor.getConceptNavigatorTreeItem(treeView, nid, parentNid);
    }

    public static void setConceptNavigatorAccessor(ConceptNavigatorAccessor accessor) {
        ConceptNavigatorHelper.accessor = accessor;
    }

    private static ConceptNavigatorCellAccessor cellAccessor;

    public interface ConceptNavigatorCellAccessor {
        void markCellDirty(KLConceptNavigatorTreeCell treeCell);
        void unselectItem(KLConceptNavigatorTreeCell treeCell);
    }

    public static void markCellDirty(KLConceptNavigatorTreeCell treeCell) {
        cellAccessor.markCellDirty(treeCell);
    }

    public static void unselectItem(KLConceptNavigatorTreeCell treeCell) {
        cellAccessor.unselectItem(treeCell);
    }

    public static void setConceptNavigatorCellAccessor(ConceptNavigatorCellAccessor cellAccessor) {
        ConceptNavigatorHelper.cellAccessor = cellAccessor;
    }


}
