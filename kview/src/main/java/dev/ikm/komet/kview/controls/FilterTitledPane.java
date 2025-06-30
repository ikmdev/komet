package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterTitledPaneSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

public class FilterTitledPane extends TitledPane {

    public FilterTitledPane() {

    }

    // titleProperty
    private final StringProperty titleProperty = new SimpleStringProperty(this, "title");
    public final StringProperty titleProperty() {
        return titleProperty;
    }
    public final String getTitle() {
        return titleProperty.get();
    }
    public final void setTitle(String value) {
        titleProperty.set(value);
    }

    // optionProperty
    private final ObjectProperty<FilterOptions.Option> optionProperty = new SimpleObjectProperty<>(this, "option");
    public final ObjectProperty<FilterOptions.Option> optionProperty() {
       return optionProperty;
    }
    public final FilterOptions.Option getOption() {
       return optionProperty.get();
    }
    public final void setOption(FilterOptions.Option value) {
        optionProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterTitledPaneSkin(this);
    }
}
