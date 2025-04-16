package dev.ikm.komet.kview.controls;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * <p>This is a custom tree data-structure implementation that is used by the {@link LineageBox} control,
 * considering that any given concept can have multiple parents.
 * </p>
 * <p>The {@link KLConceptNavigatorControl} is a regular {@link javafx.scene.control.TreeView} with
 * the usual tree data-structure: for a given dataset, parent - children relationship can be traversed recursively
 * from top to bottom, that is from ancestors to descendents.
 * </p>
 * <p>The {@link InvertedTree} however, sets the root on any given descendent of the same dataset and can be used
 * to traverse up from bottom to top into its ancestors lineage.
 * </p>
 * <p>Therefore, {@link InvertedTree#children} refer to direct ancestors of a given Concept, while
 * {@link InvertedTree#parent} refers to a direct descendant of a given Concept.
 * </p>
 */
public class InvertedTree {

    /**
     * <p>Record that is the base model for the InvertedTree. For a given {@link dev.ikm.tinkar.terms.ConceptFacade},
     * it is defined by its nid and description, and by the nid of a given parent of this ConceptFacade
     * (which is a child in the InvertedTree version.
     * </p>
     * @param nid The nid of a given {@link dev.ikm.tinkar.terms.ConceptFacade}
     * @param childNid The nid of a parent of a given {@link dev.ikm.tinkar.terms.ConceptFacade}
     * @param description The description of a given {@link dev.ikm.tinkar.terms.ConceptFacade}
     */
    public record ConceptItem(int nid, int childNid, String description) {}

    ConceptItem item;
    InvertedTree parent;
    List<InvertedTree> children;

    /**
     * <p>Creates an InvertedTree for a given item, with an empty list of children
     * </p>
     * @param item the {@link ConceptItem} to be set as root
     */
    public InvertedTree(ConceptItem item) {
        this.item = item;
        this.children = new LinkedList<>();
    }

    /**
     * <p>Adds a {@link ConceptItem} as child of this {@link InvertedTree}, by creating a new
     * InvertedTree for that child, and setting this InvertedTree as parent of such child.
     * </p>
     * @param child item to be added as child of this {@link InvertedTree}
     * @return the {@link InvertedTree} of the child
     */
    public InvertedTree addChild(ConceptItem child) {
        InvertedTree newChild = new InvertedTree(child);
        newChild.parent = this;
        children.add(newChild);
        return newChild;
    }

    /**
     * <p>A {@link InvertedTree} is root if it has no parent
     * </p>
     * @return true if this {@link InvertedTree} has no parent
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * <p>A {@link InvertedTree} is leaf if it has no children
     * </p>
     * @return true if this {@link InvertedTree} has no children
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * <p>Level is the lineage depth of a given {@link InvertedTree}
     * </p>
     * <p>If this item is root, it has level 0, its parents level 1, its grandparents level 2, and so on.
     * </p>
     * @return the level of a {@link InvertedTree}
     */
    public int getLevel() {
        if (this.isRoot()) {
            return 0;
        }
        return parent.getLevel() + 1;
    }

    /**
     * Removes the children (that is, the ancestors) of a given {@link InvertedTree}
     */
    public void reset() {
        children.clear();
    }

    /**
     * <p>Iterates recursively this {@link InvertedTree} to find the InvertedTree of the 'child' ancestor, or
     * null if it is not found
     * </p>
     * @param child the {@link ConceptItem} to find within the lineage of this {@link InvertedTree}
     * @return the {@link InvertedTree} for the given child, or null if not found
     */
    public InvertedTree getInvertedTree(ConceptItem child) {
        if (item.equals(child)) {
            return this;
        }
        for (InvertedTree i : children) {
            if (i.item.equals(child)) {
                return i;
            }
            return i.getInvertedTree(child);
        }
        return null;
    }


    /**
     * <p>Iterates recursively this {@link InvertedTree} to find if the 'child' item is an ancestor or not
     * </p>
     * @param child the {@link ConceptItem} to find within the lineage of this {@link InvertedTree}
     * @return true if the given child is an ancestor of this {@link InvertedTree}, or false otherwise
     */
    public boolean contains(ConceptItem child) {
        if (item.equals(child)) {
            return true;
        }
        for (InvertedTree i : children) {
            if (i.item.equals(child)) {
                return true;
            }
            return i.contains(child);
        }
        return false;
    }

    /**
     * <p>Utility method that can be use for debugging purposes, as it prints out this tree with indentation and
     * level indication.
     * </p>
     * <p> For instance, the following is the output of a call to this method from a given root item:
     * <pre>
     * GrandChild_1(0)
     * - Child_1(1)
     * -- ConceptA(2)
     * --- Parent_1(3)
     * ---- GrandParent_1(4)
     * ----- GGrandParent_1(5)
     * ------ Scenarios(6)
     * ...
     * --- Parent_2(3)
     * ---- GrandParent_2(4)
     * ----- GGrandParent_2(5)
     * ------ GGGrandParent_2(6)
     * ...
     * </pre>
     * </p>
     */
    public void printTree() {
        System.out.println("-".repeat(getLevel()) + " " + item.description() + "(" + getLevel() + ")");
        children.forEach(InvertedTree::printTree);
    }

    /**
     * <p>Utility method that can be used to traverse the lineage of a given item, and apply a function
     * to any of its ancestors
     * </p>
     * @param treeItem the {@link InvertedTree} root to start iterating from
     * @param consumer the function to apply to every ancestor of this root item
     */
    public void iterateTree(InvertedTree treeItem, Consumer<InvertedTree> consumer) {
        if (treeItem == null) {
            return;
        }
        for (InvertedTree child : treeItem.children) {
            consumer.accept(child);
            if (!child.isLeaf()) {
                iterateTree(child, consumer);
            }
        }
    }

    private int counter = 0;

    /**
     * <p>Utility method that returns the total number of descendants of this {@link InvertedTree}
     * </p>
     * @return the number of descendants of this {@link InvertedTree}
     */
    public int countTotalDescendants() {
        counter = 0;
        iterateTree(this, _ -> counter++);
        return counter;
    }

    private int depth = 0;

    /**
     * <p>Utility method that returns the total depth of this {@link InvertedTree}
     * </p>
     * @return the depth of this {@link InvertedTree}
     */
    public int getTreeDepth() {
        depth = 0;
        iterateTree(this, tree -> depth = Math.max(tree.getLevel(), depth));
        return depth;
    }
}
