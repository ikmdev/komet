package dev.ikm.komet.layout.container;

import dev.ikm.komet.layout.KlWidget;
import dev.ikm.komet.layout.component.KlComponentPane;
import dev.ikm.komet.preferences.KometPreferences;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * KlContainer is a class that implements the KlWidget interface and is responsible for
 * managing a collection of KlComponents within a GridPane layout.
 */
public class KlContainer<T extends GridPane> implements KlWidget<T> {
    private GridPane gridPane;
    final List<KlComponentPane> klComponents = List.of();

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
