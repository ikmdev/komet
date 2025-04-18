package dev.ikm.komet.kview.controls.skin;

import dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.kview.controls.ConceptNavigatorUtils;
import dev.ikm.komet.kview.controls.ConceptTile;
import dev.ikm.komet.kview.controls.KLConceptNavigatorControl;
import dev.ikm.komet.kview.controls.KLConceptNavigatorTreeCell;
import dev.ikm.komet.kview.controls.MultipleSelectionContextMenu;
import dev.ikm.komet.kview.controls.SingleSelectionContextMenu;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
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
import java.util.stream.Stream;

import static dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem.STATE;
import static dev.ikm.komet.kview.controls.ConceptNavigatorTreeItem.PS_STATE;

/**
 * <p>Custom skin implementation for the {@link KLConceptNavigatorControl} control.
 * Uses a {@link ConceptFacade} as the type of the value contained within all the
 * {@link ConceptNavigatorTreeItem} in this treeView.
 * </p>
 * <p>Besides rendering of the {@link KLConceptNavigatorTreeCell} cells,
 * this implementation also takes care of adding a floating header on top of the treeView,
 * a dragging box that allows dragging multiple cells, which are added to the cell as extra nodes
 * and rendered accordingly during the {@link #layoutChildren(double, double, double, double)} pass.
 * </p>
 * <p>The treeView allows for multiple selection of treeItems, which can be done by Ctrl/Cmd+mouse clicking
 * or by mouse dragging. The boolean properties {@link #multipleSelectionByClickingProperty} and
 * {@link #multipleSelectionByBoundingBoxProperty} are set accordingly, to differentiate the origin of
 * the selection. For the former, the treeView selectionModel contains the actual selection of items, but for
 * the latter, there is no real selection, and the selected items have to be found directly in the treeView from
 * the intersection of the dragging box with the treeCells of the treeView.</p>
 * <p>During the drag gesture, an image of the {@link ConceptTile} of each selected item is combined
 * to create the {@link Dragboard#setDragView(Image) dragView}.
 * </p>
 * <p>Two context menus are created, based on single or multiple selection.</p>
 */
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
    private boolean isScrollBarDragging;

    /**
     * <p>Creates a {@link KLConceptNavigatorTreeViewSkin} instance.
     * </p>
     * <p>Creates the floating header and the dragging box instances.</p>
     * <p>Installs the listener for item selection by clicking.</p>
     * <p>Installs the event filters for rendering the dragging box and for item selection by dragging.</p>
     * <p>Installs the context menus.</p>
     * @param treeView The control that this skin should be installed onto
     */
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
        clip.xProperty().bind(virtualFlow.layoutXProperty());
        clip.yProperty().bind(virtualFlow.layoutYProperty());
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
                                imageMap.put(model, ConceptNavigatorUtils.getTileSnapshot(tile));
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
            if (e.getButton() == MouseButton.PRIMARY) {
                if (isMultipleSelectionByBoundingBox() &&
                        !new Rectangle2D(xMin, yMin, xMax - xMin, yMax - yMin).contains(e.getSceneX(), e.getSceneY())) {
                    setMultipleSelectionByBoundingBox(false);
                }
                x = e.getX();
                y = e.getY();
            }
        });
        treeView.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            Point2D point = getSheet().sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));
            if (e.getButton() == MouseButton.PRIMARY && isDraggingAllowed() &&
                    draggedItems.isEmpty() && !isMultipleSelectionByClicking() &&
                    getSheet().contains(point) && !isScrollBarDragging) {
                setMultipleSelectionByBoundingBox(true);
                getSheet().setMouseTransparent(true);
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
        treeView.setOnDragDone(_ -> resetSelection());

        ScrollBar verticalBar = (ScrollBar) treeView.lookup(".scroll-bar:vertical");
        verticalBar.skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (verticalBar.getSkin() != null) {
                    StackPane thumb = (StackPane) verticalBar.lookup(".thumb");
                    thumb.addEventHandler(MouseEvent.MOUSE_PRESSED, _ -> isScrollBarDragging = false);
                    thumb.addEventHandler(MouseEvent.MOUSE_DRAGGED, _ -> isScrollBarDragging = true);
                    thumb.addEventHandler(MouseEvent.MOUSE_RELEASED, _ -> isScrollBarDragging = false);
                    verticalBar.skinProperty().removeListener(this);
                }
            }
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

    /**
     * Boolean read-only property that is set to true if the user selects multiple items of the treeView
     * by mouse dragging
     */
    private final ReadOnlyBooleanWrapper multipleSelectionByBoundingBoxProperty = new ReadOnlyBooleanWrapper(this, "multipleSelectionByBoundingBox") {
        @Override
        protected void invalidated() {
            if (get()) {
                getSkinnable().getSelectionModel().clearSelection();
            } else {
                cancelDrag();
            }
        }
    };
    public final ReadOnlyBooleanProperty multipleSelectionByBoundingBoxProperty() {
        return multipleSelectionByBoundingBoxProperty.getReadOnlyProperty();
    }
    public final boolean isMultipleSelectionByBoundingBox() {
       return multipleSelectionByBoundingBoxProperty.get();
    }
    private void setMultipleSelectionByBoundingBox(boolean b) {
        multipleSelectionByBoundingBoxProperty.set(b);
    }

    /**
     * Boolean read-only property that is set to true if the user selects multiple items of the treeView
     * by mouse clicking
     */
    private final ReadOnlyBooleanWrapper multipleSelectionByClickingProperty = new ReadOnlyBooleanWrapper(this, "multipleSelectionByClicking") {
        @Override
        protected void invalidated() {
            if (!get()) {
                draggedItems.clear();
            }
        }
    };
    public final ReadOnlyBooleanProperty multipleSelectionByClickingProperty() {
        return multipleSelectionByClickingProperty.getReadOnlyProperty();
    }
    public final boolean isMultipleSelectionByClicking() {
       return multipleSelectionByClickingProperty.get();
    }
    private void setMultipleSelectionByClicking(boolean value) {
        multipleSelectionByClickingProperty.set(value);
    }

    /**
     * Boolean field that is set from the {@link ConceptTile}, to allow or deny a drag event with
     * the dragging box, in case the drag event will be taken care directly by the tile itself.
     */
    private boolean draggingAllowed = true;
    public final boolean isDraggingAllowed() {
       return draggingAllowed;
    }
    public final void setDraggingAllowed(boolean value) {
        draggingAllowed = value;
    }

    /**
     * <p>Whenever the selected item in the tree view changes, this method allows for updating
     * the selected state of this item and all its ancestors.
     * </p>
     * @param child the selected {@link ConceptNavigatorTreeItem} for which its ancestors
     *              have to be selected too.
     * @see STATE#SELECTED
     */
    public void selectAllAncestors(ConceptNavigatorTreeItem child) {
        markAllAncestors(child, STATE.SELECTED);
    }

    /**
     * <p>Whenever the long-hovered item in the tree view changes, this method allows for updating
     * the long-hovered state of this item and all its ancestors.
     * </p>
     * @param child the long-hovered {@link ConceptNavigatorTreeItem} for which its ancestors
     *              have to be long-hovered too.
     * @see STATE#LONG_HOVER
     */
    public void hoverAllAncestors(ConceptNavigatorTreeItem child) {
        markAllAncestors(child, STATE.LONG_HOVER);
    }

    /**
     * <p>Whenever the selected item in the tree view changes, or there is no selection, this method
     * removes the selected state of this item and all its siblings and ancestors.
     * </p>
     * @see STATE#SELECTED
     */
    public void unselectAllItems() {
        unmarkAllItems(STATE.SELECTED);
    }

    /**
     * <p>Whenever the long-hovered item in the tree view changes, or there is no long-hovered item, this method
     * removes the long-hovered state of this item and all its siblings and ancestors.
     * </p>
     * @see STATE#LONG_HOVER
     */
    public void unhoverAllItems() {
        unmarkAllItems(STATE.LONG_HOVER);
    }

    /** {@inheritDoc} **/
     @Override
    protected VirtualFlow<TreeCell<ConceptFacade>> createVirtualFlow() {
        virtualFlow = new ConceptNavigatorVirtualFlow();
        return virtualFlow;
    }

    /** {@inheritDoc}
     * Overridden to take care of the floating header, if visible.
     */
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

    /**
     * Gets the group node of the virtual flow, which contains the actual {@link KLConceptNavigatorTreeCell cells}.
     * @return the {@link Group} node with cells.
     */
    private Group getSheet() {
        if (sheet == null) {
            sheet = (Group) virtualFlow.lookup(".sheet");
        }
        return sheet;
    }

    /**
     * <p>Gets a {@link Stream<KLConceptNavigatorTreeCell>} of the cells found in the virtual flow.
     * </p>
     * @return a {@link Stream<KLConceptNavigatorTreeCell>}
     */
    private Stream<KLConceptNavigatorTreeCell> getConceptNavigatorTreeCellStream() {
        return getSheet().getChildren().stream()
                .filter(KLConceptNavigatorTreeCell.class::isInstance)
                .map(KLConceptNavigatorTreeCell.class::cast);
    }

    /**
     * <p>Traverse all the {@link KLConceptNavigatorTreeCell cells} and stop any hovering animation,
     * and iterate over the whole tree, from root to bottom, to reset the range of bits related
     * to the passed {@link STATE}.
     * </p>
     * @param state the selected or long-hovered {@link STATE}
     */
    private void unmarkAllItems(STATE state) {
        getConceptNavigatorTreeCellStream().forEach(ConceptNavigatorHelper::unselectItem);
        ConceptNavigatorUtils.iterateTree((ConceptNavigatorTreeItem) getSkinnable().getRoot(), model -> {
            PS_STATE.clearBitsRange(model.getBitSet(), state);
            markCellDirty(model);
        });
        virtualFlow.requestLayout();
    }

    /**
     * <p>Traverse the tree view, starting from the given {@link ConceptNavigatorTreeItem}, up to its siblings
     * and ancestors, and for each item, set the bits of bitSet that relate to the passed {@link STATE}.
     * </p>
     * @param child the starting {@link ConceptNavigatorTreeItem}
     * @param state the selected or long-hovered {@link STATE}
     */
    private void markAllAncestors(ConceptNavigatorTreeItem child, STATE state) {
        if (isMultipleSelectionByClicking()) {
            return;
        }
        boolean select = state == STATE.SELECTED;
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
                    applyPseudoClassState(previousSibling, state, level);
                    previousSibling = (ConceptNavigatorTreeItem) previousSibling.previousSibling();
                }
            }
            model = (ConceptNavigatorTreeItem) model.getParent();
        }
        virtualFlow.requestLayout();

        // debug:
