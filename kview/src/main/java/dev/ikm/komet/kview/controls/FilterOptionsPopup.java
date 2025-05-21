package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterOptionsPopupSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

public class FilterOptionsPopup extends PopupControl {

    public FilterOptionsPopup() {
        setAutoHide(false);
        getStyleClass().add("filter-options-popup");
        setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
    }

    // filterOptionsProperty
    private final ObjectProperty<FilterOptions> filterOptionsProperty = new SimpleObjectProperty<>(this, "filterOptions", new FilterOptions());
    public final ObjectProperty<FilterOptions> filterOptionsProperty() {
       return filterOptionsProperty;
    }
    public final FilterOptions getFilterOptions() {
       return filterOptionsProperty.get();
    }
    public final void setFilterOptions(FilterOptions value) {
        filterOptionsProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterOptionsPopupSkin(this);
    }

}
