package dev.ikm.komet.controls;

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
            getChildren().removeIf(Label.class::isInstance);
            ConceptNavigatorTreeItem treeItem = get();
            if (treeItem != null && treeItem.getValue() != null) {
                ConceptNavigatorTreeItem parentItem = (ConceptNavigatorTreeItem) treeItem.getParent();
                if (parentItem != null) {
                    List<ConceptNavigatorTreeItem> secondaryParents = conceptNavigator.
                            getSecondaryParents(treeItem.getValue().nid(), parentItem.getValue().nid());
                    treeItem.setExtraParents(secondaryParents);
                    for (ConceptNavigatorTreeItem extraParentItem : secondaryParents) {
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
