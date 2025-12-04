package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterTitledPaneSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

public class FilterTitledPane extends TitledPane {

    public FilterTitledPane() {
        getStyleClass().add("filter-titled-pane");
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

    // defaultOptionProperty
    private final ObjectProperty<FilterOptions.Option> defaultOptionProperty = new SimpleObjectProperty<>(this, "defaultOption");
    public final ObjectProperty<FilterOptions.Option> defaultOptionProperty() {
       return defaultOptionProperty;
    }
    public final FilterOptions.Option getDefaultOption() {
       return defaultOptionProperty.get();
    }
    public final void setDefaultOption(FilterOptions.Option value) {
        defaultOptionProperty.set(value);
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

    // navigatorProperty
    private final ObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>(this, "navigator");
    public final ObjectProperty<Navigator> navigatorProperty() {
        return navigatorProperty;
    }
    public final Navigator getNavigator() {
        return navigatorProperty.get();
    }
    public final void setNavigator(Navigator value) {
        navigatorProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterTitledPaneSkin(this);
    }
}
