package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeCellSkin;
import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import static dev.ikm.komet.controls.ConceptNavigatorModel.PS_STATE;
import static dev.ikm.komet.controls.ConceptNavigatorModel.MAX_LEVEL;

public class KLConceptNavigatorTreeCell extends TreeCell<ConceptNavigatorModel> {

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass LEAF_PSEUDO_CLASS = PseudoClass.getPseudoClass("leaf");
    private static final PseudoClass DEFINED_PSEUDO_CLASS = PseudoClass.getPseudoClass("defined");
    private static final PseudoClass DRAG_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("drag-selected");

    public static final PseudoClass LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-long-hover");
    private static final PseudoClass BORDER_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-long-hover");
    private static final PseudoClass BORDER_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-border-selected");

    private static final PseudoClass CURVED_LINE_LONG_HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-long-hover");
    private static final PseudoClass CURVED_LINE_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("cn-curved-line-selected");
    public static final PseudoClass[] LINE_I_LONG_HOVER_PSEUDO_CLASS;
    public static final PseudoClass[] LINE_I_SELECTED_PSEUDO_CLASS;
    static {
        LINE_I_LONG_HOVER_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        LINE_I_SELECTED_PSEUDO_CLASS = new PseudoClass[MAX_LEVEL];
        for (int i = 0; i < MAX_LEVEL; i++) {
            LINE_I_LONG_HOVER_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-long-hover-" + i);
            LINE_I_SELECTED_PSEUDO_CLASS[i] = PseudoClass.getPseudoClass("cn-line-selected-" + i);
        }
    }

    public static final DataFormat CONCEPT_NAVIGATOR_DRAG_FORMAT;
    static {
        DataFormat dataFormat = DataFormat.lookupMimeType("object/concept-navigator-format");
        CONCEPT_NAVIGATOR_DRAG_FORMAT = dataFormat == null ? new DataFormat("object/concept-navigator-format") : dataFormat;
    }

    private final Label label;
    private final HBox box;
    private final ConceptNavigatorTooltip tooltip;

    private final KLConceptNavigatorControl treeView;
    private KLConceptNavigatorTreeViewSkin treeViewSkin;
    private KLConceptNavigatorTreeCellSkin myTreeCellSkin;

    private PauseTransition hoverTransition;
    private Subscription subscription;

