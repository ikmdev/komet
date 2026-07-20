package dev.ikm.komet.layout.controls;

import dev.ikm.komet.layout.controls.skin.DateFilterTitledPaneSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class DateFilterTitledPane extends FilterTitledPane {

    public enum MODE {
        LATEST,
        SINGLE_DATE,
        PRE_INCEPTION,
        DATE_RANGE_LIST
    }

    public DateFilterTitledPane() {
        getStyleClass().add("date-filter-titled-pane");
    }

    // modeProperty
    private final ObjectProperty<MODE> modeProperty = new SimpleObjectProperty<>(this, "mode");
    public final ObjectProperty<MODE> modeProperty() {
       return modeProperty;
    }
    public final MODE getMode() {
       return modeProperty.get();
    }
    public final void setMode(MODE value) {
        modeProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateFilterTitledPaneSkin(this);
    }
}