//        ConceptNavigatorUtils.printTree((KLConceptNavigatorControl) getSkinnable(), (ConceptNavigatorTreeItem) getSkinnable().getRoot(), false);
    }

    /**
     * <p>Recursive method that traverses a {@link ConceptNavigatorTreeItem} and, if is expanded, its children,
     * from top to bottom, setting the bits of bitSet that relate to the passed {@link STATE} at a
     * given indentation level.
     * </p>
     * @param item a {@link ConceptNavigatorTreeItem}
     * @param state the selected or long-hovered {@link STATE}
     * @param level the indentation level
     */
    private void applyPseudoClassState(ConceptNavigatorTreeItem item, STATE state, int level) {
        item.getBitSet().set(state == STATE.SELECTED ? PS_STATE.LINE_I_SELECTED.getBit() + level : PS_STATE.LINE_I_LONG_HOVER.getBit() + level);
        markCellDirty(item);
        if (item.isExpanded()) {
            item.getChildren().forEach(i -> applyPseudoClassState((ConceptNavigatorTreeItem) i, state, level));
        }
    }

    /**
     * <p>Convenience method to mark dirty the {@link KLConceptNavigatorTreeCell} that contains the passed
     * {@link ConceptNavigatorTreeItem}, if found, to force a call to {@link KLConceptNavigatorTreeCell updateItem}
     * in the next layout pass.
     * </p>
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     */
    private void markCellDirty(ConceptNavigatorTreeItem treeItem) {
        getCellForTreeItem(treeItem).ifPresent(ConceptNavigatorHelper::markCellDirty);
    }

    /**
     * <p>Finds the {@link KLConceptNavigatorTreeCell} for the passed {@link ConceptNavigatorTreeItem},
     * within the list of current cells in the virtual flow, if any.
     * </p>
     * @param treeItem a {@link ConceptNavigatorTreeItem}
     * @return an optional of {@link KLConceptNavigatorTreeCell}
     */
    private Optional<KLConceptNavigatorTreeCell> getCellForTreeItem(ConceptNavigatorTreeItem treeItem) {
        if (treeItem == null) {
            return Optional.empty();
        }
        return getConceptNavigatorTreeCellStream()
                .filter(cell -> treeItem.equals(cell.getTreeItem()))
                .findFirst();
    }

    /**
     * <p>When the user drags over the {@link KLConceptNavigatorControl}, after drawing the dragging box,
     * iterate over all the cells in the virtual flow, to find out if the dragging box intersects them.
     * </p>
     * <p>For the intersected cells, sets the long-hovered pseudoClass and adds the item of the cell to the list
     * of dragged items.</p>
     * <p>And finds the bounding box of the graphic node (that is, the {@link ConceptTile}) of the affected cells,
     * in order to create a snapshot</p>
     */
    private void prepareDrag() {
        if (draggingBox.getLayoutBounds().getWidth() < 10 || draggingBox.getLayoutBounds().getHeight() < 10) {
            draggingBox.getElements().clear();
            return;
        }
        if (!draggingBox.getElements().isEmpty()) {
            xMin = Double.MAX_VALUE; yMin = Double.MAX_VALUE;
            xMax = Double.MIN_VALUE; yMax = Double.MIN_VALUE;
            getConceptNavigatorTreeCellStream()
                    .filter(cell -> cell.getGraphic() != null)
                    .forEach(cell -> {
                        Node graphic = cell.getGraphic();
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
                    });
            setupContextMenu(draggedItems.stream().map(ConceptNavigatorTreeItem::getValue).toList());
            draggingBox.getElements().clear();
        }
    }

    /**
     * <p>Resets the list of dragged items, clears the dragging box, and resets the pseudoClass long-hovered state of the
     * cells in the virtual flow.
     * </p>
     */
    private void cancelDrag() {
        draggedItems.clear();
        draggingBox.getElements().clear();
        getConceptNavigatorTreeCellStream()
                .filter(cell -> cell.getGraphic() != null)
                .forEach(cell -> cell.pseudoClassStateChanged(KLConceptNavigatorTreeCell.LONG_HOVER_PSEUDO_CLASS, false));
        getSheet().setMouseTransparent(false);
    }

    /**
     * <p>Creates a snapshot of the selected items that can be passed to {@link Dragboard#setDragView(Image)}.
     * </p>
     * <p>When selection is done via dragging box, it returns snapshot of the bounding rectangle of the continuous
     * selection of {@link ConceptTile}. But when the selection is done via clicking, since this can be discontinuous,
     * the cached snapshot of each item is added to an {@link ImageView}, and those are grouped into a {@link VBox},
     * from which the snapshot is finally taken.</p>
     * @return a {@link WritableImage} of the selected items
     */
    private WritableImage createSnapshot() {
        SnapshotParameters p = new SnapshotParameters();
        if (isMultipleSelectionByBoundingBox()) {
            Point2D point2D = new Point2D(xMin, yMin);
            point2D = getSheet().sceneToLocal(point2D);
            p.setViewport(new Rectangle2D(point2D.getX(), point2D.getY(), xMax - xMin, yMax - yMin));
            return getSheet().snapshot(p, null);
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

    /**
     * <p>Sets up the multiple selection context menu, and uses the list of the selected concepts
     * to set an {@link EventHandler<ActionEvent>} for
     * {@link MultipleSelectionContextMenu#setPopulateMessageAction(EventHandler)}, based on the
     * {@link KLConceptNavigatorControl#onActionProperty()}, if defined.
     * </p>
     * @param items a {@link List<ConceptFacade>}
     */
    private void setupContextMenu(List<ConceptFacade> items) {
        multipleSelectionContextMenu = new MultipleSelectionContextMenu();
        multipleSelectionContextMenu.setPopulateMessageAction(_ -> {
            if (((KLConceptNavigatorControl) getSkinnable()).getOnAction() != null) {
                ((KLConceptNavigatorControl) getSkinnable()).getOnAction().accept(items);
            }
            resetSelection();
        });
        // DUMMY!
        multipleSelectionContextMenu.setJournalMessageAction(e -> System.out.println("Journal action"));
        multipleSelectionContextMenu.setChapterMessageAction(e -> System.out.println("Chapter action"));
        multipleSelectionContextMenu.setCopyMessageAction(e -> System.out.println("Copy action"));
        multipleSelectionContextMenu.setSaveMessageAction(e -> System.out.println("Save action"));
    }

    /**
     * Clears {@link #multipleSelectionByBoundingBoxProperty()} and {@link #multipleSelectionByClickingProperty()},
     * and clears the selection of the treeView.
     */
    private void resetSelection() {
        setMultipleSelectionByBoundingBox(false);
        setMultipleSelectionByClicking(false);
        getSkinnable().getSelectionModel().clearSelection();
    }
}
