package dev.ikm.komet.app;

import dev.ikm.komet.app.credential.CredentialItemWrapper;
import dev.ikm.komet.framework.propsheet.SheetItem;
import dev.ikm.tinkar.common.alert.AlertStreams;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.controlsfx.control.PropertySheet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The InfoTask class is a task that prompts the user to enter a Git Remote URL.
 * It extends the Task<Boolean> class, indicating that it returns a Boolean value upon completion.
 * <p>
 * This task adds the URL to the git config as the `origin` remote, so git commands will use the supplied remote by default.
 */
public class InfoTask extends Task<Boolean>  {

    final Git git;

    /**
     * GetRemoteTask is a task that retrieves the Git Remote URI.
     */
    public InfoTask(Git git) {
        this.git = git;
    }

    /**
     * Displays a dialog box for the user to enter the Git Remote URL.
     *
     * @return {@code true} if the OK button is pressed, {@code false} otherwise.
     */
    @Override
    public Boolean call() {
        AtomicBoolean okButtonPressed = new AtomicBoolean(false);

        StringBuilder repoNameText = new StringBuilder("Error: could not retrieve repo name.");
        String diffText = "Error: could not retrieve diff.";
        String statusText = "Error: could not retrieve status.";
        try {
            git.remoteList().call().stream()
                    .filter(remoteConfig -> remoteConfig.getName().equals("origin"))
                    .findFirst()
                    .ifPresent(remoteConfig -> {
                        // Get uri to "sniff out" any errors before clearing default text
                        String pushUri = remoteConfig.getURIs().getFirst().toString();
                        repoNameText.setLength(0);
                        repoNameText.append("Repository: ").append(pushUri);
                    });

            Status status = git.status().call();
            statusText = "Added: %s\nUncommitted: %s\nUntracked: %s".formatted(
                    status.getAdded(),
                    status.getUncommittedChanges(),
                    status.getUntracked());
        } catch (GitAPIException e) {
            System.out.println("Error retrieving git information: " + e);
        }
        Text repoName = new Text(repoNameText.toString());
        Text status = new Text(statusText);

        VBox vBox = new VBox(repoName, status);

        //Create an Alert with OK and Cancel buttons
        Alert alert = new Alert(Alert.AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Repository Info");

        alert.getDialogPane().setContent(vBox); // Set the custom pane as the content of the DialogPane
        Window window = alert.getDialogPane().getScene().getWindow(); // Fetch the Window from dialog pane
        Stage stage = (Stage) window; // Cast it to a Stage to get access to stage scenes
        stage.initStyle(StageStyle.UTILITY); // Set the Stage style

        // Show and wait for user action
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK) // If OK button is pressed
                .ifPresent(response -> okButtonPressed.set(true));

        return okButtonPressed.get();
    }
}
