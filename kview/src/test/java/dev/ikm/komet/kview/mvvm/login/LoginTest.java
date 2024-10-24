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
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class LoginTest {

    private static final String SIGN_IN_BUTTON_ID = "#signInButton";
    private static final String USERNAME_TEXTFIELD_ID = "#usernameTextField";
    private static final String PASSWORD_TEXTFIELD_ID = "#passwordField";
    private static final String USERNAME_ERROR_LABEL_ID = "#usernameErrorLabel";
    private static final String PASSWORD_ERROR_LABEL_ID = "#passwordErrorLabel";
    private static final String AUTH_ERROR_LABEL_ID = "#authErrorLabel";

    public static final String USERNAME_ERROR_MSG = "Username is required and must be greater than 5 characters.";
    public static final String PASSWORD_ERROR_MSG = "Password is required and must be greater than 5 characters.";
    public static final String AUTH_ERROR_MSG = "Authentication failed: Invalid credentials.";

    private static final Logger LOG = LoggerFactory.getLogger(LoginTest.class);
    private static final String ORIGINAL_USER_HOME = System.getProperty("user.home");
    private static final String SOLOR_DIR_NAME = "Solor";
    private static final String USERS_INI_FILE_NAME = "users.ini";
    private static final String MOCKED_USER = "user.test";
    private static final String MOCKED_PASSWORD = "secret123";
    private static final String MOCKED_USER_ROLE = "user";

    private Path tempDirectory;
    private Set<UsernamePasswordCredentials> userCredentials;
    private MockedStatic<EvtBusFactory> mockedStaticEvtBusFactory;
    private final FxRobot robot = new FxRobot();

    /**
     * Initialize users before any tests run.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Checks if the users file exists in the user's home directory.</li>
     *   <li>If the users file does not exist, it creates the file with mock user data.</li>
     *   <li>Loads user credentials from the users file.</li>
     *   <li>Logs the loaded users and asserts that the users file is not empty or corrupted.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if an error occurs during setup
     */
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

    /**
     * Sets up the test environment before each test.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Registers the primary stage in TestFX.</li>
     *   <li>Mocks the {@link EvtBusFactory} to provide a mock event bus.</li>
     *   <li>Loads the LoginPage from FXML and displays it in the stage.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if an error occurs during setup
     */
    @BeforeEach
    void setup() throws Exception {
        // Register the primary stage in TestFX
        FxToolkit.registerPrimaryStage();

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

    /**
     * Creates the temporary directory and the users file.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Creates a temporary directory for the user home.</li>
     *   <li>Sets the system property for user.home to the temporary directory.</li>
     *   <li>Creates the "Solor" directory if it does not exist.</li>
     *   <li>Creates and populates the users file with mock user data.</li>
     * </ul>
     * </p>
     *
     * @return the path to the created users file
     * @throws RuntimeException if an error occurs during the creation of the directory or file
     */
    private Path createUsersFile() {
        try {
            // Create temporary directory for user home
            tempDirectory = Files.createTempDirectory("mockedHome");

            // Set the system property for user.home to the temporary directory
            System.setProperty("user.home", tempDirectory.toString());
            // Create the "Solor" directory
            Path solorDir = tempDirectory.resolve(SOLOR_DIR_NAME);
            if (Files.notExists(solorDir)) {
                Files.createDirectory(solorDir);
            }

            // Create and populate the users file
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

    /**
     * Loads user credentials from the specified file asynchronously and returns the set of users.
     *
     * @param filePath the path to the file containing user credentials
     * @return a set of {@link UsernamePasswordCredentials} loaded from the file
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public static Set<UsernamePasswordCredentials> getUserCredentials(Path filePath)
            throws ExecutionException, InterruptedException {
        BasicUserManager userManager = new BasicUserManager();
        return userManager.loadFileAsync(filePath.toString()).get();
    }

    /**
     * Cleans up the test environment after each test.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Cleans up TestFX stages and mocked resources.</li>
     *   <li>Closes the mocked static resources for {@link EvtBusFactory}.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if an error occurs during cleanup
     */
    @AfterEach
    void cleanup() throws Exception {
        // Clean up TestFX stages and mocked resources
        FxToolkit.cleanupStages();

        // Close the mocked static resources for EvtBusFactory
        if (mockedStaticEvtBusFactory != null) {
            mockedStaticEvtBusFactory.close();
        }
    }

    /**
     * Cleans up the temporary directory and restores the original <code>user.home</code> system property.
     *
     * @throws IOException if an I/O error occurs
     */
    @AfterAll
    public void cleanupFiles() throws IOException {
        // Restore the original user.home system property
        if (ORIGINAL_USER_HOME != null) {
            System.setProperty("user.home", ORIGINAL_USER_HOME);
        }

        // Clean up the temporary directory
        if (tempDirectory != null) {
            try (Stream<Path> paths = Files.walk(tempDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        }
    }

    /**
     * Test to verify that the sign-in button is disabled when either the username or password field is empty.
     * The sign-in button should only be enabled when both fields have text.
     */
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

    /**
     * Test to verify that no error is displayed for a valid email.
     * This test enters a valid email in the username field and checks
     * that the username error label is empty.
     */
    @Test
    public void testValidEmail() {
        enterUsername("user@example.com");

        String usernameError = getErrorLabelText(USERNAME_ERROR_LABEL_ID);
        assertEquals("", usernameError, "No error should be displayed for a valid email.");
    }

    /**
     * Test to verify input validation for the login form.
     * <p>
     * This test uses parameterized inputs to check various scenarios for username and password validation.
     * It verifies that the appropriate error messages are displayed for invalid inputs.
     * </p>
     *
     * @param username the username to test
     * @param password the password to test
     * @param expectedUsernameError the expected error message for the username field
     * @param expectedPasswordError the expected error message for the password field
     * @param expectedAuthError the expected error message for authentication
     */
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

    /**
     * Provides a stream of arguments for input validation tests.
     * <p>
     * This method returns a stream of arguments, each containing a username, password,
     * expected username error message, expected password error message, and expected
     * authentication error message. These arguments are used to test various scenarios
     * for username and password validation.
     * </p>
     *
     * @return a stream of {@link Arguments} for input validation tests
     */
    private Stream<Arguments> inputValidationProvider() {
        return Stream.of(
                Arguments.of("user", "test123", USERNAME_ERROR_MSG, "", ""),
                Arguments.of("test123", "test", "", PASSWORD_ERROR_MSG, ""),
                Arguments.of("invalidUser", "invalidPass", "", "", AUTH_ERROR_MSG)
        );
    }

    /**
     * Test to verify successful authentication.
     * <p>
     * This test enters valid credentials and clicks the sign-in button.
     * It then checks that the authentication error label is empty, indicating a successful login.
     * </p>
     *
     * @param user the valid user credentials to test
     */
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

    /**
     * Provides a stream of valid users.
     * <p>
     * This method returns a stream of {@link UsernamePasswordCredentials} representing valid users.
     * If no valid users are available, it throws an {@link IllegalStateException}.
     * </p>
     *
     * @return a stream of {@link UsernamePasswordCredentials} representing valid users
     * @throws IllegalStateException if no valid users are available for testing
     */
    private Stream<UsernamePasswordCredentials> validUsersCredentialsProvider() {
        if (userCredentials == null || userCredentials.isEmpty()) {
            throw new IllegalStateException("No valid users available for testing.");
        }
        return userCredentials.stream();
    }

    /**
     * Helper method to enter username in the username field.
     *
     * @param username the username to enter
     */
    private void enterUsername(String username) {
        robot.clickOn(USERNAME_TEXTFIELD_ID).write(username);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Helper method to enter password in the password field.
     *
     * @param password the password to enter
     */
    private void enterPassword(String password) {
        robot.clickOn(PASSWORD_TEXTFIELD_ID).write(password);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Helper method to click the sign-in button.
     */
    private void clickSignInButton() {
        robot.clickOn(SIGN_IN_BUTTON_ID);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Helper method to get the text from an error label.
     *
     * @param labelId the ID of the label
     * @return the text of the label
     */
    private String getErrorLabelText(String labelId) {
        Label label = robot.lookup(labelId).queryAs(Label.class);
        return label.getText();
    }
}
