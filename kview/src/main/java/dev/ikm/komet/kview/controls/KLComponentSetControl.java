package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentSetControlSkin;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.LinkedList;
import java.util.List;

public class KLComponentSetControl extends Control {

    public KLComponentSetControl() {
        getStyleClass().add("component-set-control");
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

    // entitiesProperty
    private final ListProperty<Entity<?>> entitiesProperty = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
    public final ListProperty<Entity<?>> entitiesProperty() {
       return entitiesProperty;
    }
    public final List<Entity<?>> getEntitiesList() {
       return entitiesProperty.get();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentSetControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return KLComponentSetControl.class.getResource("component-set-control.css").toExternalForm();
    }
}
