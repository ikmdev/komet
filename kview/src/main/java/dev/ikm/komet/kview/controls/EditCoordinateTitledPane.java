package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.EditCoordinateTitledPaneSkin;
import dev.ikm.komet.navigator.graph.Navigator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;

public class EditCoordinateTitledPane extends TitledPane {

    public EditCoordinateTitledPane() {
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
    private final ObjectProperty<EditCoordinateOptions.Option> defaultOptionProperty = new SimpleObjectProperty<>(this, "defaultOption");
    public final ObjectProperty<EditCoordinateOptions.Option> defaultOptionProperty() {
       return defaultOptionProperty;
    }
    public final EditCoordinateOptions.Option getDefaultOption() {
       return defaultOptionProperty.get();
    }
    public final void setDefaultOption(EditCoordinateOptions.Option value) {
        defaultOptionProperty.set(value);
    }

    // optionProperty
    private final ObjectProperty<EditCoordinateOptions.Option> optionProperty = new SimpleObjectProperty<>(this, "option");
    public final ObjectProperty<EditCoordinateOptions.Option> optionProperty() {
       return optionProperty;
    }
    public final EditCoordinateOptions.Option getOption() {
       return optionProperty.get();
    }
    public final void setOption(EditCoordinateOptions.Option value) {
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
        return new EditCoordinateTitledPaneSkin(this);
    }
}
