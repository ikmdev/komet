package dev.ikm.komet.sync.credential;

import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.controlsfx.control.PropertySheet;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The GetCredentialsTask class is a task that prompts the user to enter credentials for a given URL.
 * It extends the Task<Boolean> class, indicating that it returns a Boolean value upon completion.
 *
 * This task stores the credentials for a URL in the user preferences (using encryption), so that the next execution
 * will check the stored credentials and populate the dialog with the stored values.
 */
public class GetCredentialsTask extends Task<Boolean>  {

    final CredentialItem[] credentialItems;
    final URIish uri;
    final String url;

    /**
     * GetCredentialsTask is a task that retrieves credentials for a given URI.
     */
    public GetCredentialsTask(URIish uri, CredentialItem[] credentialItems) {
        this.uri = uri;
        this.credentialItems = credentialItems;
        this.url = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
    }

    /**
     * Retrieves credentials for a given URL and displays a dialog box for the user to enter them.
     *
     * @return {@code true} if the OK button is pressed, {@code false} otherwise.
     */
    @Override
    public Boolean call() {
        AtomicBoolean okButtonPressed = new AtomicBoolean(false);
        KometPreferences userPreferences = Preferences.get().getUserPreferences();
        Optional<char[]> optionalPassword = getPasswordForUrl(url, userPreferences);
        Optional<String> optionalUser = getUserForUrl(url, userPreferences);

        ObservableList<PropertySheet.Item> properties = FXCollections.observableArrayList();
        for (CredentialItem item : credentialItems) {
            switch (item) {
                case CredentialItem.Username username ->
                        optionalUser.ifPresentOrElse(username::setValue, () -> username.setValue(uri.getUser()));
                case CredentialItem.Password password ->
                        optionalPassword.ifPresentOrElse(password::setValue, () -> password.setValue(new char[0]));
                default -> throw new IllegalStateException("Unexpected value: " + item);
            }
            properties.add(new CredentialItemWrapper(uri, item));
        }
        if (optionalUser.isPresent() && optionalPassword.isPresent()) {
            return true;
        }

        PropertySheet propertySheet = new PropertySheet(properties);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setMode(PropertySheet.Mode.NAME);

        Label label = new Label("For: " + url);

        VBox vBox = new VBox(label, propertySheet);

        //Create an Alert with OK and Cancel buttons
        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Enter credentials");

        // Set the custom pane as the content of the DialogPane
        alert.getDialogPane().setContent(vBox);

        // Fetch the Window from dialog pane
        Window window = alert.getDialogPane().getScene().getWindow();
        // Cast it to a Stage to get access to stage scenes
        Stage stage = (Stage) window;
        // Set the Stage style
        stage.initStyle(StageStyle.UTILITY);

        // Show and wait for user action
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK) // If OK button is pressed
                .ifPresent(response -> {
                    for (CredentialItem item : credentialItems) {
                        switch (item) {
                            case CredentialItem.Username username ->
                                    setUserForUrl(url, username.getValue(), userPreferences);
                            case CredentialItem.Password password ->
                                    setPasswordForUrl(url, password.getValue(), userPreferences);
                            default -> throw new IllegalStateException("Unexpected value: " + item);
                        }
                    }
                    okButtonPressed.set(true);
                }); // Do some action


        return okButtonPressed.get();
    }

    // TODO Add to preferences as a standard call...
    Optional<char[]> getPasswordForUrl(String url, KometPreferences preferences) {
        return preferences.getPassword(url);
    }

    // TODO Add to preferences as a standard call...
    void setPasswordForUrl(String url, char[] password, KometPreferences preferences) {
        preferences.putPassword(url, password);
    }

    // TODO Add to preferences as a standard call...
    Optional<String> getUserForUrl(String url, KometPreferences preferences) {
        return preferences.get(url + "-USER");
    }

    // TODO Add to preferences as a standard call...
    void setUserForUrl(String url, String user, KometPreferences preferences) {
        preferences.put(url + "-USER", user);
    }
}
