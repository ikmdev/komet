package dev.ikm.komet.kview.controls;

import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.kview.controls.skin.EditCoordinateOptionsPopupSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

public class EditCoordinateOptionsPopup extends PopupControl {

    public enum FILTER_TYPE {
//        NAVIGATOR,
//        SEARCH,
        CHAPTER_WINDOW,
        JOURNAL_VIEW,
        LANDING_PAGE
    }

    private final FILTER_TYPE filterType;
    private static final String DEFAULT_OPTIONS_KEY = "default-options";

    private final EditCoordinateOptionsUtils filterOptionsUtils;
//    private final ObservableViewNoOverride parentViewCoordinate;
    private final ViewProperties viewProperties;
    public EditCoordinateOptionsPopup(FILTER_TYPE filterType, ViewProperties viewProperties) {
        this.filterType = filterType;
        this.viewProperties = viewProperties;
        viewPropertiesReadOnlyObjectWrapperProperty = new ReadOnlyObjectWrapper<>(this, "viewProperties", viewProperties);
//        this.parentViewCoordinate = viewProperties.parentView();
        filterOptionsUtils = new EditCoordinateOptionsUtils();
        inheritedFilterOptionsProperty = new ReadOnlyObjectWrapper<>(this, "inheritedFilterOptions", new EditCoordinateOptions(viewProperties.parentView().editCoordinate()));

        // Set initial filter options to match the parent view's edit coordinate
        setFilterOptions(new EditCoordinateOptions(viewProperties.parentView().editCoordinate()));
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

    // readonly baseView
//    public final ObservableEditCoordinate getParentViewCoordinate() {
//        return parentViewCoordinate;
//    }
    // inheritedFilterOptionsProperty
    private final ReadOnlyObjectWrapper<EditCoordinateOptions> inheritedFilterOptionsProperty;
    public final ReadOnlyObjectProperty<EditCoordinateOptions> inheritedFilterOptionsProperty() {
       return inheritedFilterOptionsProperty.getReadOnlyProperty();
    }
    public final EditCoordinateOptions getInheritedFilterOptions() {
       return inheritedFilterOptionsProperty.get();
    }

    private final ReadOnlyObjectWrapper<ViewProperties> viewPropertiesReadOnlyObjectWrapperProperty;
    public final ReadOnlyObjectProperty<ViewProperties> viewPropertiesReadOnlyObjectWrapperProperty() {
       return viewPropertiesReadOnlyObjectWrapperProperty.getReadOnlyProperty();
    }
    public final ViewProperties getViewProperties() {
       return viewPropertiesReadOnlyObjectWrapperProperty.get();
    }

    // filterOptionsProperty
    private final ObjectProperty<EditCoordinateOptions> filterOptionsProperty = new SimpleObjectProperty<>(this, "filterOptions");
    public final ObjectProperty<EditCoordinateOptions> filterOptionsProperty() {
        return filterOptionsProperty;
    }
    public final EditCoordinateOptions getFilterOptions() {
        return filterOptionsProperty.get();
    }
    public final void setFilterOptions(EditCoordinateOptions value) {
        filterOptionsProperty.set(value);
    }

//    // navigatorProperty
//    private final ObjectProperty<Navigator> navigatorProperty = new SimpleObjectProperty<>(this, "navigator");
//    public final ObjectProperty<Navigator> navigatorProperty() {
//       return navigatorProperty;
//    }
//    public final Navigator getNavigator() {
//       return navigatorProperty.get();
//    }
//    public final void setNavigator(Navigator value) {
//        navigatorProperty.set(value);
//    }

    // containerProperty
    private final ObjectProperty<Node> containerProperty = new SimpleObjectProperty<>(this, "container");
    public final ObjectProperty<Node> containerProperty() {
       return containerProperty;
    }
    public final Node getContainer() {
       return containerProperty.get();
    }
    public final void setContainer(Node value) {
        containerProperty.set(value);
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

    public EditCoordinateOptionsUtils getFilterOptionsUtils() {
        return filterOptionsUtils;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EditCoordinateOptionsPopupSkin(this);
    }

}
