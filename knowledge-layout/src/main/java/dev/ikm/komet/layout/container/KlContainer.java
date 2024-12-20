package dev.ikm.komet.layout.container;

import dev.ikm.komet.layout.KometPlugin;
import dev.ikm.komet.layout.component.KlComponentPaneSingle;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * KlContainer is a class that implements the KometPlugin interface and is responsible for
 * managing a collection of KlComponents within a GridPane layout.
 */
public class KlContainer implements KometPlugin {
    private GridPane gridPane;
    List<KlComponentPaneSingle> klComponents;

    /**
     * Returns the GridPane instance managed by this KlContainer.
     *
     * @return the GridPane instance
     */
    @Override
    public <SGN extends Node> SGN sceneGraphNode() {
        return (SGN) gridPane;
    }
}
