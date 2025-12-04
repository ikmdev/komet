package dev.ikm.komet.sync.credential;

import javafx.application.Platform;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * PluginCredentialProvider is a custom implementation of the {@link CredentialsProvider} class.
 * It provides a way to retrieve credentials for a given URI by displaying a dialog box
 * for the user to enter their username and password.
 */
public class PluginCredentialProvider extends CredentialsProvider {
    private static final Logger LOG = LoggerFactory.getLogger(PluginCredentialProvider.class);
    @Override
    public boolean isInteractive() {
        return true;
    }

    /**
     * Checks if the credential items are supported.
     *
     * @param items the credential items to check
     * @return {@code true} if the credential items are supported, {@code false} otherwise
     */
    @Override
    public boolean supports(CredentialItem... items) {
        return true;
    }

    /**
     * Retrieves the credentials for the given URI by displaying a dialog box for the user to enter their
     * username and password.
     *
     * @param uri    the URI for which to retrieve the credentials
     * @param items  the credential items to retrieve (e.g., username, password)
     * @return {@code true} if the OK button is pressed on the dialog box, {@code false} otherwise
     * @throws UnsupportedCredentialItem if the credential items are not supported
     * @throws RuntimeException         if there is an interruption or execution exception
     */
    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        try {
            GetCredentialsTask task = new GetCredentialsTask(uri, items);
            Platform.runLater(task);
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
