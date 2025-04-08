package dev.ikm.komet.kview.controls;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class InvertedTree {

    public record ConceptItem(int nid, int childNid, String description) {}

    ConceptItem item;
    InvertedTree parent;
    List<InvertedTree> children;

    public InvertedTree(ConceptItem item) {
        this.item = item;
        this.children = new LinkedList<>();
    }

    public InvertedTree addChild(ConceptItem child) {
        InvertedTree newChild = new InvertedTree(child);
        newChild.parent = this;
        children.add(newChild);
        return newChild;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public int getLevel() {
        if (this.isRoot()) {
            return 0;
        }
        return parent.getLevel() + 1;
    }

    public void reset() {
        children.clear();
    }

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

    public void printTree() {
        System.out.println("-".repeat(getLevel()) + " " + item.description() + "(" + getLevel() + ")");
        children.forEach(InvertedTree::printTree);
    }

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

    public int countTotalDescendants() {
        counter = 0;
        iterateTree(this, _ -> counter++);
        return counter;
    }

    private int depth = 0;

    public int getTreeDepth() {
        depth = 0;
        iterateTree(this, tree -> depth = Math.max(tree.getLevel(), depth));
        return depth;
    }
}
