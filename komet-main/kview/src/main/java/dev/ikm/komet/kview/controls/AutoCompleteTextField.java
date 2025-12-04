package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.AutoCompleteTextFieldSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.util.Callback;
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
     * This will return a Cell to be shown in the auto-complete popup for each result returned
     * by the 'completer'. AutoCompleteTextField already supplies a default implementation
     * for this property: it returns a simple Cell, the text shown in it is the result of
     * calling String Converter (if supplied) or toString on the object returned by the completer.
     */
    private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> suggestionsCellFactory = new SimpleObjectProperty<>(this::createDefaultCellForPopup);
    public final void setSuggestionsCellFactory(Callback<ListView<T>, ListCell<T>> factory) { suggestionsCellFactory.set(factory); }
    public final Callback<ListView<T>, ListCell<T>> getSuggestionsCellFactory() { return suggestionsCellFactory.get(); }
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> suggestionsCellFactoryProperty() { return suggestionsCellFactory; }

    // -- suggestions node size
    /**
     * The height of each suggestion Node in the auto complete suggestions popup.
     */
    private final DoubleProperty suggestionsNodeHeight = new SimpleDoubleProperty(25);
    public double getSuggestionsNodeHeight() { return suggestionsNodeHeight.get(); }
    public DoubleProperty suggestionsNodeHeightProperty() { return suggestionsNodeHeight; }
    public void setSuggestionsNodeHeight(double size) { suggestionsNodeHeight.set(size); }

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

    // -- popup style classes
    private final ObservableList<String> popupStyleClasses = FXCollections.observableArrayList();
    public ObservableList<String> getPopupStyleClasses() { return popupStyleClasses; }

    // -- popup header pane
    private final ObjectProperty<HeaderPane> popupHeaderPane = new SimpleObjectProperty<>();
    public HeaderPane getPopupHeaderPane() { return popupHeaderPane.get(); }
    public ObjectProperty<HeaderPane> popupHeaderPaneProperty() { return popupHeaderPane; }
    public void setPopupHeaderPane(HeaderPane popupHeaderPane) { this.popupHeaderPane.set(popupHeaderPane); }

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

    private ListCell<T> createDefaultCellForPopup(ListView<T> listView) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                setGraphic(null);
                if (item == null) {
                    setText(null);
                } else {
                    String text = getConverter() != null ? getConverter().toString(item) : item.toString();
                    setText(text);
                }
            }
        };
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

    /***************************************************************************
     *                                                                         *
     * Support Classes                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * A header that can be shown at the top of the suggestions popup
     * @param <T> the type of each suggestion
     */
    public interface HeaderPane<T> {
        /**
         * This method should create and return the Node that will go into the header.
         *
         * @return the Node that goes into the header
         */
        Node createContent();

        /**
         * Whenever the suggestion's list is changed this method gets called so that the header can update itself.
         *
         * @param items the current list of suggestions.
         */
        void updateContent(List<T> items);
    }
}