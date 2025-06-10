package dev.ikm.komet.kview.mvvm.viewmodel.test;

import dev.ikm.komet.kview.mvvm.view.confirmation.ConfirmationPaneController;
import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.carlfx.cognitive.loader.Config;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationPaneViewModel.ConfirmationPropertyName.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ApplicationExtension.class)
public class ConfirmationPaneTestFX {

    private static final String TITLE_LABEL_ID = "#confirmationTitle";
    private static final String MESSAGE_LABEL_ID = "#confirmationMessage";
    private static final String CLOSE_BUTTON_ID = "#closePropertiesPanel";

    private final FxRobot robot = new FxRobot();
    private ConfirmationPaneViewModel confirmationPaneViewModel;

    /**
     * Sets up the test environment before each test.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Registers the primary stage in TestFX.</li>
     *   <li>Loads the ConfirmationPane from FXML and displays it in the stage.</li>
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
            // Load the LoginPage from FXML and display it in the stage

            Config closePropertiesConfig = new Config(ConfirmationPaneController.class.getResource(ConfirmationPaneController.FXML_FILE));
            JFXNode<BorderPane, ConfirmationPaneController> closePropsJfxNode = FXMLMvvmLoader.make(closePropertiesConfig);
            BorderPane confirmationPane = closePropsJfxNode.node();

            Optional<ConfirmationPaneViewModel> confirmationPaneViewModelOpt = closePropsJfxNode.getViewModel(ConfirmationPaneController.VIEW_MODEL_NAME);
            assertTrue(confirmationPaneViewModelOpt.isPresent());
            confirmationPaneViewModel = confirmationPaneViewModelOpt.get();

            Scene scene = new Scene(confirmationPane, 650, 400);
            stage.setScene(scene);
            stage.show();
        });
    }

    /**
     * Cleans up the test environment after each test.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Cleans up TestFX stages and mocked resources.</li>
     * </ul>
     * </p>
     *
     * @throws Exception if an error occurs during cleanup
     */
    @AfterEach
    void cleanup() throws Exception {
        // Clean up TestFX stages and mocked resources
        FxToolkit.cleanupStages();
    }

    /**
     * Verifies that the title and message labels are set from the property values.
     */
    @Test
    public void setConfirmationTextTest() throws Exception {
        final String TITLE = "title";
        final String MESSAGE = "message";

        WaitForAsyncUtils.waitForFxEvents();

        Label titleLabel = robot.lookup(TITLE_LABEL_ID).query();
        Label messageLabel = robot.lookup(MESSAGE_LABEL_ID).query();

        assertNotNull(titleLabel);
        assertNotNull(messageLabel);

        WaitForAsyncUtils.waitForFxEvents();

        Platform.runLater(() -> {
            confirmationPaneViewModel.setPropertyValue(CONFIRMATION_TITLE, TITLE);
            confirmationPaneViewModel.setPropertyValue(CONFIRMATION_MESSAGE, MESSAGE);
        });

        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(TITLE, titleLabel.getText());
        assertEquals(MESSAGE, messageLabel.getText());
    }

    /**
     * Verifies that when the close button is pressed that the CLOSE_CONFIRMATION_PANEL property value
     * is changed to true, which triggers the subscribe() method to be called.
     */
    @Test
    public void pressCloseButtonTest() {
        final AtomicBoolean subscribeCalled = new AtomicBoolean(false);

        Button closeButton = robot.lookup(CLOSE_BUTTON_ID).queryButton();
        assertNotNull(closeButton);


        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(closeButton.isDisabled());

        BooleanProperty closeConfPaneProp = confirmationPaneViewModel.getBooleanProperty(CLOSE_CONFIRMATION_PANEL);
        closeConfPaneProp.subscribe(closeIt -> {
            if (closeIt) {
                subscribeCalled.set(true);
            }
        });

        robot.clickOn(closeButton);

        assertTrue(subscribeCalled.get());
    }

}
