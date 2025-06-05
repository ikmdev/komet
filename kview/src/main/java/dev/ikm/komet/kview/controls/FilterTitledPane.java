package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterTitledPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final StringProperty optionProperty = new SimpleStringProperty(this, "option");
    public final StringProperty optionProperty() {
        return optionProperty;
    }
    public final String getOption() {
        return optionProperty.get();
    }
    public final void setOption(String value) {
        optionProperty.set(value);
    }

    // defaultOptionProperty
    private final StringProperty defaultOptionProperty = new SimpleStringProperty(this, "defaultOption");
    public final StringProperty defaultOptionProperty() {
        return defaultOptionProperty;
    }
    public final String getDefaultOption() {
        return defaultOptionProperty.get();
    }
    public final void setDefaultOption(String value) {
        defaultOptionProperty.set(value);
    }

    // multiSelectProperty
    private final BooleanProperty multiSelectProperty = new SimpleBooleanProperty(this, "multiSelect");
    public final BooleanProperty multiSelectProperty() {
        return multiSelectProperty;
    }
    public final boolean isMultiSelect() {
        return multiSelectProperty.get();
    }
    public final void setMultiSelect(boolean value) {
        multiSelectProperty.set(value);
    }

    // excludingProperty
    private final BooleanProperty excludingProperty = new SimpleBooleanProperty(this, "excluding");
    public final BooleanProperty excludingProperty() {
        return excludingProperty;
    }
    public final boolean isExcluding() {
        return excludingProperty.get();
    }
    public final void setExcluding(boolean value) {
        excludingProperty.set(value);
    }

    // availableOptionsProperty
    private final ObservableList<String> availableOptionsProperty = FXCollections.observableArrayList();
    public final ObservableList<String> getAvailableOptions() {
        return availableOptionsProperty;
    }

    // selectedOptionsProperty
    private final ObservableList<String> selectedOptionsProperty = FXCollections.observableArrayList();
    public final ObservableList<String> getSelectedOptions() {
        return selectedOptionsProperty;
    }

    // excludedOptionsProperty
    private final ObservableList<String> excludedOptionsProperty = FXCollections.observableArrayList();
    public final ObservableList<String> getExcludedOptions() {
        return excludedOptionsProperty;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterTitledPaneSkin(this);
    }
}
