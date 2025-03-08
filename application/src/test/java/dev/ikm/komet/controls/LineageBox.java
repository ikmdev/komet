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
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.List;

public class LineageBox extends VBox {

    private static final PseudoClass ROOT_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("root-lineage");
    private static final PseudoClass COLLAPSED_LINEAGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed-lineage");
    private final StackPane closePane;
    private final KLConceptNavigatorControl conceptNavigator;
    private int currentLevel;

    public LineageBox(KLConceptNavigatorControl conceptNavigator) {
        this.conceptNavigator = conceptNavigator;
        IconRegion closeIconRegion = new IconRegion("icon", "close");
        closePane = new StackPane(closeIconRegion);
        closePane.getStyleClass().addAll("region", "close");
        closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (getConcept() != null) {
                getConcept().setViewLineage(false);
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
            currentLevel = -1;
            getChildren().removeIf(HBox.class::isInstance);
            ConceptNavigatorTreeItem treeItem = get();
            if (treeItem != null && treeItem.getValue() != null) {
                currentLevel = conceptNavigator.getTreeItemLevel(treeItem);
                ConceptNavigatorTreeItem parentItem = (ConceptNavigatorTreeItem) treeItem.getParent();
                if (parentItem != null) {
                    List<ConceptNavigatorTreeItem> secondaryParents = conceptNavigator.
                            getSecondaryParents(treeItem.getValue().nid(), parentItem.getValue().nid());
                    treeItem.setExtraParents(secondaryParents);
                    for (ConceptNavigatorTreeItem extraParentItem : secondaryParents) {
                        getChildren().addFirst(getParentBox(extraParentItem));
                    }
                }
            }
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

    private Path getLine(double x, double y, String styleClass) {
        Path line = new Path();
        line.getElements().addAll(new MoveTo(x, y - 5), new LineTo(x, y + 5));
        line.getStyleClass().add(styleClass);
        line.setTranslateY(5);
        return line;
    }

    private HBox getParentBox(ConceptNavigatorTreeItem treeItem) {
        Region spacer = new Region();
        int level = currentLevel - conceptNavigator.getTreeItemLevel(treeItem);
        spacer.setMinSize(128 - 8 * level, 1);
        spacer.setPrefSize(128 - 8 * level, 1);
        spacer.setMaxSize(128 - 8 * level, 1);
        IconRegion iconRegion = new IconRegion("icon");
        StackPane regionPane = new StackPane(iconRegion);
        regionPane.getStyleClass().add("region");
        Label label = getLineLabel(treeItem);
        HBox.setHgrow(label, Priority.ALWAYS);
        HBox box = new HBox(spacer, regionPane, label);
        boolean isRoot = conceptNavigator.getSecondaryParents(treeItem.getValue().nid(), -1).isEmpty();
        box.pseudoClassStateChanged(ROOT_LINEAGE_PSEUDO_CLASS, isRoot);
        if (isRoot) {
            iconRegion.getStyleClass().add("root-angle");
        } else if (level == 1) {
            iconRegion.getStyleClass().add("chev-line");
            box.pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, true);
        } else {
            iconRegion.getStyleClass().add("angle");
        }
        box.getStyleClass().add("lineage-hbox");
        return box;
    }

    private Label getLineLabel(ConceptNavigatorTreeItem treeItem) {
        Label label = new Label(treeItem.getValue().description());
        label.setOnMouseClicked(e -> {
            label.getParent().pseudoClassStateChanged(COLLAPSED_LINEAGE_PSEUDO_CLASS, false);
            conceptNavigator.getSecondaryParents(treeItem.getValue().nid(), -1).stream()
                        .filter(item -> getChildren().stream().noneMatch(n -> n.getUserData() != null && n.getUserData().equals(item.getValue().nid())))
                        .forEach(item -> {
                            HBox ancestorBox = getParentBox(item);
                            ancestorBox.setUserData(item.getValue().nid());
                            getChildren().addFirst(ancestorBox);
                            requestLayout();
                        });
        });
        label.getStyleClass().add("lineage-label");
        return label;
    }
}
