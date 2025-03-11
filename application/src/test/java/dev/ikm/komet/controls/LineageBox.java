package dev.ikm.komet.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class LineageBox extends VBox {

    private static final PseudoClass ROOT_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("root-lineage");
    private static final PseudoClass COLLAPSED_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed-lineage");
    private final StackPane closePane;
    private final KLConceptNavigatorControl conceptNavigator;

    public LineageBox(KLConceptNavigatorControl conceptNavigator) {
        this.conceptNavigator = conceptNavigator;
        IconRegion closeIconRegion = new IconRegion("icon", "close");
        closePane = new StackPane(closeIconRegion);
        closePane.getStyleClass().addAll("region", "close");
        closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            ConceptNavigatorTreeItem concept = getConcept();
            if (concept != null) {
                concept.setViewLineage(false);
                concept.resetViewLineageBitSet();
                setConcept(null);
            }
            e.consume();
        });
        closePane.setManaged(false);
        getChildren().add(closePane);

        getStyleClass().add("lineage-box");
        setManaged(false);
    }

    // conceptProperty
    private final ObjectProperty<ConceptNavigatorTreeItem> conceptProperty = new SimpleObjectProperty<>(this, "concept") {

        @Override
        protected void invalidated() {
            initialize();
        }
    };
    public final ObjectProperty<ConceptNavigatorTreeItem> conceptProperty() {
       return conceptProperty;
    }
    public final ConceptNavigatorTreeItem getConcept() {
       return conceptProperty.get();
    }
    public final void setConcept(ConceptNavigatorTreeItem value) {
        conceptProperty.set(value);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = closePane.prefWidth(getHeight());
        double h = closePane.prefHeight(getWidth());
        closePane.resizeRelocate(getWidth() - w - getInsets().getRight(), getInsets().getTop(), w, h);
    }

    private void initialize() {
        getChildren().removeIf(HBox.class::isInstance);
        ConceptNavigatorTreeItem childItem = getConcept();
        if (childItem != null && childItem.getValue() != null) {
            // primary parent under current tree hierarchy
            ConceptNavigatorTreeItem primaryParentItem = (ConceptNavigatorTreeItem) childItem.getParent();
            if (primaryParentItem != null) {
                // list of secondary parents for the concept child, different from the primary parent item
                List<ConceptNavigatorTreeItem> secondaryParents = conceptNavigator.
                        getSecondaryParents(childItem.getValue().nid(), primaryParentItem.getValue().nid());
                childItem.setExtraParents(secondaryParents);
                // for the concept child, add all its direct secondary parents, all collapsed
                for (ConceptNavigatorTreeItem extraParentItem : secondaryParents) {
                    getChildren().addFirst(new ParentHBox(this, extraParentItem, extraParentItem));
                    // restore expanded items
                    ConceptNavigatorTreeItem parent = (ConceptNavigatorTreeItem) extraParentItem.getParent();
                    if (parent != null) {
                        int index = 1;
                        while (parent != null && childItem.getViewLineageBitSet(extraParentItem).get(index)) {
                            getChildren().addFirst(new ParentHBox(this, parent, extraParentItem));
                            parent = (ConceptNavigatorTreeItem) parent.getParent();
                            index++;
                        }
                    }
                }
                requestLayout();
            }
        }
    }

    private class ParentHBox extends HBox {

        private final LineageBox lineageBox;
        private final ConceptNavigatorTreeItem extraParentItem;


        public ParentHBox(LineageBox lineageBox, ConceptNavigatorTreeItem treeItem, ConceptNavigatorTreeItem extraParentItem) {
            this.lineageBox = lineageBox;
            this.extraParentItem = extraParentItem;

            Region spacer = new Region();
            int level = conceptNavigator.getDepthOfTreeItem(treeItem, getAlternateChildWithNid(extraParentItem, getConcept().getValue().nid()));
            int spacerWidth = 128 - 8 * level;
            spacer.setMinSize(spacerWidth, 1);
            spacer.setPrefSize(spacerWidth, 1);
            spacer.setMaxSize(spacerWidth, 1);

            IconRegion iconRegion = new IconRegion("icon");
            StackPane regionPane = new StackPane(iconRegion);
            regionPane.getStyleClass().add("region");

            Label label = getConceptLabel(treeItem, level);
            HBox.setHgrow(label, Priority.ALWAYS);

            getChildren().addAll(spacer, regionPane, label);
            setUserData(treeItem.getValue().nid());
            boolean isRoot = conceptNavigator.getAllSecondaryParents(treeItem.getValue().nid()).isEmpty();
            pseudoClassStateChanged(ROOT_LINEAGE_PSEUDO_CLASS, isRoot);
            if (isRoot) { // item is root
                iconRegion.getStyleClass().add("root-angle");
            } else if (level == 1) { // item is first secondary parent
                iconRegion.getStyleClass().add("angle-circle-line");
                pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, !getConcept().getViewLineageBitSet(extraParentItem).get(level));
            } else {
                boolean hasMultipleParents = conceptNavigator.getAllSecondaryParents(treeItem.getValue().nid()).size() > 1;
                iconRegion.getStyleClass().add(hasMultipleParents ? "angle-circle-angle" : "angle");
                pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, !getConcept().getViewLineageBitSet(extraParentItem).get(level));
            }
            getStyleClass().add("lineage-hbox");
        }

        private Label getConceptLabel(ConceptNavigatorTreeItem treeItem, int level) {
            Label label = new Label(treeItem.getValue().description());
            label.setOnMouseClicked(e -> {
                label.getParent().pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, false);
                getConcept().getViewLineageBitSet(extraParentItem).set(level, true);
                conceptNavigator.getAllSecondaryParents(treeItem.getValue().nid()).stream()
                        .filter(item -> lineageBox.getChildren().stream()
                                .noneMatch(n -> n.getUserData() != null && n.getUserData().equals(item.getValue().nid())))
                        .forEach(item -> lineageBox.getChildren().addFirst(new ParentHBox(lineageBox, item, extraParentItem)));
            });
            label.getStyleClass().add("lineage-label");
            return label;
        }

        private ConceptNavigatorTreeItem getAlternateChildWithNid(ConceptNavigatorTreeItem treeItem, int nid) {
            return treeItem.getChildren().stream()
                    .filter(ConceptNavigatorTreeItem.class::isInstance)
                    .map(ConceptNavigatorTreeItem.class::cast)
                    .filter(c -> c.getValue().nid() == nid)
                    .findFirst()
                    .orElseThrow();
        }
    }
}
