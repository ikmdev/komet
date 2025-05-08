package dev.ikm.komet.sync.credential;

import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;

/**
 * The CredentialItemWrapper class is a wrapper class that implements the PropertySheet.Item interface.
 * It encapsulates a CredentialItem object and a URIish object.
 * This class provides methods to get and set the type, category, name, description, value, property editor class,
 * and observable value of the wrapped CredentialItem object.
 */
public class CredentialItemWrapper implements PropertySheet.Item {
    private final CredentialItem credentialItem;
    private final URIish urIish;

    /**
     * Wrapper class for the CredentialItem that provides the URIish associated with it.
     *
     * @param urIish The URIish associated with the credential item.
     * @param credentialItem The credential item to wrap.
     */
    public CredentialItemWrapper(URIish urIish, CredentialItem credentialItem) {
        this.credentialItem = credentialItem;
        this.urIish = urIish;
    }

    /**
     * Returns the type of the credential item.
     *
     * @return The type of the credential item.
     * @throws IllegalStateException If the credential item is of an unexpected type.
     */
    @Override
    public Class<?> getType() {
        return switch (credentialItem) {
            case CredentialItem.StringType stringType -> String.class;
            case CredentialItem.YesNoType yesNoType -> Boolean.class;
            case CredentialItem.CharArrayType charArrayType -> String.class;
            case CredentialItem.InformationalMessage informationalMessage -> String.class;
            default -> throw new IllegalStateException("Unexpected value: " + credentialItem);
        };
    }

    /**
     * Returns the category of the credential item.
     *
     * @return The category of the credential item.
     */
    @Override
    public String getCategory() {
        return urIish.toString();
    }

    /**
     * Returns the name of the credential item.
     *
     * @return The name of the credential item.
     */
    @Override
    public String getName() {
        return credentialItem.getPromptText();
    }

    /**
     * Returns the description of the credential item.
     *
     * @return The description of the credential item.
     */
    @Override
    public String getDescription() {
        return credentialItem.getPromptText();
    }

    /**
     * This method retrieves the value of the credential item. The value can be of different types depending on the category of the credential item.
     *
     * @return The value of the credential item.
     * @throws IllegalStateException If the credential item is of an unexpected type.
     */
    @Override
    public Object getValue() {
        return switch (credentialItem) {
            case CredentialItem.StringType stringType -> stringType.getValue();
            case CredentialItem.YesNoType yesNoType -> yesNoType.getValue();
            case CredentialItem.CharArrayType charArrayType when charArrayType.getValue() != null -> String.copyValueOf(charArrayType.getValue());
            case CredentialItem.CharArrayType charArrayType when charArrayType.getValue() == null-> null;
            case CredentialItem.InformationalMessage informationalMessage -> informationalMessage.getPromptText();
            default -> throw new IllegalStateException("Unexpected value: " + credentialItem);
        };
     }

    /**
     * Sets the value of the credential item based on its category.
     *
     * @param value The value to set for the credential item.
     * @throws UnsupportedOperationException If the credential item is in the category of an informational message.
     * @throws IllegalStateException If the credential item is of an unexpected type.
     */
    @Override
    public void setValue(final Object value) {
        switch (credentialItem) {
            case CredentialItem.YesNoType yesNoType when value == null -> yesNoType.setValue(false);
            case CredentialItem.YesNoType yesNoType when value != null -> yesNoType.setValue((Boolean) value);
            case CredentialItem.StringType stringType when value == null -> stringType.setValue("");
            case CredentialItem.StringType stringType when value != null -> stringType.setValue(value.toString());
            case CredentialItem.CharArrayType charArrayType when value == null -> charArrayType.setValue(new char[0]);
            case CredentialItem.CharArrayType charArrayType when value != null -> charArrayType.setValue(value.toString().toCharArray());
            case CredentialItem.InformationalMessage informationalMessage -> throw new UnsupportedOperationException("Can't set value for informational message");
            default -> throw new IllegalStateException("Unexpected value: " + credentialItem + " " + value);
        };
    }

    /**
     * Returns the optional class of the property editor for the credential item.
     * If the credential item is of type CharArrayType, the method returns an Optional containing the class PasswordEditor.
     * Otherwise, it returns an empty Optional.
     *
     * @return The optional class of the property editor for the credential item.
     */
    @Override
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
        if (this.credentialItem instanceof CredentialItem.CharArrayType) {
            return Optional.of(PasswordEditor.class);
        }
        return Optional.empty();
    }

    /**
     * Returns an empty Optional of ObservableValue.
     * This method is used to retrieve an ObservableValue for the credential item.
     *
     * @return The Optional of ObservableValue, which is always empty.
     */
    @Override
    public Optional<ObservableValue<? extends Object>> getObservableValue() {
        return Optional.empty();
    }
}
