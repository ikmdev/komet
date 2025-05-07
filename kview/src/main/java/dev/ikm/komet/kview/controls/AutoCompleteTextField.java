package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.AutoCompleteTextFieldSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

public class AutoCompleteTextField<T> extends TextField {

    /*=*************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     ************************************************************************=*/

    /**
     * The constructor for AutoCompleteTextField.
     */
    public AutoCompleteTextField() {
        getStyleClass().setAll("auto-complete-text-field");
    }

    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // -- value
    /**
     * The suggestion the user has selected from the suggestions popup or null if he has not yet selected any.
     */
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();
    public T getValue() { return value.get(); }
    public ObjectProperty<T> valueProperty() { return value; }
    public void setValue(T value) { this.value.set(value); }

    // -- completer
    /**
     * The auto-complete function. It will receive the string the user has input and should return the list of
     * auto-complete suggestions. This function runs on a background thread.
     */
    private final ObjectProperty<Function<String, List<T>>> completer = new SimpleObjectProperty<>();
    public final void setCompleter(Function<String, List<T>> handler) { completer.set(handler); }
    public final Function<String, List<T>> getCompleter() { return completer.get(); }
    public final ObjectProperty<Function<String, List<T>>> completerProperty() { return completer; }

    // -- completer wait time
    /**
     * The time to wait after the user has changed the textfield text to call the completer.
     */
    private final ObjectProperty<Duration> completerWaitTime = new SimpleObjectProperty<>(Duration.millis(300));
    public Duration getCompleterWaitTime() { return completerWaitTime.get(); }
    public ObjectProperty<Duration> completerWaitTimeProperty() { return completerWaitTime; }
    public void setCompleterWaitTime(Duration duration) { completerWaitTime.set(duration); }

    // -- suggestions node factory
    /**
     * This will return a node to be shown in the auto-complete popup for each result returned
     * by the 'completer'. AutoCompleteTextField already supplies a default implementation
     * for this property: it returns a Label inside a simple container, it's text is the result of
     * calling toString on the object returned by the completer.
     */
    private final ObjectProperty<Function<T, Node>> suggestionsNodeFactory = new SimpleObjectProperty<>(this::createDefaultNodeForPopup);
    public final void setSuggestionsNodeFactory(Function<T, Node> factory) { suggestionsNodeFactory.set(factory); }
    public final Function<T, Node> getSuggestionsNodeFactory() { return suggestionsNodeFactory.get(); }
    public final ObjectProperty<Function<T, Node>> suggestionsNodeFactoryProperty() { return suggestionsNodeFactory; }

    // --- string converter
    /**
     * Converts the user-typed input to an object of type T, or the object of type T to a String.
     * @return the converter property
     */
    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter", AutoCompleteTextField.defaultStringConverter());
    public final ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    public final void setConverter(StringConverter<T> value) { converterProperty().set(value); }
    public final StringConverter<T> getConverter() {return converterProperty().get(); }

    // -- max number of suggestions
    private final IntegerProperty maxNumberOfSuggestions = new SimpleIntegerProperty(5);
    public int getMaxNumberOfSuggestions() { return maxNumberOfSuggestions.get(); }
    public IntegerProperty maxNumberOfSuggestionsProperty() { return maxNumberOfSuggestions; }
    public void setMaxNumberOfSuggestions(int value) { maxNumberOfSuggestions.set(value);}

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AutoCompleteTextFieldSkin<T>(this);
    }

    @Override
    public String getUserAgentStylesheet() { return KLInstantControl.class.getResource("auto-complete-text-field.css").toExternalForm(); }

    /***************************************************************************
     *                                                                         *
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/

    private Node createDefaultNodeForPopup(T value) {
        StackPane stackPane = new StackPane();
        String text = getConverter() != null ? getConverter().toString(value) : value.toString();
        Label label = new Label(text);
        stackPane.setAlignment(Pos.CENTER_LEFT);
        stackPane.getChildren().add(label);
        return stackPane;
    }

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<>() {
            @Override public String toString(T t) {
                return t == null ? null : t.toString();
            }
            @Override public T fromString(String string) {
                return (T) string;
            }
        };
    }
}