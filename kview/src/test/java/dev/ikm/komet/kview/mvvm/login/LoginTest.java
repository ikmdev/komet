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

import dev.ikm.komet.framework.events.EvtBus;
import dev.ikm.komet.framework.events.EvtBusFactory;
import dev.ikm.komet.kview.mvvm.view.login.LoginPageController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class LoginTest extends ApplicationTest {

    private Path tempDirectory;
    private String originalUserHome;
    private MockedStatic<EvtBusFactory> mockedStaticEvtBusFactory;

    /**
     * Creates the temporary directory and the users.ini file.
     */
    private void createUsersFile() {
        try {
            // Create temporary directory for user home
            tempDirectory = Files.createTempDirectory("mockedHome");

            // Store the original user.home property to restore it later
            originalUserHome = System.getProperty("user.home");

            // Set the system property for user.home to the temporary directory
            System.setProperty("user.home", tempDirectory.toString());

            // Create the "Solor" directory
            Path solorDir = tempDirectory.resolve("Solor");
            if (Files.notExists(solorDir)) {
                Files.createDirectory(solorDir);
            }

            // Create and populate the users.ini file
            Path usersFile = solorDir.resolve("users.ini");
            try (BufferedWriter writer = Files.newBufferedWriter(usersFile)) {
                writer.write("""
                    # Define users and passwords
                    [users]
                    user.test = secret123, user
                    """);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory or users.ini file", e);
        }
    }

    /**
     * Cleans up the temporary directory and restores the original user.home system property.
     */
    private void cleanupFiles() throws IOException {
        // Restore the original user.home system property
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }

        // Clean up the temporary directory
        if (tempDirectory != null) {
            try (Stream<Path> paths = Files.walk(tempDirectory)) {
                paths.map(Path::toFile).forEach(File::delete);
            }
        }
    }

    @BeforeEach
    void setup() throws Exception {
        // Register the primary stage in TestFX
        FxToolkit.registerPrimaryStage();

        // Create the users.ini file in the temporary directory
        createUsersFile();

        // Launch the application
        FxToolkit.setupStage(stage -> {
            // Mock the EvtBusFactory to provide a mock event bus
            EvtBus mockEventBus = mock(EvtBus.class);
            mockedStaticEvtBusFactory = mockStatic(EvtBusFactory.class);
            mockedStaticEvtBusFactory.when(EvtBusFactory::getDefaultEvtBus).thenReturn(mockEventBus);

            // Load the LoginPage from FXML and display it in the stage
            JFXNode<BorderPane, Void> loginNode = FXMLMvvmLoader.make(
                    LoginPageController.class.getResource("login-page.fxml"));
            BorderPane loginPane = loginNode.node();
            Scene scene = new Scene(loginPane, 650, 400);
            stage.setScene(scene);
            stage.show();
        });
    }

    @AfterEach
    void cleanup() throws Exception {
        // Clean up TestFX stages and mocked resources
        FxToolkit.cleanupStages();

        // Close the mocked static resources for EvtBusFactory
        if (mockedStaticEvtBusFactory != null) {
            mockedStaticEvtBusFactory.close();
        }

        // Clean up the temporary directory and files
        cleanupFiles();
    }

    @Test
    public void shouldDisableSignInButtonWhenUsernameOrPasswordFieldIsEmpty() {
        // Initial state: both fields are empty, sign-in button should be disabled
        Button signInButton = lookup("#signInButton").queryButton();
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when fields are empty.");

        // Enter only username
        clickOn("#usernameTextField").write("test123");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when password field is empty.");

        // Clear username, enter only password
        clickOn("#usernameTextField").eraseText(7);
        clickOn("#passwordField").write("test123");
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(signInButton.isDisabled(), "Sign-in button should be disabled when username field is empty.");

        // Enter both username and password
        clickOn("#usernameTextField").write("test123");
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(signInButton.isDisabled(), "Sign-in button should be enabled when both fields have text.");
    }

    @Test
    public void testValidEmail() {
        // Simulate user entering a valid email
        clickOn("#usernameTextField").write("user@example.com");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that no validation error is displayed
        Label usernameErrorLabel = lookup("#usernameErrorLabel").query();
        assertEquals("", usernameErrorLabel.getText(), "No error should be displayed for a valid email.");
    }

    @Test
    public void testUsernameTooShort() {
        // Simulate user entering a username shorter than 5 characters
        clickOn("#usernameTextField").write("user");
        clickOn("#passwordField").write("test123");
        clickOn("#signInButton").clickOn();
        WaitForAsyncUtils.waitForFxEvents();
        Label usernameErrorLabel = lookup("#usernameErrorLabel").query();
        System.out.println("Validation Message: '" + usernameErrorLabel.getText() + "'");
        assertEquals("Username is required and must be greater then 5 characters.", usernameErrorLabel.getText());
    }

    @Test
    public void testPasswordTooShort() {
        // Simulate user entering a password shorter than 5 characters
        clickOn("#usernameTextField").write("test123");
        clickOn("#passwordField").write("test");
        clickOn("#signInButton").clickOn();
        WaitForAsyncUtils.waitForFxEvents();
        Label passwordErrorLabel = lookup("#passwordErrorLabel").query();
        System.out.println("Validation Message: '" + passwordErrorLabel.getText() + "'");
        assertEquals("Password is required and must be greater then 5 characters.", passwordErrorLabel.getText());
    }

    @Test
    public void testInvalidCredentials() {
        // Simulate user entering invalid credentials
        clickOn("#usernameTextField").write("invalidUser");
        clickOn("#passwordField").write("invalidPassword");
        clickOn("#signInButton").clickOn();
        WaitForAsyncUtils.waitForFxEvents();
        Label authErrorLabel = lookup("#authErrorLabel").query();
        System.out.println("Validation Message: '" + authErrorLabel.getText() + "'");
        assertEquals("Authentication failed: Invalid credentials.", authErrorLabel.getText());
    }

    @Test
    public void testSuccessfulAuthentication() {
        // Simulate user entering valid credentials
        clickOn("#usernameTextField").write("user.test");
        clickOn("#passwordField").write("secret123");
        clickOn("#signInButton").clickOn();
        WaitForAsyncUtils.waitForFxEvents();
        Label authErrorLabel = lookup("#authErrorLabel").query();
        System.out.println("Validation Message: '" + authErrorLabel.getText() + "'");
        assertEquals("", authErrorLabel.getText());
    }
}
