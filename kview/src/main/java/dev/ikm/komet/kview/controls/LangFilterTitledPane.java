package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.LangFilterTitledPaneSkin;
import dev.ikm.tinkar.terms.EntityFacade;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class LangFilterTitledPane extends FilterTitledPane {

    public LangFilterTitledPane() {
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
    private final ObjectProperty<FilterOptions.LanguageFilterCoordinates> defaultLangCoordinatesProperty = new SimpleObjectProperty<>(this, "defaultLangCoordinates");
    public final ObjectProperty<FilterOptions.LanguageFilterCoordinates> defaultLangCoordinatesProperty() {
       return defaultLangCoordinatesProperty;
    }
    public final FilterOptions.LanguageFilterCoordinates getDefaultLangCoordinates() {
       return defaultLangCoordinatesProperty.get();
    }
    public final void setDefaultLangCoordinates(FilterOptions.LanguageFilterCoordinates value) {
        defaultLangCoordinatesProperty.set(value);
    }

    // langCoordinatesProperty
    private final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langCoordinatesProperty = new SimpleObjectProperty<>(this, "langCoordinates");
    public final ObjectProperty<FilterOptions.LanguageFilterCoordinates> langCoordinatesProperty() {
       return langCoordinatesProperty;
    }
    public final FilterOptions.LanguageFilterCoordinates getLangCoordinates() {
       return langCoordinatesProperty.get();
    }
    public final void setLangCoordinates(FilterOptions.LanguageFilterCoordinates value) {
        langCoordinatesProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LangFilterTitledPaneSkin(this);
    }
}
