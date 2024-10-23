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
package dev.ikm.komet.kview.mvvm.view.login;

import dev.ikm.komet.kview.mvvm.viewmodel.LoginViewModel;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.validator.ValidationMessage;

import java.net.URL;
import java.util.ResourceBundle;

import static dev.ikm.komet.kview.mvvm.view.login.LoginViewPropertyName.*;

/**
 * Controller class for the Login Page.
 * <p>
 * This class handles user interactions with the login page, including input validation,
 * property bindings between the view and the view model, and the authentication process.
 * </p>
 */
public class LoginPageController implements Initializable {

    @FXML
    private TextField usernameTextField;

    @FXML
    private Label usernameErrorLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Button signInButton;

    @FXML
    private Label authErrorLabel;

    @InjectViewModel
    private LoginViewModel loginViewModel;

    /**
     * Initializes the controller class.
     *
     * @param location  The location used to resolve relative paths for the root object, or {@code null} if not known.
     * @param resources The resources used to localize the root object, or {@code null} if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ChangeListener<String> isNotPopulatedListener = (observable, oldValue, newValue) ->
                loginViewModel.getValidators(IS_NOT_POPULATED).stream()
                .findAny()
                .ifPresent(validator -> validator.apply(null, loginViewModel));

        usernameTextField.textProperty().addListener(isNotPopulatedListener);
        passwordField.textProperty().addListener(isNotPopulatedListener);

        usernameTextField.textProperty().bindBidirectional(loginViewModel.getProperty(USERNAME));
        passwordField.textProperty().bindBidirectional(loginViewModel.getProperty(PASSWORD));
        signInButton.disableProperty().bind(loginViewModel.getProperty(SIGN_IN_BUTTON_STATE));
        authErrorLabel.textProperty().bind(loginViewModel.getProperty(AUTH_ERROR));
    }

    /**
     * Handles the action event when the sign-in button is clicked.
     *
     * @param actionEvent The action event triggered by the sign-in button.
     */
    @FXML
    private void signInAction(ActionEvent actionEvent) {
        loginViewModel.save();

        if (loginViewModel.hasErrorMsgs()) {
            String usernameErrorMessage = "";
            String passwordErrorMessage = "";

            for (ValidationMessage validationMessage : loginViewModel.getValidationMessages()) {
                loginViewModel.updateErrors(validationMessage);

                String propName = validationMessage.propertyName();
                String message = validationMessage.interpolate(loginViewModel);

                if (USERNAME.name().equals(propName)) {
                    usernameErrorMessage = message;
                } else if (PASSWORD.name().equals(propName)) {
                    passwordErrorMessage = message;
                }
            }

            // set the error labels after processing all validation messages
            usernameErrorLabel.setText(usernameErrorMessage);
            passwordErrorLabel.setText(passwordErrorMessage);
        } else {
            // Clear the error labels
            usernameErrorLabel.setText("");
            passwordErrorLabel.setText("");

            // Perform authentication
            final String username = loginViewModel.getValue(USERNAME);
            final String password = loginViewModel.getValue(PASSWORD);
            loginViewModel.authenticate(username, password);
        }
    }
}
