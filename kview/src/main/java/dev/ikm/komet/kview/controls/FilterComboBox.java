package dev.ikm.komet.kview.controls;

import dev.ikm.komet.kview.controls.skin.FilterComboBoxSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

import java.util.Objects;

/**
 * A general purpose combo box in which the user can type to search the options.
 *
 * <p>It looks and behaves like a non-editable {@link ComboBox}, plus: typing while it has focus
 * opens the popup narrowed to the options whose {@link #converterProperty() text} contains the
 * typed search text (case-insensitively), which is shown in place of the value meanwhile. The
 * current value, when among the matches, or else the first match is highlighted; Enter picks the
 * highlighted option (the arrow keys move the highlight), Backspace edits the search text and
 * Escape abandons the search.
 *
 * <p>Narrowing the options never changes the {@link #valueProperty() value}: only the user picking
 * an option does. (A plain {@code ComboBox} whose items are filtered shifts or clears its value as
 * the selected item comes and goes, which is why this is a control of its own rather than a
 * filtered {@code ComboBox}.)
 *
 * @param <T> the type of the options
 */
public class FilterComboBox<T> extends Control {

    public FilterComboBox() {
        getStyleClass().add("filter-combo-box");
    }

    // -- items
    private final ObservableList<T> items = FXCollections.observableArrayList();
    /** The options the user may choose from */
    public final ObservableList<T> getItems() { return items; }

    // -- value
    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value");
    /** The chosen option; it need not be one of the {@link #getItems() items} */
    public final ObjectProperty<T> valueProperty() { return value; }
    public final T getValue() { return value.get(); }
    public final void setValue(T value) { this.value.set(value); }

    // -- converter
    private final ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<>(this, "converter", new StringConverter<>() {
                @Override
                public String toString(T object) {
                    return Objects.toString(object, "");
                }

                @Override
                public T fromString(String string) {
                    return null;
                }
            });
    /** Turns an option into the text that shows it and that the search text is matched against */
    public final ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    public final StringConverter<T> getConverter() { return converter.get(); }
    public final void setConverter(StringConverter<T> value) { converter.set(value); }

    // -- prompt text
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText");
    /** The text shown while the value is {@code null} */
    public final StringProperty promptTextProperty() { return promptText; }
    public final String getPromptText() { return promptText.get(); }
    public final void setPromptText(String value) { promptText.set(value); }

    // -- placeholder
    private final ObjectProperty<Node> placeholder =
            new SimpleObjectProperty<>(this, "placeholder", new Label("No matching options"));
    /** Shown in the popup when no option matches the search text */
    public final ObjectProperty<Node> placeholderProperty() { return placeholder; }
    public final Node getPlaceholder() { return placeholder.get(); }
    public final void setPlaceholder(Node value) { placeholder.set(value); }

    /** {@inheritDoc} */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new FilterComboBoxSkin<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return FilterComboBox.class.getResource("filter-combo-box.css").toExternalForm();
    }
}
