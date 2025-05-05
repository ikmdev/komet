package dev.ikm.komet.app;

import dev.ikm.komet.app.credential.CredentialItemWrapper;
import dev.ikm.tinkar.common.alert.AlertStreams;
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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The GetRemoteTask class is a task that prompts the user to enter a Git Remote URL.
 * It extends the Task<Boolean> class, indicating that it returns a Boolean value upon completion.
 *
 * This task adds the URL to the git config as the `origin` remote, so git commands will use the supplied remote by default.
 */
public class InitRemoteTask extends Task<Boolean>  {

    final Git git;
    final CredentialItem.StringType urlItem;
    final CredentialItem.StringType userName;
    final CredentialItem.StringType userEmail;

    /**
     * GetRemoteTask is a task that retrieves the Git Remote URI.
     */
    public InitRemoteTask(Git git) {
        this.git = git;
        this.urlItem = new CredentialItem.StringType("Git Remote URL: ", false);
        this.userName = new CredentialItem.StringType("Git User Name:", false);
        this.userEmail = new CredentialItem.StringType("Git User Email: ", false);
    }

    /**
     * Displays a dialog box for the user to enter the Git Remote URL.
     *
     * @return {@code true} if the OK button is pressed, {@code false} otherwise.
     */
    @Override
    public Boolean call() {
        AtomicBoolean okButtonPressed = new AtomicBoolean(false);

        ObservableList<PropertySheet.Item> properties = FXCollections.observableArrayList();
        URIish ignoredUri = new URIish();
        properties.add(new CredentialItemWrapper(ignoredUri, urlItem));
        properties.add(new CredentialItemWrapper(ignoredUri, userName));
        properties.add(new CredentialItemWrapper(ignoredUri, userEmail));

        PropertySheet propertySheet = new PropertySheet(properties);
        propertySheet.setSearchBoxVisible(false);
        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setMode(PropertySheet.Mode.NAME);

        Label label = new Label("Enter the Git Remote URL to use");

        VBox vBox = new VBox(label, propertySheet);

        //Create an Alert with OK and Cancel buttons
        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Initialize Git Remote");

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
                    try {
                        URIish uri = new URIish(urlItem.getValue().trim());
                        git.remoteAdd()
                                .setName("origin")
                                .setUri(uri)
                                .call();
                        git.getRepository().getConfig().setString("user", null, "name", userName.getValue().trim());
                        git.getRepository().getConfig().setString("user", null, "email", userEmail.getValue().trim());
                    } catch (URISyntaxException ex) {
                        AlertStreams.dispatchToRoot(ex);
                    } catch (GitAPIException e) {
                        throw new RuntimeException(e);
                    } finally {
                        okButtonPressed.set(true);
                    }
                }); // Do some action

        return okButtonPressed.get();
    }
}
