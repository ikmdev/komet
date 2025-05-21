package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterTitledPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

import java.util.ArrayList;
import java.util.List;

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

    // options
    private final List<String> options = new ArrayList<>();
    public final List<String> getOptions() {
        return options;
    }
    public final void setOptions(List<String> value) {
        options.clear();
        options.addAll(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterTitledPaneSkin(this);
    }
}
