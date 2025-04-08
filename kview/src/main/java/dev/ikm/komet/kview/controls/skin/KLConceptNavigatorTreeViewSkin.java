package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.ConceptTile;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell;
import dev.ikm.komet.kview.controls.MultipleSelectionContextMenu;
import dev.ikm.komet.kview.controls.SingleSelectionContextMenu;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem.STATE;
import static dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem.PS_STATE;

public class KLConceptNavigatorTreeViewSkin extends TreeViewSkin<ConceptFacade> {

    private static final PseudoClass MULTIPLE_SELECTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("multiple");

    private final Label header;
    private ConceptNavigatorVirtualFlow virtualFlow;
    private final Path draggingBox;
    private Group sheet;
    private double x, y;
    private double xMin, yMin, xMax, yMax;

    private final List<ConceptNavigatorTreeItem> draggedItems = new ArrayList<>();
    private final Map<ConceptNavigatorTreeItem, WritableImage> imageMap = new HashMap<>();

    private MultipleSelectionContextMenu multipleSelectionContextMenu;
    private final SingleSelectionContextMenu singleSelectionContextMenu;

    public KLConceptNavigatorTreeViewSkin(KLConceptNavigatorControl treeView) {
        super(treeView);
        header = new Label();
        header.getStyleClass().add("concept-header");
        header.textProperty().bind(treeView.headerProperty());
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

        ObservableList<TreeItem<ConceptFacade>> selectedItems = treeView.getSelectionModel().getSelectedItems();
        selectedItems.addListener((ListChangeListener<TreeItem<ConceptFacade>>) c -> {
            boolean multiple = selectedItems.size() > 1;
            setMultipleSelectionByClicking(multiple);
            if (multiple) {
                unhoverAllItems();
                unselectAllItems();
            }
            setupContextMenu(selectedItems.stream().map(TreeItem::getValue).toList());
            while (c.next()) {
                if (c.wasAdded()) {
                    pseudoClassStateChanged(MULTIPLE_SELECTION_PSEUDO_CLASS, true);
                    for (TreeItem<ConceptFacade> item : c.getAddedSubList()) {
                        ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) item;
                        getCellForTreeItem(model).ifPresent(cell -> {
                            if (cell.getGraphic() instanceof ConceptTile tile) {
                                imageMap.put(model, tile.getTileSnapshot());
                            }
                        });
                    }
                } else if (c.wasRemoved()) {
                    for (TreeItem<ConceptFacade> item : c.getRemoved()) {
                        imageMap.remove((ConceptNavigatorTreeItem) item);
                    }
                }
            }
            pseudoClassStateChanged(MULTIPLE_SELECTION_PSEUDO_CLASS, multiple);
        });
        treeView.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (isMultipleSelectionByBoundingBox() &&
                    !new Rectangle2D(xMin, yMin, xMax - xMin, yMax - yMin).contains(e.getSceneX(), e.getSceneY())) {
                setMultipleSelectionByBoundingBox(false);
            }
            x = e.getX();
            y = e.getY();
        });
        treeView.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isDraggingAllowed() && draggedItems.isEmpty() && !isMultipleSelectionByClicking()) {
                setMultipleSelectionByBoundingBox(true);
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
            if (isMultipleSelectionByBoundingBox()) {
                prepareDrag();
                if (draggedItems.isEmpty()) {
                    setMultipleSelectionByBoundingBox(false);
                }
                e.consume();
            }
        });

        treeView.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
            if (isMultipleSelectionByClicking()) {
                draggedItems.clear();
                draggedItems.addAll(selectedItems.stream()
                        .map(ConceptNavigatorTreeItem.class::cast)
                        .toList());
            }
            if (!draggedItems.isEmpty()) {
                List<UUID[]> list = draggedItems.stream()
                        .filter(i -> i.getValue() != null && i.getValue().publicId() != null)
                        .map(i -> i.getValue().publicId().asUuidArray())
                        .toList();
                Dragboard dragboard = getSkinnable().startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT, list);
                dragboard.setContent(clipboardContent);
                WritableImage snapshot = createSnapshot();
                dragboard.setDragView(snapshot);
                e.consume();
            }
        });
        treeView.setOnDragDone(e -> {
            setMultipleSelectionByBoundingBox(false);
            setMultipleSelectionByClicking(false);
        });

        singleSelectionContextMenu = new SingleSelectionContextMenu();
        treeView.setOnContextMenuRequested(e -> {
            if (selectedItems.isEmpty() && draggedItems.isEmpty()) {
                return;
            }
            if (selectedItems.size() < 2 && draggedItems.size() < 2) {
                List<ConceptFacade> relatedConcepts;
                if (!selectedItems.isEmpty()) {
                    // DUMMY! Just the children of the concept, if any
                    relatedConcepts = selectedItems.getFirst().getChildren().stream()
                            .limit(5)
                            .map(TreeItem::getValue)
                            .toList();
                } else {
                    // DUMMY! Just the children of the concept, if any
                    relatedConcepts = draggedItems.getFirst().getChildren().stream()
                            .limit(5)
                            .map(TreeItem::getValue)
                            .toList();
                }
                singleSelectionContextMenu.setRelatedByMessageItems(relatedConcepts,
                        // DUMMY action
                        ev -> System.out.println(((MenuItem) ev.getSource()).getText()));
                singleSelectionContextMenu.show(treeView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            } else if (multipleSelectionContextMenu != null) {
                multipleSelectionContextMenu.show(treeView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            }
        });
    }

    @Override
    protected VirtualFlow<TreeCell<ConceptFacade>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    // multipleSelectionByBoundingBoxProperty
    private final BooleanProperty multipleSelectionByBoundingBoxProperty = new SimpleBooleanProperty(this, "multipleSelectionByBoundingBox") {
        @Override
        protected void invalidated() {
            if (get()) {
                getSkinnable().getSelectionModel().clearSelection();
            } else {
                cancelDrag();
            }
        }
    };
    public final boolean isMultipleSelectionByBoundingBox() {
       return multipleSelectionByBoundingBoxProperty.get();
    }
    public final void setMultipleSelectionByBoundingBox(boolean value) {
        multipleSelectionByBoundingBoxProperty.set(value);
    }

    // multipleSelectionByClickingProperty
    private final BooleanProperty multipleSelectionByClickingProperty = new SimpleBooleanProperty(this, "multipleSelectionByClicking") {
        @Override
        protected void invalidated() {
            if (!get()) {
                draggedItems.clear();
                getSkinnable().getSelectionModel().clearSelection();
            }
        }
    };
    public final boolean isMultipleSelectionByClicking() {
       return multipleSelectionByClickingProperty.get();
    }
    public final void setMultipleSelectionByClicking(boolean value) {
        multipleSelectionByClickingProperty.set(value);
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
        iterateTree((ConceptNavigatorTreeItem) getSkinnable().getRoot(), model -> {
            model.getBitSet().clear(statesBits.getFirst(), statesBits.getLast());
            markCellDirty(model);
        });
        virtualFlow.requestLayout();
    }

    public void selectAllAncestors(ConceptNavigatorTreeItem child) {
        markAllAncestors(child, true);
    }

    public void hoverAllAncestors(ConceptNavigatorTreeItem child) {
        markAllAncestors(child, false);
    }

    private void markAllAncestors(ConceptNavigatorTreeItem child, boolean select) {
        if (isMultipleSelectionByClicking()) {
            return;
        }
        ConceptNavigatorTreeItem model = child;
        while (model != null) {
            // for each ancestor (including starting one)
            if (model == child) {
                if (!select) {
                    // mark current item as long-hovered
                    model.getBitSet().set(PS_STATE.LONG_HOVER.getBit());
                }
            } else {
                // mark ancestor as selected/long-hovered
                model.getBitSet().set(select ? PS_STATE.BORDER_SELECTED.getBit() : PS_STATE.BORDER_LONG_HOVER.getBit());
            }
            // show curved-line
            model.getBitSet().set(select ? PS_STATE.CURVED_LINE_SELECTED.getBit() : PS_STATE.CURVED_LINE_LONG_HOVER.getBit());
            markCellDirty(model);

            int level = getSkinnable().getTreeItemLevel(model) - 2;
            if (level >= 0) {
                // for each previous sibling of this ancestor:
                ConceptNavigatorTreeItem previousSibling = (ConceptNavigatorTreeItem) model.previousSibling();
                while (previousSibling != null) {
                    // and all expanded descendants if these are expanded
                    applyPseudoClassState(previousSibling, select, level);
                    previousSibling = (ConceptNavigatorTreeItem) previousSibling.previousSibling();
                }
            }
            model = (ConceptNavigatorTreeItem) model.getParent();
        }
        virtualFlow.requestLayout();

        // debug:
//        printTree(getSkinnable().getRoot(), false);

    }

    private void applyPseudoClassState(ConceptNavigatorTreeItem model, boolean select, int level) {
        model.getBitSet().set(select ? PS_STATE.LINE_I_SELECTED.getBit() + level : PS_STATE.LINE_I_LONG_HOVER.getBit() + level);
        markCellDirty(model);
        if (model.isExpanded()) {
            model.getChildren().forEach(i -> applyPseudoClassState((ConceptNavigatorTreeItem) i, select, level));
        }
    }

    private void markCellDirty(ConceptNavigatorTreeItem treeItem) {
        getCellForTreeItem(treeItem).ifPresent(KLConceptNavigatorTreeCell::markCellDirty);
    }

    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(ConceptNavigatorTreeItem treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast)
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    private void iterateTree(ConceptNavigatorTreeItem treeItem, Consumer<ConceptNavigatorTreeItem> consumer){
        for (TreeItem<ConceptFacade> child : treeItem.getChildren()) {
            ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) child;
            consumer.accept(model);
            if (!child.isLeaf()) {
                iterateTree(model, consumer);
            }
        }
    }

    private void printTree(ConceptNavigatorTreeItem treeItem, boolean printAll) {
        for (TreeItem<ConceptFacade> child : treeItem.getChildren()) {
            ConceptNavigatorTreeItem model = (ConceptNavigatorTreeItem) child;
            if (model.isLeaf()) {
                if (printAll || !model.getBitSet().isEmpty()) {
                    System.out.println("-".repeat(getSkinnable().getTreeItemLevel(model)) + " " + model);
                }
            } else {
                if (printAll || !model.getBitSet().isEmpty()) {
                    System.out.println("+".repeat(getSkinnable().getTreeItemLevel(model)) + " " + model);
                }
                printTree(model, printAll);
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
                                cell.pseudoClassStateChanged(KLConceptNavigatorTreeCell.LONG_HOVER_PSEUDO_CLASS, true);
                                draggedItems.add((ConceptNavigatorTreeItem) cell.getTreeItem());
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
            setupContextMenu(draggedItems.stream().map(ConceptNavigatorTreeItem::getValue).toList());
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
                        cell.pseudoClassStateChanged(KLConceptNavigatorTreeCell.LONG_HOVER_PSEUDO_CLASS, false);
                    }
                });
        setMultipleSelectionByBoundingBox(false);
        virtualFlow.setMouseTransparent(false);
    }

    private WritableImage createSnapshot() {
        SnapshotParameters p = new SnapshotParameters();
        if (isMultipleSelectionByBoundingBox()) {
            Point2D point2D = new Point2D(xMin, yMin);
            point2D = sheet.sceneToLocal(point2D);
            p.setViewport(new Rectangle2D(point2D.getX(), point2D.getY(), xMax - xMin, yMax - yMin));
            return sheet.snapshot(p, null);
        } else if (isMultipleSelectionByClicking()) {
            List<ImageView> list = getSkinnable().getSelectionModel().getSelectedItems().stream()
                    .map(item -> {
                        ImageView imageView = new ImageView(imageMap.get((ConceptNavigatorTreeItem) item));
                        imageView.setPreserveRatio(true);
                        return imageView;
                    })
                    .toList();
            VBox box = new VBox();
            box.getChildren().addAll(list);
            double scale = getSkinnable().getScene().getWindow().getOutputScaleY();
            p.setTransform(new Scale(scale, scale));
            p.setFill(Color.TRANSPARENT);
            return box.snapshot(p, null);
        }
        return null;
    }

    private void setupContextMenu(List<ConceptFacade> items) {
        multipleSelectionContextMenu = new MultipleSelectionContextMenu();
        multipleSelectionContextMenu.setPopulateMessageAction(_ -> {
            if (((KLConceptNavigatorControl) getSkinnable()).getOnAction() != null) {
                ((KLConceptNavigatorControl) getSkinnable()).getOnAction().accept(items);
            }
            setMultipleSelectionByClicking(false);
            setMultipleSelectionByBoundingBox(false);
        });
        multipleSelectionContextMenu.setJournalMessageAction(e -> System.out.println("Journal action"));
        multipleSelectionContextMenu.setChapterMessageAction(e -> System.out.println("Chapter action"));
        multipleSelectionContextMenu.setCopyMessageAction(e -> System.out.println("Copy action"));
        multipleSelectionContextMenu.setSaveMessageAction(e -> System.out.println("Save action"));
    }
}
