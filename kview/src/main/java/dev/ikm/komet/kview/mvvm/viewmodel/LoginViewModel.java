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

public class LoginViewModel extends ValidationViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(LoginViewModel.class);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final File rootFolder = new File(System.getProperty("user.home"), "Solor");
    private final File usersFile = new File(rootFolder, "users.ini");
    private final BasicUserManager basicUserManager = new BasicUserManager();
    private final BasicAuthenticationProvider basicAuthProvider = AuthAPI.basicAuth()
            .userManager(basicUserManager)
            .create();

    private final EvtBus eventBus;

    public LoginViewModel() {
        eventBus = EvtBusFactory.getDefaultEvtBus();

        addProperty(USERNAME, "")
                .addValidator(USERNAME, USERNAME.name(), (ReadOnlyStringProperty prop, ViewModel viewModel) -> {
                    if (prop.isEmpty().get() || prop.isNotEmpty().get() && prop.get().length() < 5) {
                        return new ValidationMessage(USERNAME, MessageType.ERROR,
                                "%s is required and must be greater then 5 characters.".formatted(USERNAME.getPropertyName()));
                    }
                    // clear any previous auth errors
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
                    // clear any previous auth errors
                    setPropertyValue(AUTH_ERROR, "");
                    return VALID;
                });

        addProperty(PASSWORD, "")
                .addValidator(PASSWORD, PASSWORD.name(), (ReadOnlyStringProperty prop, ViewModel vm) -> {
                    if (prop.isEmpty().get() || prop.isNotEmpty().get() && prop.get().length() < 5) {
                        return new ValidationMessage(PASSWORD, MessageType.ERROR,
                                "%s is required and must be greater then 5 characters.".formatted(PASSWORD.getPropertyName()));
                    }
                    // clear any previous auth errors
                    setPropertyValue(AUTH_ERROR, "");
                    return VALID;
                });

        addProperty(SIGN_IN_BUTTON_STATE, true); // disable sign in button by default
        addValidator(IS_NOT_POPULATED, IS_NOT_POPULATED.name(), (Void prop, ViewModel vm) -> {
            // if any fields are empty then it is not populated (invalid)
            if (vm.getPropertyValue(USERNAME).toString().isBlank()
                    || vm.getPropertyValue(PASSWORD).toString().isBlank()) {
                // disable the sign-in button
                vm.setPropertyValue(SIGN_IN_BUTTON_STATE, true);
                return new ValidationMessage(SIGN_IN_BUTTON_STATE, MessageType.ERROR,
                        "Please enter your username and password.");
            }

            // enable the sign-in button
            vm.setPropertyValue(SIGN_IN_BUTTON_STATE, false);
            // clear any previous auth errors
            setPropertyValue(AUTH_ERROR, "");
            return VALID;
        });

        addProperty(USERNAME_ERROR, "");
        addProperty(PASSWORD_ERROR, "");
        addProperty(AUTH_ERROR, "");

        // load users file asynchronously in the background
        basicUserManager.loadFileAsync(usersFile.getAbsolutePath())
                .thenAccept(result -> LOG.info("Users file loaded successfully."))
                .exceptionally(throwable -> {
                    LOG.error("Failed to load users file.", throwable);
                    return null;
                });
    }

    public void updateErrors(ValidationMessage validationMessage) {
        setPropertyValue(validationMessage.propertyName() + ERROR, validationMessage.interpolate(this));
    }

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
