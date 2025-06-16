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
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.carlfx.cognitive.loader.FXMLMvvmLoader;
import org.carlfx.cognitive.loader.JFXNode;

import java.util.concurrent.CompletableFuture;

import static dev.ikm.komet.kview.fxutils.FXUtils.runOnFxThread;

/**
 * Controller class for managing GitHub information view in the changeset exchange functionality.
 */
public class ConfirmClearDialogController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    /**
     * Creates the dialog, providing the CompletableFuture that provides the modal behavior of the dialog.
     * @param parentNode The node to start at to determine the topmost Pane, which is required for the GlassPane
     * @return CompletableFuture with a Boolean type, Boolean value of true is returned when the confirm button is pressed
     */
    public static CompletableFuture<Boolean> showConfirmClearDialog(Node parentNode) {
        // Create a CompletableFuture that will be completed when the user makes a choice
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Show dialog on JavaFX thread
        runOnFxThread(() -> {
            GlassPane glassPane = new GlassPane(FXUtils.getTopmostPane(parentNode));

            final JFXNode<Pane, ConfirmClearDialogController> githubInfoNode = FXMLMvvmLoader
                    .make(ConfirmClearDialogController.class.getResource("confirm-clear-dialog.fxml"));
            final Pane dialogPane = githubInfoNode.node();
            final ConfirmClearDialogController controller = githubInfoNode.controller();

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