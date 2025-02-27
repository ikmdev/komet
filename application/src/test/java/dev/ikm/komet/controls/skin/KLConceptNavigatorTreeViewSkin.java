package dev.ikm.komet.controls.skin;

import dev.ikm.komet.controls.ConceptNavigatorModel;
import dev.ikm.komet.controls.KLConceptNavigatorControl;
import dev.ikm.komet.controls.KLConceptNavigatorTreeCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.controls.ConceptNavigatorModel.STATE;
import static dev.ikm.komet.controls.ConceptNavigatorModel.PS_STATE;
import static dev.ikm.komet.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;
import static dev.ikm.komet.controls.KLConceptNavigatorTreeCell.LONG_HOVER_PSEUDO_CLASS;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<ConceptNavigatorModel> {

    private final Label header;
    private ConceptNavigatorVirtualFlow virtualFlow;
    private final Path draggingBox;
    private Group sheet;
    private double x, y;
    private double xMin, yMin, xMax, yMax;

    private final List<ConceptNavigatorModel> draggedItems = new ArrayList<>();

    public KLConceptNavigatorTreeViewSkin(TreeView<ConceptNavigatorModel> treeView) {
        super(treeView);
        header = new Label();
        header.getStyleClass().add("concept-header");
        header.textProperty().bind(((KLConceptNavigatorControl) treeView).headerProperty());
        header.visibleProperty().bind(header.textProperty().isNotEmpty());
        header.managedProperty().bind(header.visibleProperty());

        draggingBox = new Path();
        draggingBox.getStyleClass().add("dragging-box");
        draggingBox.visibleProperty().bind(Bindings.isNotEmpty(draggingBox.getElements()));
        draggingBox.setManaged(false);

        Rectangle clip = new Rectangle();
        clip.yProperty().bind(header.heightProperty());
        clip.widthProperty().bind(virtualFlow.widthProperty());
        clip.heightProperty().bind(virtualFlow.heightProperty());
        draggingBox.setClip(clip);
        getChildren().addAll(header, draggingBox);

        treeView.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (isDragging() &&
                    e.getButton() == MouseButton.SECONDARY ||
                    (e.getButton() == MouseButton.PRIMARY &&
                            !new Rectangle2D(xMin, yMin, xMax - xMin, yMax - yMin).contains(e.getSceneX(), e.getSceneY()))) {
                setDragging(false);
            }
            x = e.getX();
            y = e.getY();
        });
        treeView.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isDraggingAllowed() && draggedItems.isEmpty()) {
                setDragging(true);
                virtualFlow.setMouseTransparent(true);
                double newX = e.getX();
                double newY = e.getY();
                double x0 = Math.min(newX, x), y0 = Math.min(newY, y);
                double x1 = Math.max(newX, x), y1 = Math.max(newY, y);
                draggingBox.getElements().setAll(new MoveTo(x0, y0), new LineTo(x1, y0), new LineTo(x1, y1), new LineTo(x0, y1), new ClosePath());
                e.consume();
            }
        });
        treeView.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            if (isDragging()) {
                prepareDrag();
                if (draggedItems.isEmpty()) {
                    setDragging(false);
                }
                e.consume();
            }
        });

        treeView.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
            if (!draggedItems.isEmpty()) {
                List<UUID[]> list = draggedItems.stream()
                        .filter(i -> i.getModel() != null && i.getModel().publicId() != null)
                        .map(i -> i.getModel().publicId().asUuidArray())
                        .toList();
                Dragboard dragboard = getSkinnable().startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(CONCEPT_NAVIGATOR_DRAG_FORMAT, list);
                dragboard.setContent(clipboardContent);
                SnapshotParameters p = new SnapshotParameters();
                Point2D point2D = new Point2D(xMin, yMin);
                point2D = sheet.sceneToLocal(point2D);
                p.setViewport(new Rectangle2D(point2D.getX(), point2D.getY(),
                        xMax - xMin, yMax - yMin));
                WritableImage snapshot = sheet.snapshot(p, null);
                dragboard.setDragView(snapshot);
                e.consume();
            }
        });
        treeView.setOnDragDone(e -> setDragging(false));
    }

    @Override
    protected VirtualFlow<TreeCell<ConceptNavigatorModel>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    // draggingProperty
    private final BooleanProperty draggingProperty = new SimpleBooleanProperty(this, "dragging") {
        @Override
        protected void invalidated() {
            if (get()) {
                getSkinnable().getSelectionModel().clearSelection();
            } else {
                cancelDrag();
            }
        }
    };
    public final BooleanProperty draggingProperty() {
       return draggingProperty;
    }
    public final boolean isDragging() {
       return draggingProperty.get();
    }
    public final void setDragging(boolean value) {
        draggingProperty.set(value);
    }

    // draggingAllowedProperty
    private final BooleanProperty draggingAllowedProperty = new SimpleBooleanProperty(this, "draggingAllowed", true);
    public final BooleanProperty draggingAllowedProperty() {
       return draggingAllowedProperty;
    }
    public final boolean isDraggingAllowed() {
       return draggingAllowedProperty.get();
    }
    public final void setDraggingAllowed(boolean value) {
        draggingAllowedProperty.set(value);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        if (header.isVisible()) {
            double titleHeight = header.prefHeight(contentWidth);
            super.layoutChildren(contentX, contentY + titleHeight, contentWidth, contentHeight - titleHeight);
            header.resizeRelocate(contentX, contentY, contentWidth, titleHeight);
        } else {
            super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
        }
    }

    private Group getSheet() {
        if (sheet == null) {
            sheet = (Group) virtualFlow.lookup(".sheet");
        }
        return sheet;
    }

    public void unselectAllItems() {
        unmarkAllItems(STATE.SELECTED);
    }

    public void unhoverAllItems() {
        unmarkAllItems(STATE.LONG_HOVER);
    }

    private void unmarkAllItems(STATE state) {
        getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .forEach(KLConceptNavigatorTreeCell::unselectItem);
        List<Integer> statesBits = PS_STATE.getStatesBitRange(state);
        iterateTree(getSkinnable().getRoot(), item -> {
            item.getValue().getBitSet().clear(statesBits.getFirst(), statesBits.getLast());
            markCellDirty(item);
        });
        virtualFlow.requestLayout();
    }

    public void selectAllAncestors(TreeItem<ConceptNavigatorModel> child) {
        markAllAncestors(child, true);
    }

    public void hoverAllAncestors(TreeItem<ConceptNavigatorModel> child) {
        markAllAncestors(child, false);
    }

    private void markAllAncestors(TreeItem<ConceptNavigatorModel> child, boolean select) {
        TreeItem<ConceptNavigatorModel> treeItem = child;
        while (treeItem != null) {
            // for each ancestor (including starting one)
            if (treeItem == child) {
                if (!select) {
                    // mark current item as long-hovered
                    treeItem.getValue().getBitSet().set(PS_STATE.LONG_HOVER.getBit());
                }
            } else {
                // mark ancestor as selected/long-hovered
                treeItem.getValue().getBitSet().set(select ? PS_STATE.BORDER_SELECTED.getBit() : PS_STATE.BORDER_LONG_HOVER.getBit());
            }
            // show curved-line
            treeItem.getValue().getBitSet().set(select ? PS_STATE.CURVED_LINE_SELECTED.getBit() : PS_STATE.CURVED_LINE_LONG_HOVER.getBit());
            markCellDirty(treeItem);

            int level = getSkinnable().getTreeItemLevel(treeItem) - 2;
            if (level >= 0) {
                // for each previous sibling of this ancestor:
                TreeItem<ConceptNavigatorModel> previousSibling = treeItem.previousSibling();
                while (previousSibling != null) {
                    // and all expanded descendants if these are expanded
                    applyPseudoClassState(previousSibling, select, level);
                    previousSibling = previousSibling.previousSibling();
                }
            }
            treeItem = treeItem.getParent();
        }
        virtualFlow.requestLayout();

        // debug:
//        printTree(getSkinnable().getRoot(), false);

    }

    private void applyPseudoClassState(TreeItem<ConceptNavigatorModel> treeItem, boolean select, int level) {
        treeItem.getValue().getBitSet().set(select ? PS_STATE.LINE_I_SELECTED.getBit() + level : PS_STATE.LINE_I_LONG_HOVER.getBit() + level);
        markCellDirty(treeItem);
        if (treeItem.isExpanded()) {
            treeItem.getChildren().forEach(i -> applyPseudoClassState(i, select, level));
        }
    }

    private void markCellDirty(TreeItem<ConceptNavigatorModel> treeItem) {
        getCellForTreeItem(treeItem).ifPresent(KLConceptNavigatorTreeCell::markCellDirty);
    }

    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(TreeItem<ConceptNavigatorModel> treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    private void iterateTree(TreeItem<ConceptNavigatorModel> treeItem, Consumer<TreeItem<ConceptNavigatorModel>> consumer){
        for (TreeItem<ConceptNavigatorModel> child : treeItem.getChildren()) {
            consumer.accept(child);
            if (!child.isLeaf()) {
                iterateTree(child, consumer);
            }
        }
    }

    private void printTree(TreeItem<ConceptNavigatorModel> treeItem, boolean printAll) {
        for (TreeItem<ConceptNavigatorModel> child : treeItem.getChildren()) {
            if (child.isLeaf()) {
                if (printAll || !child.getValue().getBitSet().isEmpty()) {
                    System.out.println("-".repeat(getSkinnable().getTreeItemLevel(child)) + " " + child.getValue());
                }
            } else {
                if (printAll || !child.getValue().getBitSet().isEmpty()) {
                    System.out.println("+".repeat(getSkinnable().getTreeItemLevel(child)) + " " + child.getValue());
                }
                printTree(child, printAll);
            }
        }
    }

    private void prepareDrag() {
        if (draggingBox.getLayoutBounds().getWidth() < 10 || draggingBox.getLayoutBounds().getHeight() < 10) {
            draggingBox.getElements().clear();
            return;
        }
        if (!draggingBox.getElements().isEmpty()) {
            xMin = Double.MAX_VALUE; yMin = Double.MAX_VALUE;
            xMax = Double.MIN_VALUE; yMax = Double.MIN_VALUE;
            getSheet().getChildren().stream()
                    .filter(KLConceptNavigatorTreeCell.class::isInstance)
                    .map(KLConceptNavigatorTreeCell.class::cast)
                    .forEach(cell -> {
                        Node graphic = cell.getGraphic();
                        if (graphic != null) {
                            Bounds sceneBounds = graphic.localToScene(graphic.getLayoutBounds());
                            Bounds localBounds = getSkinnable().sceneToLocal(sceneBounds);
                            if (draggingBox.intersects(localBounds)) {
                                cell.pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, true);
                                draggedItems.add(cell.getTreeItem().getValue());
                                if (sceneBounds.getMinX() < xMin) {
                                    xMin = sceneBounds.getMinX();
                                }
                                if (sceneBounds.getMaxX() > xMax) {
                                    xMax = sceneBounds.getMaxX();
                                }
                                if (sceneBounds.getMinY() < yMin) {
                                    yMin = sceneBounds.getMinY();
                                }
                                if (sceneBounds.getMaxY() > yMax) {
                                    yMax = sceneBounds.getMaxY();
                                }
                            }
                        }
                    });
            draggingBox.getElements().clear();
        }
    }

    private void cancelDrag() {
        draggedItems.clear();
        draggingBox.getElements().clear();
        getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .forEach(cell -> {
                    if (cell.getGraphic() != null) {
                        cell.pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, false);
                    }
                });
        setDragging(false);
        virtualFlow.setMouseTransparent(false);
    }

}
