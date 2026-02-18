package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.LangEditCoordinateTitledPaneSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class LangEditCoordinateTitledPane extends FilterTitledPane {

    public LangEditCoordinateTitledPane() {
        getStyleClass().add("lang-filter-titled-pane");
    }

    // ordinalProperty
    private final IntegerProperty ordinalProperty = new SimpleIntegerProperty(this, "ordinal");
    public final IntegerProperty ordinalProperty() {
       return ordinalProperty;
    }
    public final int getOrdinal() {
       return ordinalProperty.get();
    }
    public final void setOrdinal(int value) {
        ordinalProperty.set(value);
    }

    // defaultLangCoordinatesProperty
    private final ObjectProperty<EditCoordinateOptions.LanguageFilterCoordinates> defaultLangCoordinatesProperty = new SimpleObjectProperty<>(this, "defaultLangCoordinates");
    public final ObjectProperty<EditCoordinateOptions.LanguageFilterCoordinates> defaultLangCoordinatesProperty() {
       return defaultLangCoordinatesProperty;
    }
    public final EditCoordinateOptions.LanguageFilterCoordinates getDefaultLangCoordinates() {
       return defaultLangCoordinatesProperty.get();
    }
    public final void setDefaultLangCoordinates(EditCoordinateOptions.LanguageFilterCoordinates value) {
        defaultLangCoordinatesProperty.set(value);
    }

    // langCoordinatesProperty
    private final ObjectProperty<EditCoordinateOptions.LanguageFilterCoordinates> langCoordinatesProperty = new SimpleObjectProperty<>(this, "langCoordinates");
    public final ObjectProperty<EditCoordinateOptions.LanguageFilterCoordinates> langCoordinatesProperty() {
       return langCoordinatesProperty;
    }
    public final EditCoordinateOptions.LanguageFilterCoordinates getLangCoordinates() {
       return langCoordinatesProperty.get();
    }
    public final void setLangCoordinates(EditCoordinateOptions.LanguageFilterCoordinates value) {
        langCoordinatesProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LangEditCoordinateTitledPaneSkin(this);
    }
}
