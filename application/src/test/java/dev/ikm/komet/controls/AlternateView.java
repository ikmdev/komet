package dev.ikm.komet.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class AlternateView extends VBox {

    private final StackPane closePane;
    private final KLConceptNavigatorControl conceptNavigator;

    public AlternateView(KLConceptNavigatorControl conceptNavigator) {
        this.conceptNavigator = conceptNavigator;
        IconRegion closeIconRegion = new IconRegion("icon", "close");
        closePane = new StackPane(closeIconRegion);
        closePane.getStyleClass().addAll("region", "close");
        closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (getConcept() != null) {
                getConcept().setExpanded(false);
            }
            e.consume();
        });
        closePane.setManaged(false);
        getChildren().add(closePane);

        getStyleClass().add("alternate-view");
        setManaged(false);
    }

    // conceptProperty
    private final ObjectProperty<ConceptNavigatorModel> conceptProperty = new SimpleObjectProperty<>(this, "concept") {
        @Override
        protected void invalidated() {
            ConceptNavigatorModel concept = get();
            getChildren().removeIf(Label.class::isInstance);
            if (concept != null) {
                TreeItem<ConceptNavigatorModel> treeViewItem = getTreeViewItem(conceptNavigator.getRoot(), concept);
                if (treeViewItem != null) {
                    TreeItem<ConceptNavigatorModel> parentItem = treeViewItem.getParent();
                    if (parentItem != null) {
                        ObservableList<TreeItem<ConceptNavigatorModel>> siblings = parentItem.getChildren();
                        Navigator navigator = conceptNavigator.getNavigator();
                        ImmutableList<Edge> allParents = navigator.getParentEdges(concept.getModel().nid());
                        List<ConceptNavigatorModel> secondaryParents = new ArrayList<>();

                        for (Edge parentLink : allParents) {
                            if (allParents.size() == 1 || parentLink.destinationNid() != parentItem.getValue().getModel().nid()) {
                                ConceptFacade facade = Entity.getFast(parentLink.destinationNid());
                                ConceptNavigatorModel conceptNavigatorModel = new ConceptNavigatorModel(facade);
                                conceptNavigatorModel.setDefined(navigator.getViewCalculator().hasSufficientSet(facade));
                                conceptNavigatorModel.setMultiParent(navigator.getParentNids(facade.nid()).length > 1);
                                secondaryParents.add(conceptNavigatorModel);
                            }
                        }

                        for (ConceptNavigatorModel extraParentItem : secondaryParents) {
                            concept.getExtraParents().add(extraParentItem);
                            StackPane chevStackPane = new StackPane(getLine(0, 0, "line"), new IconRegion("icon", "chevron"));
                            chevStackPane.getStyleClass().add("region");
                            Label label = new Label(extraParentItem.getModel().description(), chevStackPane);
                            label.pseudoClassStateChanged(PseudoClass.getPseudoClass("collapsed"), true);
                            label.getStyleClass().add("alternate-label");
                            getChildren().add(label);
                        }
                    }
                }
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

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = closePane.prefWidth(getHeight());
        double h = closePane.prefHeight(getWidth());
        closePane.resizeRelocate(getWidth() - w - getInsets().getRight(), getInsets().getTop(), w, h);
    }

    private static TreeItem<ConceptNavigatorModel> getTreeViewItem(TreeItem<ConceptNavigatorModel> item, ConceptNavigatorModel value) {
        if (item == null || item.getChildren() == null) {
            return null;
        }
        if (item.getValue().equals(value)) {
            return item;
        }
        for (TreeItem<ConceptNavigatorModel> child : item.getChildren()) {
            TreeItem<ConceptNavigatorModel> m = getTreeViewItem(child, value);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    private Path getLine(double x, double y, String styleClass) {
        Path line = new Path();
        line.getElements().addAll(new MoveTo(x, y - 5), new LineTo(x, y + 5));
        line.getStyleClass().add(styleClass);
        line.setTranslateY(5);
        return line;
    }
}
