/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.kview.mvvm.view.changeset.exchange;

import com.jpro.webapi.WebAPI;
import dev.ikm.komet.kview.mvvm.viewmodel.GitHubPreferencesViewModel;
import dev.ikm.komet.preferences.KometPreferences;
import dev.ikm.komet.preferences.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import one.jpro.platform.utils.OpenLink;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.stream.Stream;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.*;

/**
 * Controller class for handling GitHub preferences in the application.
 * <p>
 * This controller manages the UI components and user interactions for configuring
 * GitHub repository settings. It validates input fields, displays error messages,
 * and persists GitHub connection information to the application preferences.
 * <p>
 * The controller follows the MVVM (Model-View-ViewModel) pattern, with field validation
 * and business logic delegated to the associated {@link GitHubPreferencesViewModel}.
 *
 * @see GitHubPreferencesViewModel
 * @see KometPreferences
 */
public class GitHubPreferencesController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(GitHubPreferencesController.class);

    @FXML
    private TextField gitUrlTextField;

    @FXML
    private TextField gitEmailTextField;

    @FXML
    private TextField gitUsernameTextField;

    @FXML
    private PasswordField gitPasswordField;

    @FXML
    private Text errorText;

    @FXML
    private Hyperlink signUpHyperlink;

    @FXML
    private Button cancelButton;

    @FXML
    private Button connectButton;

    @InjectViewModel
    private GitHubPreferencesViewModel githubPreferencesViewModel;

    /**
     * Initializes the controller class.
     *
     * @param location  The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resources The resources used to localize the root object, or {@code null} if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ChangeListener<String> isNotPopulatedListener = (observable, oldValue, newValue) ->
                githubPreferencesViewModel.getValidators(IS_NOT_POPULATED).stream()
                        .findAny()
                        .ifPresent(validator -> validator.apply(null, githubPreferencesViewModel));

        gitUrlTextField.textProperty().addListener(isNotPopulatedListener);
        gitEmailTextField.textProperty().addListener(isNotPopulatedListener);
        gitUsernameTextField.textProperty().addListener(isNotPopulatedListener);
        gitPasswordField.textProperty().addListener(isNotPopulatedListener);

        gitUrlTextField.textProperty().bindBidirectional(githubPreferencesViewModel.getProperty(GIT_URL));
        gitEmailTextField.textProperty().bindBidirectional(githubPreferencesViewModel.getProperty(GIT_EMAIL));
        gitUsernameTextField.textProperty().bindBidirectional(githubPreferencesViewModel.getProperty(GIT_USERNAME));
        gitPasswordField.textProperty().bindBidirectional(githubPreferencesViewModel.getProperty(GIT_PASSWORD));
        connectButton.disableProperty().bind(githubPreferencesViewModel.getProperty(CONNECT_BUTTON_STATE));

        signUpHyperlink.setOnAction(actionEvent -> openURL("https://github.com/signup"));
    }

    /**
     * Handles the connect button click event.
     * <p>
     * This method:
     * <ul>
     *   <li>Attempts to save the GitHub configuration</li>
     *   <li>Validates all input fields</li>
     *   <li>Displays appropriate error messages if validation fails</li>
     *   <li>Persists valid settings to application user preferences</li>
     * </ul>
     *
     * @param actionEvent The event that triggered this handler
     */
    @FXML
    public void handleConnectButtonEvent(ActionEvent actionEvent) {
        githubPreferencesViewModel.save();

        if (githubPreferencesViewModel.hasErrorMsgs()) {
            String gitUrlErrorMessage = "";
            String gitEmailErrorMessage = "";
            String usernameErrorMessage = "";
            String passwordErrorMessage = "";

            for (ValidationMessage validationMessage : githubPreferencesViewModel.getValidationMessages()) {
                githubPreferencesViewModel.updateErrors(validationMessage);

                final String propName = validationMessage.propertyName();
                final String message = validationMessage.interpolate(githubPreferencesViewModel);

                if (GIT_URL.name().equals(propName)) {
                    gitUrlErrorMessage = message;
                }

                if (GIT_EMAIL.name().equals(propName)) {
                    gitEmailErrorMessage = message;
                }

                if (GIT_USERNAME.name().equals(propName)) {
                    usernameErrorMessage = message;
                }

                if (GIT_PASSWORD.name().equals(propName)) {
                    passwordErrorMessage = message;
                }
            }

            // set the error text after processing all validation messages
            errorText.setText(Stream.of(gitUrlErrorMessage, gitEmailErrorMessage, usernameErrorMessage, passwordErrorMessage)
                    .filter(error -> !error.isEmpty())
                    .reduce((first, second) -> first + " " + second)
                    .orElse("Please fill in all fields."));
        } else {
            // Clear the error labels
            errorText.setText("");

            // Save GitHub preferences
            saveToPreferences();
        }
    }

    @FXML
    public void handleCancelButtonEvent(ActionEvent actionEvent) {

    }

    /**
     * Returns the cancel button UI component.
     *
     * @return The cancel button instance
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Returns the connect button UI component.
     *
     * @return The connect button instance
     */
    public Button getConnectButton() {
        return connectButton;
    }

    /**
     * Opens a URL in the appropriate way depending on the runtime environment.
     * <p>
     * This method handles opening URLs differently based on whether the application
     * is running in a browser environment:
     * <ul>
     *   <li>In a browser environment, it opens the URL in a new browser tab using WebAPI</li>
     *   <li>In a desktop environment, it opens the URL using the default system browser</li>
     * </ul>
     *
     * @param url The URL to open, which should be a valid web address including protocol
     */
    private void openURL(final String url) {
        if (WebAPI.isBrowser()) {
            WebAPI.getWebAPI(signUpHyperlink, webAPI -> webAPI.openURLAsTab(url));
        } else {
            OpenLink.openURL(url);
        }
    }

    /**
     * Saves the current GitHub configuration to user preferences.
     * <p>
     * This method persists all GitHub-related settings (URL, email, username, and password)
     * to the application's user preferences store and synchronizes the preferences to ensure
     * they are written to the backing store.
     */
    void saveToPreferences() {
        KometPreferences userPreferences = Preferences.get().getUserPreferences();
        setGitUrl(userPreferences, gitUrlTextField.getText());
        setGitEmail(userPreferences, gitEmailTextField.getText());
        setGitUser(userPreferences, gitUsernameTextField.getText());
        setGitPassword(userPreferences, gitPasswordField.getText().toCharArray());
        try {
            userPreferences.sync();
            LOG.info("GitHub preferences saved successfully.");
        } catch (BackingStoreException ex) {
            LOG.error("Error syncing preferences: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Stores the Git repository URL in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param gitRepoUrl The Git repository URL to store
     */
    void setGitUrl(KometPreferences preferences, String gitRepoUrl) {
        preferences.put(GIT_URL, gitRepoUrl);
    }

    /**
     * Stores the Git user email in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param gitEmail The Git user email to store
     */
    void setGitEmail(KometPreferences preferences, String gitEmail) {
        preferences.put(GIT_EMAIL, gitEmail);
    }

    /**
     * Stores the Git username in the specified preferences.
     *
     * @param preferences The preferences instance to update
     * @param username The Git username to store
     */
    void setGitUser(KometPreferences preferences, String username) {
        preferences.put(GIT_USERNAME, username);
    }

    /**
     * Stores the Git password in the specified preferences.
     * <p>
     * The password is stored using the secure password storage mechanism
     * provided by {@link KometPreferences#putPassword}.
     *
     * @param preferences The preferences instance to update
     * @param password The Git password to store as a character array
     */
    void setGitPassword(KometPreferences preferences, char[] password) {
        preferences.putPassword(GIT_PASSWORD, password);
    }
}
