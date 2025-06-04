package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.KLComponentListControlSkin;
import dev.ikm.tinkar.common.id.IntIdCollection;
import dev.ikm.tinkar.terms.EntityProxy;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

/**
 * <p>KLComponentListControl is a custom control that acts as a template capable of populating multiple,
 * independent items, with relevant information, as part of a collection. It is made of one or more
 * {@link KLComponentControl KLComponentControls}, where the user can add multiple entities, including
 * duplicates.
 * <p>When two or more distinct entities are present, these can be reordered with drag and drop
 * gestures.</p>
 * <p>If there are no empty KLComponentControls, a {@link javafx.scene.control.Button} allows
 * adding one empty more, so the user can keep adding more items.
 * </p>
 *
 * <pre><code>
 * KLComponentListControl componentListControl = new KLComponentListControl();
 * componentListControl.setTitle("Component List Definition");
 * componentListControl.entitiesProperty().subscribe(entityList -> System.out.println("EntityList = " + entityList));
 * </code></pre>
 *
 * @see KLComponentControl
 * @see KLComponentSetControl
 */
public class KLComponentListControl<T extends IntIdCollection> extends Control {

    /**
     * Creates a KLComponentListControl
     */
    public KLComponentListControl() {
        getStyleClass().add("component-list-control");
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

    // -- value
    /**
     * This property holds a generic type T that extends from {@link IntIdCollection}.
     */
    private final ObjectProperty<T> valueProperty = new SimpleObjectProperty<>();
    public final ObjectProperty<T> valueProperty() { return valueProperty; }
    public final T getValue() { return valueProperty.get(); }
    public void setValue(T intIdList) { valueProperty.set(intIdList); }

    // -- type ahead completer
    /**
     * The auto-complete function. It will receive the string the user has input and should return the list of
     * auto-complete suggestions. This function runs on a background thread.
     */
    private final ObjectProperty<Function<String, List<EntityProxy>>> typeAheadCompleter = new SimpleObjectProperty<>();
    public final void setTypeAheadCompleter(Function<String, List<EntityProxy>> handler) { typeAheadCompleter.set(handler); }
    public final Function<String, List<EntityProxy>> getTypeAheadCompleter() { return typeAheadCompleter.get(); }
    public final ObjectProperty<Function<String, List<EntityProxy>>> typeAheadCompleterProperty() { return typeAheadCompleter; }

    // -- type ahead string converter
    /**
     * Converts the user-typed input to an object of type T, or the object of type T to a String.
     * @return the converter property
     */
    private final ObjectProperty<StringConverter<EntityProxy>> typeAheadStringConverter = new SimpleObjectProperty<>(this, "converter");
    public final ObjectProperty<StringConverter<EntityProxy>> typeAheadStringConverterProperty() { return typeAheadStringConverter; }
    public final void setTypeAheadStringConverter(StringConverter<EntityProxy> value) { typeAheadStringConverterProperty().set(value); }
    public final StringConverter<EntityProxy> getTypeAheadStringConverter() {return typeAheadStringConverterProperty().get(); }

    // -- suggestions node factory
    /**
     * This will return a Cell to be shown in the auto-complete popup for each result returned
     * by the 'completer'.
     */
    private final ObjectProperty<Callback<ListView<EntityProxy>, ListCell<EntityProxy>>> suggestionsCellFactory = new SimpleObjectProperty<>();
    public final void setSuggestionsCellFactory(Callback<ListView<EntityProxy>, ListCell<EntityProxy>> factory) { suggestionsCellFactory.set(factory); }
    public final Callback<ListView<EntityProxy>, ListCell<EntityProxy>> getSuggestionsCellFactory() { return suggestionsCellFactory.get(); }
    public final ObjectProperty<Callback<ListView<EntityProxy>, ListCell<EntityProxy>>> suggestionsCellFactoryProperty() { return suggestionsCellFactory; }

    // -- function to render the component's name and avoid entity.description()
    private final ObjectProperty<Function<EntityProxy, String>> componentNameRenderer = new SimpleObjectProperty<>();
    public final void setComponentNameRenderer(Function<EntityProxy, String> nameHandler) { componentNameRenderer.set(nameHandler); }
    public final Function<EntityProxy, String> getComponentNameRenderer() { return componentNameRenderer.get(); }
    public final ObjectProperty<Function<EntityProxy, String>> componentNameRendererProperty() { return componentNameRenderer; }

    // -- typeahead header pane
    private final ObjectProperty<AutoCompleteTextField.HeaderPane> typeAheadHeaderPane = new SimpleObjectProperty<>();
    public AutoCompleteTextField.HeaderPane getTypeAheadHeaderPane() { return typeAheadHeaderPane.get(); }
    public ObjectProperty<AutoCompleteTextField.HeaderPane> typeAheadHeaderPaneProperty() { return typeAheadHeaderPane; }
    public void setTypeAheadHeaderPane(AutoCompleteTextField.HeaderPane typeAheadHeaderPane) { this.typeAheadHeaderPane.set(typeAheadHeaderPane); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new KLComponentListControlSkin<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return KLComponentListControl.class.getResource("component-list-control.css").toExternalForm();
    }
}
