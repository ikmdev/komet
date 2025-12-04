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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for managing GitHub information view in the changeset exchange functionality.
 */
public class GitHubInfoController implements Initializable {

    @FXML
    private TextField gitUrlTextField;

    @FXML
    private TextField gitEmailTextField;

    @FXML
    private TextField gitUsernameTextField;

    @FXML
    private TextArea statusTextArea;

    @FXML
    private Button closeButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization logic here
    }

    /**
     * Gets the text field containing the Git repository URL.
     *
     * @return The text field for the Git repository URL.
     */
    public TextField getGitUrlTextField() {
        return gitUrlTextField;
    }

    /**
     * Gets the text field containing the Git user email.
     *
     * @return The text field for the Git user email.
     */
    public TextField getGitEmailTextField() {
        return gitEmailTextField;
    }

    /**
     * Gets the text field containing the Git username.
     *
     * @return The text field for the Git username.
     */
    public TextField getGitUsernameTextField() {
        return gitUsernameTextField;
    }

    /**
     * Gets the text area used for displaying status messages.
     *
     * @return The text area for status messages and operation results.
     */
    public TextArea getStatusTextArea() {
        return statusTextArea;
    }

    /**
     * Gets the button used to close the dialog.
     *
     * @return The close button.
     */
    public Button getCloseButton() {
        return closeButton;
    }
}