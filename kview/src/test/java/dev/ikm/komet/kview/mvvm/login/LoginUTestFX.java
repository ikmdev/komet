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
package dev.ikm.komet.kview.mvvm.login;

import dev.ikm.komet.kview.mvvm.model.BasicUserManager;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import one.jpro.platform.auth.core.basic.UsernamePasswordCredentials;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class LoginUTestFX {

    private static final String SIGN_IN_BUTTON_ID = "#signInButton";
    private static final String USERNAME_TEXTFIELD_ID = "#usernameTextField";
    private static final String PASSWORD_TEXTFIELD_ID = "#passwordField";
    private static final String USERNAME_ERROR_LABEL_ID = "#usernameErrorLabel";
    private static final String PASSWORD_ERROR_LABEL_ID = "#passwordErrorLabel";
    private static final String AUTH_ERROR_LABEL_ID = "#authErrorLabel";

    public static final String USERNAME_ERROR_MSG = "Username is required and must be greater than 5 characters.";
    public static final String PASSWORD_ERROR_MSG = "Password is required and must be greater than 5 characters.";
    public static final String AUTH_ERROR_MSG = "Authentication failed: Invalid credentials.";

    private static final Logger LOG = LoggerFactory.getLogger(LoginUTestFX.class);
    private static final String ORIGINAL_USER_HOME = System.getProperty("user.home");
    private static final String SOLOR_DIR_NAME = "Solor";
    private static final String USERS_INI_FILE_NAME = "users.ini";
    private static final String MOCKED_USER = "user.test";
    private static final String MOCKED_PASSWORD = "secret123";
    private static final String MOCKED_USER_ROLE = "user";

    private Path tempDirectory;
    private Set<UsernamePasswordCredentials> userCredentials;
    private final FxRobot robot = new FxRobot();

    @BeforeAll
    void init() throws Exception {
        Path usersFile = Path.of(ORIGINAL_USER_HOME, SOLOR_DIR_NAME, USERS_INI_FILE_NAME);
        if (Files.notExists(usersFile)) {
            LOG.info("Users file not found. Creating a mock '" + USERS_INI_FILE_NAME + "' file.");
            usersFile = createUsersFile();
        } else {
            LOG.info("Users file found at '{}'.", usersFile);
        }

        userCredentials = getUserCredentials(usersFile);
        LOG.info("Loaded users: {}", userCredentials.stream().map(UsernamePasswordCredentials::getUsername).toList());
        assertFalse(userCredentials.isEmpty(),
                "No users loaded from '" + USERS_INI_FILE_NAME + "' file. Users file is either empty or corrupted.");
    }

    @BeforeEach
    void setup() throws Exception {
        FxToolkit.registerPrimaryStage();

        FxToolkit.setupStage(stage -> {
            JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                    LoginPageController.class.getResource("login-page.fxml"));
            BorderPane loginPane = loginNode.node();
            Scene scene = new Scene(loginPane, 650, 400);
            stage.setScene(scene);
            stage.show();
        });
    }

    private Path createUsersFile() {
        try {
            tempDirectory = Files.createTempDirectory("mockedHome");
            System.setProperty("user.home", tempDirectory.toString());
            Path solorDir = tempDirectory.resolve(SOLOR_DIR_NAME);
            if (Files.notExists(solorDir)) {
                Files.createDirectory(solorDir);
            }
            Path usersFile = solorDir.resolve(USERS_INI_FILE_NAME);
            try (BufferedWriter writer = Files.newBufferedWriter(usersFile)) {
                writer.write(String.format("""
                # Define users and passwords
                [users]
                %s = %s, %s
                """, MOCKED_USER, MOCKED_PASSWORD, MOCKED_USER_ROLE));
            }
            return usersFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory or '" + USERS_INI_FILE_NAME + "' file", e);
        }
    }

    public static Set<UsernamePasswordCredentials> getUserCredentials(Path filePath)
            throws ExecutionException, InterruptedException {
        BasicUserManager userManager = new BasicUserManager();
        return userManager.loadFileAsync(filePath.toString()).get();
    }

    @AfterEach
    void cleanup() throws Exception {
        FxToolkit.cleanupStages();
    }

    @AfterAll
    public void cleanupFiles() throws IOException {
        if (ORIGINAL_USER_HOME != null) {
            System.setProperty("user.home", ORIGINAL_USER_HOME);
        }
        if (tempDirectory != null) {
            try (Stream<Path> paths = Files.walk(tempDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    @Test
    public void shouldDisableSignInButtonWhenUsernameOrPasswordFieldIsEmpty() {
        Button signInButton = robot.lookup(SIGN_IN_BUTTON_ID).queryButton();
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when fields are empty.");

        enterUsername("test123");
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when password field is empty.");

        robot.clickOn(USERNAME_TEXTFIELD_ID).eraseText(7);
        enterPassword("test123");
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when username field is empty.");

        enterUsername("test123");
        assertFalse(signInButton.isDisabled(), "Sign-in button should be enabled when both fields have text.");
    }

    @Test
    public void testValidEmail() {
        enterUsername("user@example.com");
        String usernameError = getErrorLabelText(USERNAME_ERROR_LABEL_ID);
        assertEquals("", usernameError, "No error should be displayed for a valid email.");
    }

    @ParameterizedTest
    @MethodSource("inputValidationProvider")
    public void testInputValidation(String username, String password, String expectedUsernameError, String expectedPasswordError, String expectedAuthError) {
        enterUsername(username);
        enterPassword(password);
        clickSignInButton();
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(expectedUsernameError, getErrorLabelText(USERNAME_ERROR_LABEL_ID), "Username error message mismatch.");
        assertEquals(expectedPasswordError, getErrorLabelText(PASSWORD_ERROR_LABEL_ID), "Password error message mismatch.");
        assertEquals(expectedAuthError, getErrorLabelText(AUTH_ERROR_LABEL_ID), "Authentication error message mismatch.");
    }

    private Stream<Arguments> inputValidationProvider() {
        return Stream.of(
                Arguments.of("user", "test123", USERNAME_ERROR_MSG, "", ""),
                Arguments.of("test123", "test", "", PASSWORD_ERROR_MSG, ""),
                Arguments.of("invalidUser", "invalidPass", "", "", AUTH_ERROR_MSG)
        );
    }

    @ParameterizedTest
    @MethodSource("validUsersCredentialsProvider")
    public void testSuccessfulAuthentication(UsernamePasswordCredentials user) {
        enterUsername(user.getUsername());
        enterPassword(user.getPassword());
        clickSignInButton();
        WaitForAsyncUtils.waitForFxEvents();

        String authErrorText = getErrorLabelText(AUTH_ERROR_LABEL_ID);
        LOG.info("Authentication Error Label Text: '{}'", authErrorText);
        assertEquals("", authErrorText, "Authentication error label should be empty on successful login.");
    }

    private Stream<UsernamePasswordCredentials> validUsersCredentialsProvider() {
        if (userCredentials == null || userCredentials.isEmpty()) {
            throw new IllegalStateException("No valid users available for testing.");
        }
        return userCredentials.stream();
    }

    private void enterUsername(String username) {
        robot.clickOn(USERNAME_TEXTFIELD_ID).write(username);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void enterPassword(String password) {
        robot.clickOn(PASSWORD_TEXTFIELD_ID).write(password);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void clickSignInButton() {
        robot.clickOn(SIGN_IN_BUTTON_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private String getErrorLabelText(String labelId) {
        Label label = robot.lookup(labelId).queryAs(Label.class);
        return label.getText();
    }
}
