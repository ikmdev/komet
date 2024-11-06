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
package dev.ikm.komet.kview.mvvm.viewmodel;

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.events.SignInUserEvent;
import dev.ikm.komet.kview.mvvm.model.BasicUserManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import one.jpro.platform.auth.core.AuthAPI;
import one.jpro.platform.auth.core.basic.UsernamePasswordCredentials;
import one.jpro.platform.auth.core.basic.provider.BasicAuthenticationProvider;
import org.carlfx.cognitive.validator.MessageType;
import org.carlfx.cognitive.validator.ValidationMessage;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ikm.komet.kview.events.EventTopics.USER_TOPIC;
import static dev.ikm.komet.kview.events.SignInUserEvent.SIGN_IN_USER;
import static dev.ikm.komet.kview.mvvm.view.login.LoginViewPropertyName.*;

/**
 * ViewModel for the Login View.
 * <p>
 * This class manages the state and validation logic for the login view,
 * including handling user input, validating credentials, and performing authentication.
 * It interacts with the {@link BasicUserManager} for user management and uses
 * {@link BasicAuthenticationProvider} for authentication operations.
 * </p>
 * <p>
 * The {@code LoginViewModel} extends {@link ValidationViewModel}, leveraging its
 * property management and validation framework to ensure that user inputs meet
 * the required criteria before attempting authentication.
 * </p>
 */
public class LoginViewModel extends ValidationViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(LoginViewModel.class);

    /**
     * Regular expression pattern for validating email addresses.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * The root folder where user-related files are stored.
     */
    private final File rootFolder = new File(System.getProperty("user.home"), "Solor");

    /**
     * The root folder where user-related files are stored.
     */
    private final File usersFile = new File(rootFolder, "users.ini");

    /**
     * Manager for basic user operations such as loading and saving users.
     */
    private final BasicUserManager basicUserManager = new BasicUserManager();

    /**
     * Provider for handling basic authentication operations.
     */
    private final BasicAuthenticationProvider basicAuthProvider = AuthAPI.basicAuth()
            .userManager(basicUserManager)
            .create();

    /**
     * Event bus for publishing and subscribing to events.
     */
    private final EvtBus eventBus;

    /**
     * Constructs a new {@code LoginViewModel} and initializes properties and validators.
     * <p>
     * This constructor sets up the necessary properties for username, password, sign-in button state,
     * and error messages. It also adds validators to ensure that the username and password meet
     * the required criteria and manages the state of the sign-in button based on input population.
     * Additionally, it asynchronously loads the users file.
     * </p>
     */
    public LoginViewModel() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        addProperty(USERNAME, "")
                .addValidator(USERNAME, USERNAME.name(), (ReadOnlyStringProperty prop, ViewModel viewModel) -> {
                    if (prop.isEmpty().get() || prop.isNotEmpty().get() && prop.get().length() < 5) {
                        return new ValidationMessage(USERNAME, MessageType.ERROR,
                                "%s is required and must be greater than 5 characters.".formatted(USERNAME.getPropertyName()));
                    }
                    // Clear any previous authentication errors
                    setPropertyValue(AUTH_ERROR, "");
                    return VALID;
                })
                .addValidator(USERNAME, USERNAME.name(), (ReadOnlyStringProperty prop, ViewModel vm) -> {
                    if (prop.isNotEmpty().get() && prop.get().contains("@")) {
                        String email = prop.get();
                        Matcher matcher = EMAIL_PATTERN.matcher(email);
                        if (!matcher.matches()) {
                            return new ValidationMessage(USERNAME, MessageType.ERROR,
                                    "%s is not a valid email address.".formatted(email));
                        }
                    }
                    // Clear any previous authentication errors
                    setPropertyValue(AUTH_ERROR, "");
                    return VALID;
                });

        addProperty(PASSWORD, "")
                .addValidator(PASSWORD, PASSWORD.name(), (ReadOnlyStringProperty prop, ViewModel vm) -> {
                    if (prop.isEmpty().get() || prop.isNotEmpty().get() && prop.get().length() < 5) {
                        return new ValidationMessage(PASSWORD, MessageType.ERROR,
                                "%s is required and must be greater than 5 characters.".formatted(PASSWORD.getPropertyName()));
                    }
                    // Clear any previous authentication errors
                    setPropertyValue(AUTH_ERROR, "");
                    return VALID;
                });

        addProperty(SIGN_IN_BUTTON_STATE, true); // disable sign in button by default
        addValidator(IS_NOT_POPULATED, IS_NOT_POPULATED.name(), (Void prop, ViewModel vm) -> {
            // If any fields are empty, the form is not fully populated (invalid)
            if (vm.getPropertyValue(USERNAME).toString().isBlank()
                    || vm.getPropertyValue(PASSWORD).toString().isBlank()) {
                // Disable the sign-in button
                vm.setPropertyValue(SIGN_IN_BUTTON_STATE, true);
                return new ValidationMessage(SIGN_IN_BUTTON_STATE, MessageType.ERROR,
                        "Please enter your username and password.");
            }

            // Enable the sign-in button
            vm.setPropertyValue(SIGN_IN_BUTTON_STATE, false);
            // Clear any previous authentication errors
            setPropertyValue(AUTH_ERROR, "");
            return VALID;
        });

        addProperty(USERNAME_ERROR, "");
        addProperty(PASSWORD_ERROR, "");
        addProperty(AUTH_ERROR, "");

        // Load users file asynchronously in the background
        basicUserManager.loadFileAsync(usersFile.getAbsolutePath())
                .thenAccept(result -> LOG.info("Users file loaded successfully."))
                .exceptionally(throwable -> {
                    LOG.error("Failed to load users file.", throwable);
                    return null;
                });
    }

    /**
     * Updates the error message for a specific property based on the provided validation message.
     *
     * @param validationMessage The validation message containing the property name and error details.
     */
    public void updateErrors(ValidationMessage validationMessage) {
        setPropertyValue(validationMessage.propertyName() + ERROR, validationMessage.interpolate(this));
    }

    /**
     * Authenticates the user with the provided username and password.
     * <p>
     * This method attempts to authenticate the user using the {@link BasicAuthenticationProvider}.
     * On successful authentication, it publishes a {@link SignInUserEvent} to notify other components.
     * If authentication fails, it sets an appropriate error message.
     * </p>
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
    public void authenticate(String username, String password) {
        LOG.info("Authenticating user: {}", username);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        basicAuthProvider.authenticate(credentials)
                .thenAccept(user -> {
                    // login successful
                    LOG.info("Authentication successful: {}", user.getName());
                    Platform.runLater(() -> {
                        // clear the password
                        setPropertyValue(PASSWORD, "");
                        // publish the user via the sign in event
                        eventBus.publish(USER_TOPIC, new SignInUserEvent(this, SIGN_IN_USER, user));
                    });

                    // Clear view model's values to remove passwords from memory
                    setValue(USERNAME, "");
                    setValue(PASSWORD, "");
                    reset();
                })
                .exceptionally(throwable -> {
                    LOG.error("Authentication failed: {}", throwable.getCause().getMessage());
                    Platform.runLater(() -> {
                        // set the error message
                        setPropertyValue(AUTH_ERROR, "Authentication failed: Invalid credentials.");
                    });
                    return null;
                });
    }
}
