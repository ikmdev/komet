package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentControlSkin;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.terms.EntityProxy;
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
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>KLComponentControl is a custom control that acts as a template capable of populating a single,
 * independent item, with relevant information.
 * Initially, if the control is empty, the user can either:
 * </p>
 * <p>- type in a {@link javafx.scene.control.TextField} to search for a concept. Based on the
 * {@link #onSearchActionProperty}, an action will be executed. The {@link #searchTextProperty} holds the text
 * to search for.</p>
 * <p>- or drag and drop a concept from any other external JavaFX node that holds a valid {@link Entity}.
 * The {@link #entityProperty} keeps the entity that was added.
 *</p>
 * <p>When an entity is added to the control, a close button is enabled to remove it, based on
 * the {@link #onRemoveActionProperty}.
 * </p>
 * <pre><code>KLComponentControl componentControl = new KLComponentControl();
 * componentControl.setTitle("Component definition");
 * componentControl.setOnSearchAction(e -> System.out.println("Search for " + componentControl.getSearchText()));
 * componentControl.entityProperty().subscribe(entity -> System.out.println("Entity = " + entity));
 * </code></pre>
 *
 * @see KLComponentSetControl
 * @see KLComponentListControl
 */
public class KLComponentControl extends Control {
    public static int EMPTY_NID = Integer.MIN_VALUE;

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
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- empty
    public boolean isEmpty() {
        return getEntity() == null || getEntity().nid() == KLComponentControl.EMPTY_NID;
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
    private final ObjectProperty<EntityProxy> entityProperty = new SimpleObjectProperty<>(this, "entity", EntityProxy.make(EMPTY_NID));
    public final ObjectProperty<EntityProxy> entityProperty() { return entityProperty; }
    public final EntityProxy getEntity() { return entityProperty.get(); }
    public final void setEntity(EntityProxy value) { entityProperty.set(value); }

    // -- type ahead completer
    /**
     * The auto-complete function. It will receive the string the user has input and should return the list of
     * auto-complete suggestions. This function runs on a background thread.
     */
    private final ObjectProperty<Function<String, List<EntityProxy>>> completer = new SimpleObjectProperty<>();
    public final void setTypeAheadCompleter(Function<String, List<EntityProxy>> handler) { completer.set(handler); }
    public final Function<String, List<EntityProxy>> getCompleter() { return completer.get(); }
    public final ObjectProperty<Function<String, List<EntityProxy>>> completerProperty() { return completer; }

    // -- type ahead string converter
    /**
     * Converts the user-typed input to an object of type T, or the object of type T to a String.
     * @return the converter property
     */
    private final ObjectProperty<StringConverter<EntityProxy>> typeAheadStringConverter = new SimpleObjectProperty<>(this, "converter");
    public final ObjectProperty<StringConverter<EntityProxy>> typeAheadStringConverterProperty() { return typeAheadStringConverter; }
    public final void setTypeAheadStringConverter(StringConverter<EntityProxy> value) { typeAheadStringConverterProperty().set(value); }
    public final StringConverter<EntityProxy> getTypeAheadStringConverter() {return typeAheadStringConverterProperty().get(); }

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


//    /**
//     * A property with an action to be executed when the user types some text to be searched for.
//     */
//    private final ObjectProperty<EventHandler<ActionEvent>> onSearchActionProperty = new SimpleObjectProperty<>(this, "onSearchAction");
//    public final ObjectProperty<EventHandler<ActionEvent>> onSearchActionProperty() {
//       return onSearchActionProperty;
//    }
//    public final EventHandler<ActionEvent> getOnSearchAction() {
//       return onSearchActionProperty.get();
//    }
//    public final void setOnSearchAction(EventHandler<ActionEvent> value) {
//        onSearchActionProperty.set(value);
//    }

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


//    /**
//     * A property that defines the action to be executed when the add concept button is pressed.
//     */
//    private final ObjectProperty<EventHandler<ActionEvent>> onAddConceptActionProperty = new SimpleObjectProperty<>(this, "onAddConceptAction");
//    public final ObjectProperty<EventHandler<ActionEvent>> onAddConceptActionProperty() {
//       return onAddConceptActionProperty;
//    }
//    public final EventHandler<ActionEvent> getOnAddConceptAction() {
//       return onAddConceptActionProperty.get();
//    }
//    public final void setOnAddConceptAction(EventHandler<ActionEvent> value) {
//        onAddConceptActionProperty.set(value);
//    }

    // -- on remove action
    /**
     * A property with an action to be executed when the user clicks on the remove button. By default, it will
     * remove the entity that was added.
     */
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

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

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
}