    public KLConceptNavigatorTreeCell(KLConceptNavigatorControl treeView) {
        this.treeView = treeView;

        IconRegion disclosureIconRegion = new IconRegion();
        StackPane disclosurePane = new StackPane(disclosureIconRegion);
        disclosurePane.getStyleClass().add("region");
        disclosurePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (getTreeItem() != null) {
                getTreeItem().setExpanded(!getTreeItem().isExpanded());
                e.consume();
            }
        });
        IconRegion selectIconRegion = new IconRegion("icon", "select");
        StackPane selectPane = new StackPane(selectIconRegion);
        selectPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, true);
            e.consume();
        });
        selectPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, false);
            e.consume();
        });
        selectPane.getStyleClass().addAll("region", "select");

        IconRegion treeIconRegion = new IconRegion("icon", "tree");
        StackPane treePane = new StackPane(treeIconRegion);
        treePane.getStyleClass().addAll("region", "tree");

        Region ellipse = new Region();
        ellipse.getStyleClass().add("ellipse");
        label = new Label(null);
        label.getStyleClass().add("concept-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box = new HBox(disclosurePane, ellipse, label, spacer, selectPane, treePane);
        box.getStyleClass().add("cell-box");

        this.treeView.skinProperty().subscribe(skin -> this.treeViewSkin = (KLConceptNavigatorTreeViewSkin) skin);
        treeItemProperty().subscribe(tree -> {
            if (tree != null) {
                disclosureIconRegion.getStyleClass().setAll("icon", tree.isLeaf() ? "leaf" : "disclosure");
                subscription = tree.expandedProperty().subscribe(e -> disclosurePane.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, e));
                subscription = subscription.and(tree.leafProperty().subscribe(l -> disclosurePane.pseudoClassStateChanged(LEAF_PSEUDO_CLASS, l)));
                subscription = subscription.and(box.hoverProperty().subscribe(h -> {
                    if (h) {
                        treeViewSkin.unhoverAllItems();
                        if (treeView.getSelectionModel().getSelectedItem() == getTreeItem() || treeViewSkin.isDragging()) {
                            // don't long-hover the selected item or if there's a treeView dragging event
                            return;
                        }
                        hoverTransition = new PauseTransition(new Duration(treeView.getActivation()));
                        hoverTransition.setOnFinished(e -> treeViewSkin.hoverAllAncestors(getTreeItem()));
                        hoverTransition.playFromStart();
                    }
                }));
            } else {
                cleanup();
            }
        });

        disclosureNodeProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (getDisclosureNode() != null) {
                    setDisclosureNode(null);
                    disclosureNodeProperty().removeListener(this);
                }
            }
        });
        setText(null);

        tooltip = new ConceptNavigatorTooltip(box);
        tooltip.showDelayProperty().bind(Bindings.createObjectBinding(() ->
                new Duration(treeView.getActivation()), treeView.activationProperty()));
        tooltip.setHideDelay(Duration.ZERO);

        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getClickCount() == 2 && !isEmpty()) {
                if (treeView.getOnDoubleClick() != null) {
                    treeView.getOnDoubleClick().accept(getItem());
                }
                e.consume();
            }
        });

        selectPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            e.consume();
            treeViewSkin.setDraggingAllowed(false);
        });
        selectPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> treeViewSkin.setDraggingAllowed(true));
        selectPane.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
            if (!isEmpty()) {
                Dragboard dragboard = box.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                if (getItem().getModel() != null && getItem().getModel().publicId() != null) {
                    clipboardContent.put(CONCEPT_NAVIGATOR_DRAG_FORMAT,
                            List.<UUID[]>of(getItem().getModel().publicId().asUuidArray()));
                }
                clipboardContent.putString(getItem().toString());
                dragboard.setContent(clipboardContent);
                SnapshotParameters p = new SnapshotParameters();
                double scale = getScene().getWindow().getOutputScaleY();
                p.setTransform(new Scale(scale, scale));
                WritableImage snapshot = box.snapshot(p, null);
                dragboard.setDragView(snapshot);
                pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, false);
                treeViewSkin.setDraggingAllowed(true);
            }
            e.consume();
        });
        selectPane.setOnDragDone(e -> treeViewSkin.setDraggingAllowed(true));
    }

    public void unselectItem() {
        if (hoverTransition != null) {
            hoverTransition.stop();
            hoverTransition = null;
        }
    }

    private void cleanup() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        unselectItem();
        getChildren().removeIf(Path.class::isInstance);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
        box.pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, false);
    }

    @Override
    public void updateIndex(int newIndex) {
        super.updateIndex(newIndex);
        if (newIndex == -1) {
            cleanup();
        }
    }

    private boolean itemDirty = false;
    public final void markCellDirty() {
        itemDirty = true;
        requestLayout();
    }

    @Override
    protected void layoutChildren() {
        if (itemDirty) {
            updateItem(getItem(), isEmpty());
            itemDirty = false;
        }
        super.layoutChildren();
    }

    @Override
    protected void updateItem(ConceptNavigatorModel item, boolean empty) {
        super.updateItem(item, empty);
        getPseudoClassStates().stream()
                .filter(p -> p.getPseudoClassName().startsWith("cn-"))
                .forEach(p -> pseudoClassStateChanged(p, false));
        if (item != null && !empty) {
            label.setText(item.getModel() != null ? item.getModel().description() : "");
            box.pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, item.isDefined());
            setGraphic(box);
            updateConnections();
            updateState(item.getBitSet());
            updateTooltip(item);
        } else {
            cleanup();
            setGraphic(null);
        }
    }

    private void updateTooltip(ConceptNavigatorModel item) {
        tooltip.setGraphicText(item.isDefined() ? "Defined Concept" : "Primitive Concept");
        tooltip.getGraphic().pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, item.isDefined());
        Node lookup = label.lookup(".text");
        String description = item.getModel() != null ? item.getModel().description() : "";
        if (lookup instanceof Text labelledText && !labelledText.getText().equals(description)) {
            tooltip.setText(description);
        } else {
            tooltip.setText(null);
        }
    }

    private void updateState(BitSet bitSet) {
        pseudoClassStateChanged(LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_LONG_HOVER.getBit()));
        pseudoClassStateChanged(BORDER_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.BORDER_SELECTED.getBit()));
        pseudoClassStateChanged(CURVED_LINE_LONG_HOVER_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_LONG_HOVER.getBit()));
        pseudoClassStateChanged(CURVED_LINE_SELECTED_PSEUDO_CLASS, bitSet.get(PS_STATE.CURVED_LINE_SELECTED.getBit()));
        for (int i = 0; i < Math.min(getLevel(getTreeItem()), MAX_LEVEL); i++) {
            pseudoClassStateChanged(LINE_I_LONG_HOVER_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_LONG_HOVER.getBit() + i));
            pseudoClassStateChanged(LINE_I_SELECTED_PSEUDO_CLASS[i], bitSet.get(PS_STATE.LINE_I_SELECTED.getBit() + i));
        }
    }

    private void updateConnections() {
        TreeItem<ConceptNavigatorModel> treeItem = getTreeItem();
        if (treeView == null || myTreeCellSkin == null || treeItem == null) {
            return;
        }

        List<Path> oldPaths = getChildren().stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .toList();
        int level = getLevel(treeItem);
        double indent = myTreeCellSkin.getIndent();
        List<Path> paths = new ArrayList<>();
        int start = treeView.isShowRoot() ? 1 : 0;
        for (int i = start; i < level; i++) {
            double x = 10 + indent * i;
            if (i < level - 1) {
                TreeItem<ConceptNavigatorModel> ancestor = getAncestor(treeItem, i + 1);
                if (ancestor.nextSibling() != null) {
                    paths.add(getLine(x, "dashed-line"));
                    paths.add(getLine(x, "solid-line-" + (i - start)));
                }
            } else {
                paths.add(getCurvedLine(x, treeItem.nextSibling() == null, "dashed-curved-line"));
                paths.add(getLine(x, "solid-line-" + (i - start)));
                paths.add(getCurvedLine(x, true, "solid-curved-line"));
            }
        }
        if (!oldPaths.equals(paths)) {
            getChildren().removeAll(oldPaths);
            getChildren().addAll(paths);
        }
    }

    private TreeItem<ConceptNavigatorModel> getAncestor(TreeItem<ConceptNavigatorModel> treeItem, int level) {
        if (treeItem == null) {
            return null;
        }
        TreeItem<ConceptNavigatorModel> ancestor = treeItem.getParent();
        while (ancestor != null) {
            if (getLevel(ancestor) == level) {
                return ancestor;
            }
            ancestor = ancestor.getParent();
        }
        return null;
    }

    private int getLevel(TreeItem<ConceptNavigatorModel> treeItem) {
        return treeView.getTreeItemLevel(treeItem) - (treeView.isShowRoot() ? 0 : 1);
    }

    private Path getLine(double x, String styleClass) {
        Path line = new Path();
        line.getElements().addAll(new MoveTo(x, 0), new LineTo(x, 24));
        line.getStyleClass().add(styleClass);
        return line;
    }

    private Path getCurvedLine(double x, boolean isLastSibling, String styleClass) {
        Path curvedLine = new Path();
        if (isLastSibling) {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, 7),
                    new ArcTo(5, 5, 90, x + 5, 12, false, false)
            );
        } else {
            curvedLine.getElements().addAll(
                    new MoveTo(x, 0), new LineTo(x, 24), new MoveTo(x, 7),
                    new ArcTo(5, 5, 90, x + 5, 12, false, false)
            );
        }
        curvedLine.getStyleClass().add(styleClass);
        return curvedLine;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        myTreeCellSkin = new KLConceptNavigatorTreeCellSkin(this);
        return myTreeCellSkin;
    }

    private static class ConceptNavigatorTooltip extends Tooltip {

        private final Node node;
        private final Label typeTooltipLabel;

        public ConceptNavigatorTooltip(Node node) {
            this.node = node;

            Region ellipse = new Region();
            ellipse.getStyleClass().add("tooltip-ellipse");
            typeTooltipLabel = new Label();
            typeTooltipLabel.getStyleClass().add("type-tooltip-label");
            HBox box = new HBox(ellipse, typeTooltipLabel);
            box.getStyleClass().add("tooltip-box");

            setGraphic(box);
            contentDisplayProperty().bind(
                    Bindings.when(textProperty().isNotEmpty())
                        .then(ContentDisplay.BOTTOM)
                        .otherwise(ContentDisplay.GRAPHIC_ONLY));
            getStyleClass().add("tooltip");

            Tooltip.install(node, this);
        }

        public void setGraphicText(String text) {
            typeTooltipLabel.setText(text);
        }

        @Override
        protected void show() {
            final Bounds bounds = node.localToScreen(node.getBoundsInLocal());
            Point2D anchor = new Point2D(bounds.getMinX() + 18, bounds.getMaxY());
            setAnchorX(anchor.getX());
            setAnchorY(anchor.getY());
            super.show();
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ConceptNavigatorTooltipSkin(this);
        }
    }

    private static class ConceptNavigatorTooltipSkin implements Skin<ConceptNavigatorTooltip> {

        private Label tipLabel;
        private ConceptNavigatorTooltip tooltip;

        public ConceptNavigatorTooltipSkin(ConceptNavigatorTooltip t) {
            this.tooltip = t;
            tipLabel = new Label() {
                @Override
                protected void layoutChildren() {
                    super.layoutChildren();
                    // relocate graphic at bottom-left
                    getGraphic().relocate(getPadding().getLeft(), getGraphic().getLayoutY());
                }
            };
            tipLabel.contentDisplayProperty().bind(t.contentDisplayProperty());
            tipLabel.fontProperty().bind(t.fontProperty());
            tipLabel.graphicProperty().bind(t.graphicProperty());
            tipLabel.graphicTextGapProperty().bind(t.graphicTextGapProperty());
            tipLabel.textAlignmentProperty().bind(t.textAlignmentProperty());
            tipLabel.textOverrunProperty().bind(t.textOverrunProperty());
            tipLabel.textProperty().bind(t.textProperty());
            tipLabel.wrapTextProperty().bind(t.wrapTextProperty());
            tipLabel.minWidthProperty().bind(t.minWidthProperty());
            tipLabel.prefWidthProperty().bind(t.prefWidthProperty());
            tipLabel.maxWidthProperty().bind(t.maxWidthProperty());
            tipLabel.minHeightProperty().bind(t.minHeightProperty());
            tipLabel.prefHeightProperty().bind(t.prefHeightProperty());
            tipLabel.maxHeightProperty().bind(t.maxHeightProperty());

            tipLabel.getStyleClass().setAll(t.getStyleClass());
            tipLabel.setStyle(t.getStyle());
            tipLabel.setId(t.getId());
        }

        @Override
        public ConceptNavigatorTooltip getSkinnable() {
            return tooltip;
        }

        @Override
        public Node getNode() {
            return tipLabel;
        }

        @Override
        public void dispose() {
            tooltip = null;
            tipLabel = null;
        }
    }
}
