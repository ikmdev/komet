package dev.ikm.komet.layout;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public non-sealed interface KlParent<FX extends Region> extends KlArea<FX> {
    GridPane gridPaneForChildren();
}
