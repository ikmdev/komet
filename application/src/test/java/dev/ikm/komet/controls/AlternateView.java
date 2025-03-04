package dev.ikm.komet.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AlternateView extends VBox {

    private final StackPane closePane;

    public AlternateView() {
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
    private final ObjectProperty<ConceptNavigatorModel> conceptProperty = new SimpleObjectProperty<>(this, "concept");
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
}
