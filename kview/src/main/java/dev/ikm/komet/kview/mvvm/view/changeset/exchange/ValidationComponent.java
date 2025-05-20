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

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A record class representing a validation component.
 * <p>
 * This class encapsulates a label for displaying error messages and a VBox container
 * for layout purposes. It provides methods to show and clear error messages.
 *
 * @param errorLabel    The label used to display error messages
 * @param containerVBox The VBox container for layout
 */
public record ValidationComponent(Label errorLabel, VBox containerVBox) {

    /**
     * Creates a new validation component with the specified container.
     * <p>
     * This constructor initializes a validation component with a new error label
     * and the provided container VBox.
     *
     * @param containerVBox The VBox container that will hold the error label
     */
    ValidationComponent(VBox containerVBox) {
        this(createErrorLabel(), containerVBox);
    }

    /**
     * Creates a label for displaying error messages.
     *
     * @return A configured error label
     */
    private static Label createErrorLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setTextFill(Color.RED);
        return label;
    }

    /**
     * Shows an error message in the validation component.
     * <p>
     * This method displays the specified error message in the error label and adds
     * the label to the container VBox if it's not already present.
     *
     * @param errorMessage The error message to display
     */
    public void showError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            errorLabel.setText(errorMessage);

            // Add the error label to the container if not already there
            if (!containerVBox.getChildren().contains(errorLabel)) {
                containerVBox.getChildren().add(errorLabel);
            }
        }
    }

    /**
     * Clears any displayed error message.
     * <p>
     * This method removes the error label from the container VBox,
     * effectively hiding any previously displayed error message.
     */
    void clearError() {
        containerVBox.getChildren().remove(errorLabel);
    }
}
