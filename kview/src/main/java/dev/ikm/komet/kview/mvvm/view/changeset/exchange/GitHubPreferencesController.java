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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import one.jpro.platform.utils.OpenLink;
import org.carlfx.cognitive.loader.InjectViewModel;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_EMAIL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_PASSWORD;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_URL;
import static dev.ikm.komet.kview.mvvm.view.changeset.exchange.GitPropertyName.GIT_USERNAME;

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

    @FXML
    private VBox gitUrlVBox;

    @FXML
    private TextField gitUrlTextField;

    @FXML
    public VBox gitEmailVBox;

    @FXML
    private TextField gitEmailTextField;

    @FXML
    private VBox gitUsernameVBox;

    @FXML
    private TextField gitUsernameTextField;

    @FXML
    private VBox gitPasswordVBox;

    @FXML
    private PasswordField gitPasswordField;

    @FXML
    private Hyperlink signUpHyperlink;

    @FXML
    private Button cancelButton;

    @FXML
    private Button connectButton;

    @InjectViewModel
    private GitHubPreferencesViewModel gitHubPreferencesViewModel;

    /**
     * Initializes the controller class.
     *
     * @param location  The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resources The resources used to localize the root object, or {@code null} if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up field bindings
        gitUrlTextField.textProperty().bindBidirectional(gitHubPreferencesViewModel.getProperty(GIT_URL));
        gitEmailTextField.textProperty().bindBidirectional(gitHubPreferencesViewModel.getProperty(GIT_EMAIL));
        gitUsernameTextField.textProperty().bindBidirectional(gitHubPreferencesViewModel.getProperty(GIT_USERNAME));
        gitPasswordField.textProperty().bindBidirectional(gitHubPreferencesViewModel.getProperty(GIT_PASSWORD));
        connectButton.disableProperty().bind(gitHubPreferencesViewModel.invalidProperty());

        // Set up validation change listeners
        setupValidation();

        // Set up property change listeners
        gitHubPreferencesViewModel.doOnChange(() -> gitHubPreferencesViewModel.validate(),
                GIT_URL, GIT_EMAIL, GIT_USERNAME, GIT_PASSWORD);

        // Trigger initial validation
        gitHubPreferencesViewModel.validate();

        signUpHyperlink.setOnAction(actionEvent -> openURL("https://github.com/signup"));
    }

    /**
     * Sets up the validation components for all input fields.
     * <p>
     * This method establishes the validation logic for each form field by:
     * <ul>
     *   <li>Creating validation components for each input field</li>
     *   <li>Binding validation error display to the view model's validation results</li>
     *   <li>Setting up error message interpolation and display in the UI</li>
     * </ul>
     * Validation results are automatically updated when field values change.
     */
    private void setupValidation() {
        // Create validation components and set up validation in one step
        Map.of(GIT_URL, new ValidationComponent(gitUrlVBox),
                GIT_EMAIL, new ValidationComponent(gitEmailVBox),
                GIT_USERNAME, new ValidationComponent(gitUsernameVBox),
                GIT_PASSWORD, new ValidationComponent(gitPasswordVBox)
        ).forEach((property, component) ->
                gitHubPreferencesViewModel.validateOnChange(property, messages -> {
                    // Clear previous error messages
                    component.clearError();

                    // Show new error messages if any
                    if (!messages.isEmpty()) {
                        component.showError(messages.stream()
                                .peek(gitHubPreferencesViewModel::updateErrors)
                                .map(msg -> msg.interpolate(gitHubPreferencesViewModel))
                                .collect(Collectors.joining("\n")));
                    }
                })
        );
    }

    /**
     * Handles the connect button click event.
     * <p>
     * This method:
     * <ul>
     *   <li>Attempts to save the GitHub configuration</li>
     *   <li>Validates all input fields</li>
     *   <li>Persists valid settings to the DAO layer</li>
     * </ul>
     *
     * @param actionEvent The event that triggered this handler
     */
    @FXML
    public void handleConnectButtonEvent(ActionEvent actionEvent) {
        gitHubPreferencesViewModel.save();
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
}