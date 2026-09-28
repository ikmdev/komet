package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell;
import dev.ikm.komet.kview.controls.PinnedAncestorsLayout;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * <p>The pinned ancestors of the {@link KLConceptNavigatorControl}: an overlay on top of the tree, that
 * shows the ancestors of the first concept that is visible at the current scroll position, so the user
 * always knows where in the hierarchy the visible concepts are.
 * <p>Each ancestor is rendered with a regular {@link KLConceptNavigatorTreeCell}, at its own indentation
 * level, so it looks and behaves like any other concept of the tree: it can be selected, collapsed,
 * dragged, or opened. Clicking on it also scrolls the tree to it.
 * <p>Only the nearest {@link PinnedAncestorsLayout#MAX_COLLAPSED_ANCESTORS} ancestors are pinned. When
 * there are more, a toggle on top allows showing all of them, and hiding them again. The choice is kept
 * while scrolling.
 * <p>This pane is not managed: it lays itself over the virtual flow after each layout pass of the latter.
 * @see PinnedAncestorsLayout
 */
public class PinnedAncestorsPane extends VBox {

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");
    private static final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.kview.controls.concept-navigator");

    private final KLConceptNavigatorControl treeView;
    private final ConceptNavigatorVirtualFlow virtualFlow;

    private final Button toggleButton;
    private final Label countLabel;
    private final StackPane toggleRow;
    private final List<KLConceptNavigatorTreeCell> cells = new ArrayList<>();
    private int shownCells;
    private boolean expanded;
    private boolean measured;

    private Region viewport;
    private double rowHeight = 26;
    private double toggleHeight = 22;
    private double insets;

    /**
     * <p>Creates a {@link PinnedAncestorsPane} instance.
     * @param treeView the {@link KLConceptNavigatorControl} that holds the concepts
     * @param virtualFlow the {@link ConceptNavigatorVirtualFlow} of the control, that this pane overlays
     */
    public PinnedAncestorsPane(KLConceptNavigatorControl treeView, ConceptNavigatorVirtualFlow virtualFlow) {
        this.treeView = treeView;
        this.virtualFlow = virtualFlow;

        Region chevron = new Region();
        chevron.getStyleClass().add("chevron");
        toggleButton = new Button(null, chevron);
        toggleButton.setMaxWidth(Double.MAX_VALUE);
        toggleButton.setAlignment(Pos.CENTER_LEFT);
        toggleButton.setFocusTraversable(false);
        toggleButton.setOnAction(_ -> {
            expanded = !expanded;
            virtualFlow.requestLayout();
        });
        countLabel = new Label();
        countLabel.getStyleClass().add("count-label");
        countLabel.setMouseTransparent(true);
        StackPane.setAlignment(countLabel, Pos.CENTER_RIGHT);
        toggleRow = new StackPane(toggleButton, countLabel);
        toggleRow.getStyleClass().add("toggle-row");

        // The pane covers the virtual flow, so it has to pass on the scroll gestures it gets
        addEventHandler(ScrollEvent.ANY, e -> {
            virtualFlow.fireEvent(e.copyFor(virtualFlow, virtualFlow));
            e.consume();
        });

        getStyleClass().add("pinned-ancestors");
        setManaged(false);
        setVisible(false);
    }

    /**
     * {@inheritDoc}
     * Overridden to give the pinned ancestors the full width of the viewport, as the virtual flow does
     * with its cells, regardless of their maximum width.
     */
    @Override
    protected void layoutChildren() {
        double x = snappedLeftInset();
        double y = snappedTopInset();
        double width = getWidth() - snappedLeftInset() - snappedRightInset();
        for (Node child : getManagedChildren()) {
            double height = child.prefHeight(width);
            child.resizeRelocate(x, y, width, height);
            y += height;
        }
    }

    /**
     * <p>Finds the ancestors for the current scroll position, and lays them over the virtual flow.
     * <p>It has to be called after each layout pass of the virtual flow.
     */
    public void update() {
        if (!measured) {
            measure();
        }
        List<TreeCell<ConceptFacade>> visibleCells = getVisibleCells();
        List<PinnedAncestorsLayout.Row> rows = visibleCells.stream()
                .map(cell -> new PinnedAncestorsLayout.Row(getTop(cell), cell.getHeight(), getDepth(cell.getTreeItem())))
                .toList();
        visibleCells.stream()
                .filter(cell -> !((ConceptNavigatorTreeItem) cell.getTreeItem()).isViewLineage())
                .findFirst()
                .ifPresent(cell -> rowHeight = cell.getHeight());

        double width = getViewport().getWidth();
        int fromIndex = 0;
        PinnedAncestorsLayout.Result lastResult = null;
        while (true) {
            PinnedAncestorsLayout.Result result = PinnedAncestorsLayout.compute(rows, fromIndex, expanded,
                    new PinnedAncestorsLayout.Metrics(rowHeight, toggleHeight, insets));
            if (result.shownCount() == 0) {
                clear();
                return;
            }
            if (lastResult != null && lastResult.anchorIndex() == result.anchorIndex()) {
                break;
            }
            showAncestors((ConceptNavigatorTreeItem) visibleCells.get(result.anchorIndex()).getTreeItem(), result);
            // the actual sizes are known only once the nodes are styled: a pinned ancestor that shows its
            // lineage, for instance, is taller than a regular row
            applyCss();
            insets = snappedTopInset() + snappedBottomInset();
            if (result.toggleVisible()) {
                toggleHeight = toggleRow.prefHeight(width);
            }
            PinnedAncestorsLayout.Row anchor = rows.get(result.anchorIndex());
            if (anchor.top() + anchor.height() <= prefHeight(width)) {
                // the anchor is covered after all: try with the next row
                fromIndex = result.anchorIndex() + 1;
            }
            lastResult = result;
        }
        setVisible(true);
        resizeRelocate(virtualFlow.getLayoutX(), virtualFlow.getLayoutY(), width, prefHeight(width));
        layout();
    }

    /**
     * <p>Finds the sizes of the parts of this pane that don't depend on the pinned ancestors, so the
     * height of the pane can be known before it shows them.
     */
    private void measure() {
        getChildren().setAll(toggleRow);
        applyCss();
        toggleHeight = toggleRow.prefHeight(-1);
        insets = snappedTopInset() + snappedBottomInset();
        getChildren().clear();
        measured = true;
    }

    /**
     * <p>Estimated height that this pane needs to show the ancestors of a given {@link ConceptNavigatorTreeItem}.
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @return the height of the pane when the ancestors of the item are pinned, or 0 if it has no ancestors
     */
    public double getHeightFor(ConceptNavigatorTreeItem treeItem) {
        return PinnedAncestorsLayout.height(getDepth(treeItem), expanded,
                new PinnedAncestorsLayout.Metrics(rowHeight, toggleHeight, insets));
    }

    /**
     * <p>Whether this pane covers, at least partially, a cell of the virtual flow.
     * @param cell a cell of the virtual flow
     * @return true if the cell is covered by this pane
     */
    public boolean covers(TreeCell<ConceptFacade> cell) {
        return isVisible() && getTop(cell) < getHeight();
    }

    /**
     * <p>Gets a {@link Stream} of the cells that show a pinned ancestor.
     * @return a {@link Stream} of {@link KLConceptNavigatorTreeCell}
     */
    public Stream<KLConceptNavigatorTreeCell> getCellStream() {
        return cells.stream().limit(shownCells);
    }

    /**
     * <p>Finds the cell that shows the passed {@link ConceptNavigatorTreeItem} as pinned ancestor, if any.
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @return an optional of {@link KLConceptNavigatorTreeCell}
     */
    public Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(ConceptNavigatorTreeItem treeItem) {
        return getCellStream()
                .filter(cell -> cell.getTreeItem() == treeItem)
                .findFirst();
    }

    /**
     * <p>Pins the nearest ancestors of the anchor, reusing the existing cells.
     * @param anchor the {@link ConceptNavigatorTreeItem} of the first visible concept
     * @param result the {@link PinnedAncestorsLayout.Result} for the anchor
     */
    private void showAncestors(ConceptNavigatorTreeItem anchor, PinnedAncestorsLayout.Result result) {
        List<TreeItem<ConceptFacade>> ancestors = new ArrayList<>();
        TreeItem<ConceptFacade> ancestor = anchor.getParent();
        while (ancestors.size() < result.shownCount()) {
            ancestors.addFirst(ancestor);
            ancestor = ancestor.getParent();
        }

        while (cells.size() < ancestors.size()) {
            cells.add(createCell());
        }
        for (int i = 0; i < cells.size(); i++) {
            KLConceptNavigatorTreeCell cell = cells.get(i);
            int index = i < ancestors.size() ? treeView.getRow(ancestors.get(i)) : -1;
            if (cell.getIndex() != index || (index != -1 && cell.getTreeItem() != ancestors.get(i))) {
                cell.updateIndex(index);
            }
        }
        shownCells = ancestors.size();

        if (result.toggleVisible()) {
            toggleButton.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, expanded);
            toggleButton.setText(expanded ?
                    resources.getString("pinned.ancestors.show.fewer") :
                    MessageFormat.format(resources.getString("pinned.ancestors.show.more"),
                            result.ancestorCount() - result.shownCount()));
            countLabel.setText(MessageFormat.format(resources.getString("pinned.ancestors.count"),
                    result.shownCount(), result.ancestorCount()));
        }

        List<Node> children = new ArrayList<>();
        if (result.toggleVisible()) {
            children.add(toggleRow);
        }
        children.addAll(cells.subList(0, shownCells));
        if (!children.equals(getChildren())) {
            getChildren().setAll(children);
        }
    }

    /**
     * <p>Removes the pinned ancestors, when the first visible concept has none.
     */
    private void clear() {
        if (!isVisible()) {
            return;
        }
        setVisible(false);
        getChildren().clear();
        getCellStream().forEach(cell -> cell.updateIndex(-1));
        shownCells = 0;
    }

    private KLConceptNavigatorTreeCell createCell() {
        KLConceptNavigatorTreeCell cell = new KLConceptNavigatorTreeCell(treeView);
        cell.updateTreeView(treeView);
        cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && e.isStillSincePress() &&
                    cell.getTreeItem() != null && !isTileButton(e.getTarget(), cell)) {
                treeView.scrollTo(treeView.getRow(cell.getTreeItem()));
            }
        });
        return cell;
    }

    /**
     * <p>Whether the target of an event is one of the buttons of the concept tile (disclosure, select or
     * lineage), which have their own action.
     */
    private static boolean isTileButton(Object target, KLConceptNavigatorTreeCell cell) {
        Node node = target instanceof Node n ? n : null;
        while (node != null && node != cell) {
            if (node.getStyleClass().contains("region")) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    /**
     * <p>Gets the cells of the virtual flow that show a concept, from top to bottom.
     */
    private List<TreeCell<ConceptFacade>> getVisibleCells() {
        List<TreeCell<ConceptFacade>> visibleCells = new ArrayList<>();
        TreeCell<ConceptFacade> cell = virtualFlow.getFirstVisibleCell();
        while (cell != null && cell.getTreeItem() != null) {
            visibleCells.add(cell);
            cell = virtualFlow.getVisibleCell(cell.getIndex() + 1);
        }
        return visibleCells;
    }

    /**
     * <p>Gets the y coordinate of the top of a cell of the virtual flow, relative to the top of its viewport.
     */
    private double getTop(TreeCell<ConceptFacade> cell) {
        return virtualFlow.sceneToLocal(cell.localToScene(0, 0)).getY();
    }

    /**
     * <p>Gets the number of ancestors of a {@link TreeItem} that the tree shows.
     */
    private int getDepth(TreeItem<ConceptFacade> treeItem) {
        return treeView.getTreeItemLevel(treeItem) - (treeView.isShowRoot() ? 0 : 1);
    }

    private Region getViewport() {
        if (viewport == null) {
            viewport = (Region) virtualFlow.lookup(".clipped-container");
        }
        return viewport;
    }
}
