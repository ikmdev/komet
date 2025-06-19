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
package dev.ikm.komet.kview.mvvm.view.genediting;

import dev.ikm.komet.kview.controls.GlassPane;
import dev.ikm.komet.kview.fxutils.FXUtils;
import dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationDialogViewModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.InjectViewModel;
import org.carlfx.cognitive.loader.JFXNode;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationDialogViewModel.ConfirmationPropertyName.CONFIRMATION_DIALOG_MESSAGE;
import static dev.ikm.komet.kview.mvvm.viewmodel.ConfirmationDialogViewModel.ConfirmationPropertyName.CONFIRMATION_DIALOG_TITLE;

/**
 * Controller for the Confirmation dialog
 */
public class ConfirmationDialogController {

    /**
     * The URL to the controller's FXML file
     */
    public static final URL CONFIRMATION_DIALOG_FXML_URL = ConfirmationDialogController.class.getResource("confirmation-dialog.fxml");
    /**
     * The instance variable name for the view model
     */
    public static final String CONFIRMATION_DIALOG_VIEW_MODEL = "viewModel";

    /**
     * The title of the confirmation dialog, which value is set by the view model title property
     */
    @FXML
    private Label title;
    /**
     * The message of the confirmation dialog, which value is set by the view model message property
     */
    @FXML
    private Label message;
    @FXML
    private Button cancelButton;
    @FXML
    private Button confirmButton;

    /**
     * The view model for the controller, which provides the view text for the confirmation dialog
     */
    @InjectViewModel
    private ConfirmationDialogViewModel viewModel;

    /**
     * Bind the title and message to the view model properties
     */
    @FXML
    public void initialize() {
        title.textProperty().bind(viewModel.getProperty(CONFIRMATION_DIALOG_TITLE));
        message.textProperty().bind(viewModel.getProperty(CONFIRMATION_DIALOG_MESSAGE));
    }

    /**
     * Creates the confirmation dialog with a title and message, providing the CompletableFuture that provides
     * the modal behavior of the dialog.
     * @param parentNode The node to start at to determine the topmost Pane, which is required for the GlassPane
     * @param title The title of the confirmation dialog
     * @param message The message to display in the confirmation dialog
     * @return CompletableFuture with a Boolean type, Boolean value of true is returned when the confirm button is pressed
     */
    public static CompletableFuture<Boolean> showConfirmationDialog(Node parentNode, String title, String message) {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(FXUtils.getTopmostPane(parentNode));

            final JFXNode<Pane, ConfirmationDialogController> githubInfoNode = FXMLMvvmLoader
                    .make(CONFIRMATION_DIALOG_FXML_URL);
            final Pane dialogPane = githubInfoNode.node();
            final ConfirmationDialogController controller = githubInfoNode.controller();
            var viewModel = githubInfoNode.getViewModel(CONFIRMATION_DIALOG_VIEW_MODEL).get();

            // set the view model property values
            viewModel.setPropertyValue(CONFIRMATION_DIALOG_TITLE, title);
            viewModel.setPropertyValue(CONFIRMATION_DIALOG_MESSAGE, message);

            controller.confirmButton.setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(true); // Complete with true on close
            });

            controller.cancelButton.setOnAction(_ -> {
                glassPane.removeContent(dialogPane);
                glassPane.hide();
                future.complete(false); // Complete with true on close
            });

            glassPane.addContent(dialogPane);
            glassPane.show();
        });

        return future;
    }

}