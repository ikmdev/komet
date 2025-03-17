package dev.ikm.komet.controls.skin;

import com.pixelduke.control.skin.ConsciousScrollPaneSkin;
import dev.ikm.komet.controls.ConceptNavigatorTreeItem;
import dev.ikm.komet.controls.IconRegion;
import dev.ikm.komet.controls.LineageBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class LineageBoxSkin extends ConsciousScrollPaneSkin {
    private final StackPane closePane;

    public LineageBoxSkin(LineageBox lineageBox) {
        super(lineageBox);

        IconRegion closeIconRegion = new IconRegion("icon", "close");
        closePane = new StackPane(closeIconRegion);
        closePane.getStyleClass().addAll("region", "close");
        closePane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            ConceptNavigatorTreeItem concept = lineageBox.getConcept();
            if (concept != null) {
                concept.setViewLineage(false);
                concept.getInvertedTree().reset();
                lineageBox.setConcept(null);
            }
            e.consume();
        });
        closePane.setManaged(false);
        getChildren().add(closePane);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        double w2 = closePane.prefWidth(w);
        double h2 = closePane.prefHeight(h);
        closePane.resizeRelocate(w - w2 - 4, 4, w2, h2);
    }
}
