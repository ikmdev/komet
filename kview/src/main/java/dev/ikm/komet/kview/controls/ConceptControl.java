package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.ConceptControlSkin;
import dev.ikm.tinkar.entity.Entity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class ConceptControl extends Control {

    private static final String SEARCH_TEXT_VALUE = "search.text.value";

    public ConceptControl() {
        getStyleClass().add("concept-control");
        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded() && SEARCH_TEXT_VALUE.equals(change.getKey())) {
                if (change.getValueAdded() instanceof String value) {
                    searchTextProperty.set(value);
                }
                getProperties().remove(SEARCH_TEXT_VALUE);
            }
        });
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

    // entityProperty
    private final ObjectProperty<Entity<?>> entityProperty = new SimpleObjectProperty<>(this, "entity");
    public final ObjectProperty<Entity<?>> entityProperty() {
       return entityProperty;
    }
    public final Entity<?> getEntity() {
       return entityProperty.get();
    }
    public final void setEntity(Entity<?> value) {
        entityProperty.set(value);
    }

    // searchTextProperty
    private final ReadOnlyStringWrapper searchTextProperty = new ReadOnlyStringWrapper(this, "searchText");
    public final ReadOnlyStringProperty searchTextProperty() {
       return searchTextProperty.getReadOnlyProperty();
    }
    public final String getSearchText() {
       return searchTextProperty.get();
    }

    // onSearchActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onSearchActionProperty = new SimpleObjectProperty<>(this, "onSearchAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onSearchActionProperty() {
       return onSearchActionProperty;
    }
    public final EventHandler<ActionEvent> getOnSearchAction() {
       return onSearchActionProperty.get();
    }
    public final void setOnSearchAction(EventHandler<ActionEvent> value) {
        onSearchActionProperty.set(value);
    }

    // showAddConceptProperty
    private final BooleanProperty showAddConceptProperty = new SimpleBooleanProperty(this, "showAddConcept");
    public final BooleanProperty showAddConceptProperty() {
       return showAddConceptProperty;
    }
    public final boolean isShowAddConcept() {
       return showAddConceptProperty.get();
    }
    public final void setShowAddConcept(boolean value) {
        showAddConceptProperty.set(value);
    }

    // onAddConceptActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onAddConceptActionProperty = new SimpleObjectProperty<>(this, "onAddConceptAction");
    public final ObjectProperty<EventHandler<ActionEvent>> onAddConceptActionProperty() {
       return onAddConceptActionProperty;
    }
    public final EventHandler<ActionEvent> getOnAddConceptAction() {
       return onAddConceptActionProperty.get();
    }
    public final void setOnAddConceptAction(EventHandler<ActionEvent> value) {
        onAddConceptActionProperty.set(value);
    }

    // onRemoveActionProperty
    private final ObjectProperty<EventHandler<ActionEvent>> onRemoveActionProperty = new SimpleObjectProperty<>(this, "onRemoveAction", e -> setEntity(null));
    public final ObjectProperty<EventHandler<ActionEvent>> onRemoveActionProperty() {
       return onRemoveActionProperty;
    }
    public final EventHandler<ActionEvent> getOnRemoveAction() {
       return onRemoveActionProperty.get();
    }
    public final void setOnRemoveAction(EventHandler<ActionEvent> value) {
        onRemoveActionProperty.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ConceptControlSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return ConceptControl.class.getResource("concept-control.css").toExternalForm();
    }
}
