package dev.ikm.komet.controls;

import javafx.scene.layout.Region;

class IconRegion extends Region {

    public IconRegion(String... styleClasses) {
        getStyleClass().addAll(styleClasses);
    }
}
