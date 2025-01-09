package dev.ikm.komet.layout.container;

import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.component.KlComponentPane;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * KlContainer is a class that implements the KlWidget interface and is responsible for
 * managing a collection of KlComponents within a GridPane layout.
 */
public class KlContainer implements KlWidget {
    private GridPane gridPane;
    List<KlComponentPane> klComponents;

    /**
     * Returns the GridPane instance managed by this KlContainer.
     *
     * @return the GridPane instance
     */
    @Override
    public GridPane klWidget() {
        return gridPane;
    }
}
