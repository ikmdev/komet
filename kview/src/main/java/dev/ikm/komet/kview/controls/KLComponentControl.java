package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentControlSkin;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityFacade;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static dev.ikm.komet.terms.KometConcept.BLANK_CONCEPT;

/**
 * <p>KLComponentControl is a custom control that acts as a template capable of populating a single,
 * independent item, with relevant information.
 * Initially, if the control is empty, the user can either:
 * <p>- type in a {@link javafx.scene.control.TextField} to search for a concept The {@link #searchTextProperty} holds
 * the text to search for.</p>
 * <p>- or drag and drop a concept from any other external JavaFX node that holds a valid {@link Entity}.
 * The {@link #entityProperty} keeps the entity that was added.
 * <p>When an entity is added to the control, a close button is enabled to remove it, based on
 * the {@link #onRemoveActionProperty}.
 * <pre><code>KLComponentControl componentControl = new KLComponentControl();
 * componentControl.setTitle("Component definition");
 * componentControl.setOnSearchAction(e -> System.out.println("Search for " + componentControl.getSearchText()));
 * componentControl.entityProperty().subscribe(entity -> System.out.println("Entity = " + entity));
 * </code></pre>
 *
 * @see KLComponentCollectionControl
 */
public class KLComponentControl extends Control {

