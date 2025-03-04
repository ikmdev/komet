package dev.ikm.komet.controls;

import dev.ikm.komet.controls.skin.KLConceptNavigatorTreeViewSkin;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import javafx.util.Subscription;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static dev.ikm.komet.controls.KLConceptNavigatorTreeCell.CONCEPT_NAVIGATOR_DRAG_FORMAT;

public class ConceptTile extends HBox {

    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass LEAF_PSEUDO_CLASS = PseudoClass.getPseudoClass("leaf");
    private static final PseudoClass DEFINED_PSEUDO_CLASS = PseudoClass.getPseudoClass("defined");
    public static final PseudoClass DRAG_SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("drag-selected");

    private final KLConceptNavigatorTreeCell cell;
    private final KLConceptNavigatorControl treeView;
    private KLConceptNavigatorTreeViewSkin treeViewSkin;

    private final IconRegion disclosureIconRegion;
    private final StackPane disclosurePane;
    private final Label label;
    private final ConceptNavigatorTooltip tooltip;
    private final ResourceBundle resources = ResourceBundle.getBundle("dev.ikm.komet.controls.concept-navigator");

    private Subscription subscription;
    private PauseTransition hoverTransition;

    public ConceptTile(KLConceptNavigatorTreeCell cell, KLConceptNavigatorControl treeView) {
        this.cell = cell;
        this.treeView = treeView;
        treeView.skinProperty().subscribe(skin -> this.treeViewSkin = (KLConceptNavigatorTreeViewSkin) skin);

        disclosureIconRegion = new IconRegion();
        disclosurePane = new StackPane(disclosureIconRegion);
        disclosurePane.getStyleClass().add("region");
        disclosurePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (cell.getTreeItem() != null) {
                cell.getTreeItem().setExpanded(!cell.getTreeItem().isExpanded());
                e.consume();
            }
        });
        IconRegion selectIconRegion = new IconRegion("icon", "select");
        StackPane selectPane = new StackPane(selectIconRegion);
        selectPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            cell.pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, true);
            e.consume();
        });
        selectPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            cell.pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, false);
            e.consume();
        });
        selectPane.getStyleClass().addAll("region", "select");

        IconRegion treeIconRegion = new IconRegion("icon", "tree");
        StackPane treePane = new StackPane(treeIconRegion);
        Tooltip treeTooltip = new Tooltip(resources.getString("alternate.parents")) {

            @Override
            protected void show() {
                final Bounds bounds = treePane.localToScreen(treePane.getBoundsInLocal());
                Point2D anchor = new Point2D(bounds.getMaxX() + 2, bounds.getMinY());
                setAnchorX(anchor.getX());
                setAnchorY(anchor.getY());
                super.show();
            }
        };
        Tooltip.install(treePane, treeTooltip);
        treePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            getConcept().setExpanded(!getConcept().isExpanded());
            e.consume();
        });
        treePane.getStyleClass().addAll("region", "tree");

        Region ellipse = new Region();
        ellipse.getStyleClass().add("ellipse");
        label = new Label(null);
        label.getStyleClass().add("concept-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(disclosurePane, ellipse, label, spacer, selectPane, treePane);
        getStyleClass().add("concept-tile");

        tooltip = new ConceptNavigatorTooltip(label);
        tooltip.showDelayProperty().bind(Bindings.createObjectBinding(() ->
                new Duration(treeView.getActivation()), treeView.activationProperty()));
        tooltip.setHideDelay(Duration.ZERO);

        selectPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            e.consume();
            treeViewSkin.setDraggingAllowed(false);
        });
        selectPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> treeViewSkin.setDraggingAllowed(true));
        selectPane.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
            if (!cell.isEmpty()) {
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                ConceptNavigatorModel item = cell.getItem();
                if (item.getModel() != null && item.getModel().publicId() != null) {
                    clipboardContent.put(CONCEPT_NAVIGATOR_DRAG_FORMAT,
                            List.<UUID[]>of(item.getModel().publicId().asUuidArray()));
                }
                clipboardContent.putString(item.toString());
                dragboard.setContent(clipboardContent);
                dragboard.setDragView(getTileSnapshot());
                cell.pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, false);
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

    public void cleanup() {
        setConcept(null);
    }

    // conceptProperty
    private final ObjectProperty<ConceptNavigatorModel> conceptProperty = new SimpleObjectProperty<>(this, "concept") {
        @Override
        protected void invalidated() {
            if (subscription != null) {
                subscription.unsubscribe();
            }
            cell.expandedProperty().unbind();
            cell.expandedProperty().set(false);
            ConceptNavigatorModel model = get();
            if (model != null) {
                TreeItem<ConceptNavigatorModel> treeItem = cell.getTreeItem();
                disclosureIconRegion.getStyleClass().setAll("icon", treeItem.isLeaf() ? "leaf" : "disclosure");
                subscription = treeItem.expandedProperty().subscribe(e -> disclosurePane.pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, e));
                subscription = subscription.and(treeItem.leafProperty().subscribe(l -> disclosurePane.pseudoClassStateChanged(LEAF_PSEUDO_CLASS, l)));
                subscription = subscription.and(hoverProperty().subscribe(h -> {
                    if (h) {
                        treeViewSkin.unhoverAllItems();
                        if (treeView.getSelectionModel().getSelectedItem() == treeItem || treeViewSkin.isMultipleSelectionByBoundingBox()) {
                            // don't long-hover the selected item or if there's a treeView dragging event
                            return;
                        }
                        hoverTransition = new PauseTransition(new Duration(treeView.getActivation()));
                        hoverTransition.setOnFinished(e -> treeViewSkin.hoverAllAncestors(treeItem));
                        hoverTransition.playFromStart();
                    }
                }));

                String description = model.getModel() != null ? model.getModel().description() : "";
                label.setText(description);
                pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, model.isDefined());
                cell.expandedProperty().bind(model.expandedProperty());
            } else {
                unselectItem();
                label.setText(null);
                pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, false);
            }
        }
    };
    public final ObjectProperty<ConceptNavigatorModel> conceptProperty() {
       return conceptProperty;
    }
    public final ConceptNavigatorModel getConcept() {
       return conceptProperty.get();
    }
    public final void setConcept(ConceptNavigatorModel value) {
        conceptProperty.set(value);
    }

    public void updateTooltip() {
        tooltip.setGraphicText(getConcept().isDefined() ? resources.getString("defined.concept") : resources.getString("primitive.concept"));
        tooltip.getGraphic().pseudoClassStateChanged(DEFINED_PSEUDO_CLASS, getConcept().isDefined());
        Node lookup = label.lookup(".text");
        String description = getConcept().getModel() != null ? getConcept().getModel().description() : "";
        if (lookup instanceof Text labelledText && !labelledText.getText().equals(description)) {
            tooltip.setText(description);
        } else {
            tooltip.setText(null);
        }
    }

    public WritableImage getTileSnapshot() {
        SnapshotParameters p = new SnapshotParameters();
        double scale = getScene().getWindow().getOutputScaleY();
        p.setTransform(new Scale(scale, scale));
        return snapshot(p, null);
    }

}
