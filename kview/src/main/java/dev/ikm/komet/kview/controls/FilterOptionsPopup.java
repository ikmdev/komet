package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterOptionsPopupSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

public class FilterOptionsPopup extends PopupControl {

    public enum FILTER_TYPE {
        NAVIGATOR,
        SEARCH
    }

    private final FILTER_TYPE filterType;
    private static final String DEFAULT_OPTIONS_KEY = "default-options";

    public FilterOptionsPopup(FILTER_TYPE filterType) {
        this.filterType = filterType;
        setAutoHide(true);
        getStyleClass().add("filter-options-popup");
        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);

        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded() && DEFAULT_OPTIONS_KEY.equals(change.getKey())) {
                if (change.getValueAdded() instanceof Boolean value) {
                    defaultOptionsSetProperty.set(value);
                }
                getProperties().remove(DEFAULT_OPTIONS_KEY);
            }
        });
    }

    // initialFilterOptionsProperty
    private final ObjectProperty<FilterOptions> initialFilterOptionsProperty = new SimpleObjectProperty<>(this, "initialFilterOptions", new FilterOptions());
    public final ObjectProperty<FilterOptions> initialFilterOptionsProperty() {
        return initialFilterOptionsProperty;
    }
    public final FilterOptions getInitialFilterOptions() {
        return initialFilterOptionsProperty.get();
    }
    public final void setInitialFilterOptions(FilterOptions value) {
        initialFilterOptionsProperty.set(value);
    }

    // filterOptionsProperty
    private final ObjectProperty<FilterOptions> filterOptionsProperty = new SimpleObjectProperty<>(this, "filterOptions");
    public final ObjectProperty<FilterOptions> filterOptionsProperty() {
        return filterOptionsProperty;
    }
    public final FilterOptions getFilterOptions() {
        return filterOptionsProperty.get();
    }
    public final void setFilterOptions(FilterOptions value) {
        filterOptionsProperty.set(value);
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

    // defaultOptionsSetProperty
    private final ReadOnlyBooleanWrapper defaultOptionsSetProperty = new ReadOnlyBooleanWrapper(this, "defaultOptionsSet", true);
    public final ReadOnlyBooleanProperty defaultOptionsSetProperty() {
       return defaultOptionsSetProperty.getReadOnlyProperty();
    }
    public final boolean isDefaultOptionsSet() {
       return defaultOptionsSetProperty.get();
    }

    public final FILTER_TYPE getFilterType() {
        return filterType;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterOptionsPopupSkin(this);
    }

}