    private static final String SEARCH_TEXT_VALUE = "search.text.value";

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    /**
     * Creates a KLComponentControl
     */
    public KLComponentControl() {
        getStyleClass().add("component-control");
        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.wasAdded() && SEARCH_TEXT_VALUE.equals(change.getKey())) {
                if (change.getValueAdded() instanceof String value) {
                    searchTextProperty.set(value);
                }
                getProperties().remove(SEARCH_TEXT_VALUE);
            }
        });

        sceneProperty().subscribe(newScene -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getUserAgentStylesheet());
            }
        });
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns whether a component value means empty.
     *
     * @param entity the component, or {@code null}
     * @return true if the value is {@code null} or the blank concept
     */
    public static boolean isEmpty(EntityFacade entity) {
        return entity == null || EntityFacade.same(entity, BLANK_CONCEPT);
    }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentControlSkin(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLComponentControl.class.getResource("component-control.css").toExternalForm();
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- empty
    public boolean isEmpty() {
        return isEmpty(getEntity());
    }

    // -- title
    /**
     * A string property that sets the title of the control, if any
     */
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

    // -- entity
    /**
     * This property holds the {@link Entity} that has been added to the control
     */
    private final ObjectProperty<EntityFacade> entityProperty = new SimpleObjectProperty<>(this, "entity", null);
    public final ObjectProperty<EntityFacade> entityProperty() { return entityProperty; }

    /**
     * The component the control holds, in whatever facade form it was given: a proxy, a binding
     * enum or an entity. Compare it with {@link EntityFacade#same}, never with {@code ==}.
     *
     * @return the component, or {@code null}
     */
    public final EntityFacade getEntity() {
        return entityProperty.get();
    }

    /**
     *
     * @param value
     */
    public final void setEntity(EntityFacade value) {
        entityProperty.set(value);
    }

    // -- type ahead completer
    /**
     * The auto-complete function. It will receive the string the user has input and should return the list of
     * auto-complete suggestions. This function runs on a background thread.
     */
    private final ObjectProperty<Function<String, List<EntityFacade>>> completer = new SimpleObjectProperty<>();
    public final void setTypeAheadCompleter(Function<String, List<EntityFacade>> handler) { completer.set(handler); }
    public final Function<String, List<EntityFacade>> getCompleter() { return completer.get(); }
    public final ObjectProperty<Function<String, List<EntityFacade>>> completerProperty() { return completer; }

    // -- function to render the component's name and avoid entity.description()
    private final ObjectProperty<Function<EntityFacade, String>> componentNameRenderer = new SimpleObjectProperty<>();
    public final Function<EntityFacade, String> getComponentNameRenderer() { return componentNameRenderer.get(); }
    public final void setComponentNameRenderer(Function<EntityFacade, String> nameHandler) {
        componentNameRenderer.set(nameHandler);
    }
    public final ObjectProperty<Function<EntityFacade, String>> componentNameRendererProperty() {
        return componentNameRenderer;
    }

    // -- type ahead string converter
    /**
     * Converts the user-typed input to an object of type T, or the object of type T to a String.
     */
    private final ObjectProperty<StringConverter<EntityFacade>> typeAheadStringConverter = new SimpleObjectProperty<>(this, "converter");
    public final ObjectProperty<StringConverter<EntityFacade>> typeAheadStringConverterProperty() { return typeAheadStringConverter; }
    public final void setTypeAheadStringConverter(StringConverter<EntityFacade> value) { typeAheadStringConverterProperty().set(value); }
    public final StringConverter<EntityFacade> getTypeAheadStringConverter() {return typeAheadStringConverterProperty().get(); }

    // -- suggestions node factory
    /**
     * This will return a Cell to be shown in the auto-complete popup for each result returned
     * by the 'completer'.
     */
    private final ObjectProperty<Callback<ListView<EntityFacade>, ListCell<EntityFacade>>> suggestionsCellFactory = new SimpleObjectProperty<>();
    public final void setSuggestionsCellFactory(Callback<ListView<EntityFacade>, ListCell<EntityFacade>> factory) { suggestionsCellFactory.set(factory); }
    public final Callback<ListView<EntityFacade>, ListCell<EntityFacade>> getSuggestionsCellFactory() { return suggestionsCellFactory.get(); }
    public final ObjectProperty<Callback<ListView<EntityFacade>, ListCell<EntityFacade>>> suggestionsCellFactoryProperty() { return suggestionsCellFactory; }

    // -- search text
    /**
     * A read only property with the text that was typed to be searched for.
     */
    private final ReadOnlyStringWrapper searchTextProperty = new ReadOnlyStringWrapper(this, "searchText");
    public final ReadOnlyStringProperty searchTextProperty() {
       return searchTextProperty.getReadOnlyProperty();
    }
    public final String getSearchText() {
       return searchTextProperty.get();
    }

    // -- show add concept
    /**
     * A boolean property that when true adds an extra button to the control
     */
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

    // -- show drag handle
    /**
     * A boolean property that when true shows the drag handle
     */
    private final BooleanProperty showDragHandle = new SimpleBooleanProperty();
    public boolean isShowDragHandle() { return showDragHandle.get(); }
    public BooleanProperty showDragHandleProperty() { return showDragHandle; }
    public void setShowDragHandle(boolean value) { showDragHandle.set(value); }

    // -- component allowed filter
    /**
     * A Predicate used to filter Components with PublicIds that are allowed to be set in this control.
     * If the Predicate returns true then the Component with PublicId is allowed.
     * The filter that is set by default will allow any PublicId.
     */
    private final ObjectProperty<Predicate<PublicId>> componentAllowedFilter = new SimpleObjectProperty<>(_ -> true);
    public Predicate<PublicId> getComponentAllowedFilter() { return componentAllowedFilter.get(); }
    public ObjectProperty<Predicate<PublicId>> componentAllowedFilterProperty() { return componentAllowedFilter; }
    public void setComponentAllowedFilter(Predicate<PublicId> value) { componentAllowedFilter.set(value); }

    // -- on remove action
    /**
     * A property with an action to be executed when the user clicks on the remove button. By default, it will
     * remove the entity that was added.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onRemoveActionProperty = new SimpleObjectProperty<>(this, "onRemoveAction", e -> setEntity(BLANK_CONCEPT));
    public final ObjectProperty<EventHandler<ActionEvent>> onRemoveActionProperty() {
       return onRemoveActionProperty;
    }
    public final EventHandler<ActionEvent> getOnRemoveAction() {
       return onRemoveActionProperty.get();
    }
    public final void setOnRemoveAction(EventHandler<ActionEvent> value) {
        onRemoveActionProperty.set(value);
    }

    // -- typeahead header pane
    private final ObjectProperty<AutoCompleteTextField.HeaderPane> typeAheadHeaderPane = new SimpleObjectProperty<>();
    public AutoCompleteTextField.HeaderPane getTypeAheadHeaderPane() { return typeAheadHeaderPane.get(); }
    public ObjectProperty<AutoCompleteTextField.HeaderPane> typeAheadHeaderPaneProperty() { return typeAheadHeaderPane; }
    public void setTypeAheadHeaderPane(AutoCompleteTextField.HeaderPane typeAheadHeaderPane) { this.typeAheadHeaderPane.set(typeAheadHeaderPane); }

    // -- on dropping multiple concepts
    private final ObjectProperty<Consumer<List<List<UUID[]>>>> onDroppingMultipleConcepts = new SimpleObjectProperty<>();
    public final void setOnDroppingMultipleConcepts(Consumer<List<List<UUID[]>>> consumer) { this.onDroppingMultipleConcepts.set(consumer); }
    public final Consumer<List<List<UUID[]>>> getOnDroppingMultipleConcepts() { return onDroppingMultipleConcepts.get(); }
    public final ObjectProperty<Consumer<List<List<UUID[]>>>> onDroppingMultipleConceptsProperty() { return onDroppingMultipleConcepts; }
}
