package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.DateFilterTitledPaneSkin;
import javafx.scene.control.Skin;

public class DateFilterTitledPane extends FilterTitledPane {

    public DateFilterTitledPane() {
        getStyleClass().add("date-filter-titled-pane");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateFilterTitledPaneSkin(this);
    }
}

