package dev.ikm.komet.controls;

import dev.ikm.komet.navigator.graph.Navigator;
import dev.ikm.tinkar.coordinate.navigation.calculator.Edge;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.ConceptFacade;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class LineageBox extends VBox {

    private final StackPane closePane;
    private final KLConceptNavigatorControl conceptNavigator;

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
            ConceptNavigatorTreeItem treeViewItem = get();
            getChildren().removeIf(Label.class::isInstance);
            if (treeViewItem != null) {
                ConceptNavigatorTreeItem parentItem = (ConceptNavigatorTreeItem) treeViewItem.getParent();
                if (parentItem != null) {
                    Navigator navigator = conceptNavigator.getNavigator();
                    ImmutableList<Edge> allParents = navigator.getParentEdges(treeViewItem.getValue().nid());
                    List<ConceptNavigatorTreeItem> secondaryParents = new ArrayList<>();

                    for (Edge parentLink : allParents) {
                        if (allParents.size() == 1 || parentLink.destinationNid() != parentItem.getValue().nid()) {
                            ConceptFacade facade = Entity.getFast(parentLink.destinationNid());
                            ConceptNavigatorTreeItem conceptNavigatorTreeItem = new ConceptNavigatorTreeItem(facade);
                            conceptNavigatorTreeItem.setDefined(navigator.getViewCalculator().hasSufficientSet(facade));
                            conceptNavigatorTreeItem.setMultiParent(navigator.getParentNids(facade.nid()).length > 1);
                            secondaryParents.add(conceptNavigatorTreeItem);
                        }
                    }

                    for (ConceptNavigatorTreeItem extraParentItem : secondaryParents) {
                        treeViewItem.getExtraParents().add(extraParentItem);
                        StackPane chevStackPane = new StackPane(getLine(0, 0, "line"), new IconRegion("icon", "chevron"));
                        chevStackPane.getStyleClass().add("region");
                        Label label = new Label(extraParentItem.getValue().description(), chevStackPane);
                        label.pseudoClassStateChanged(PseudoClass.getPseudoClass("collapsed"), true);
                        label.getStyleClass().add("lineage-label");
                        getChildren().add(label);
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
}
