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
package dev.ikm.komet.kview.mvvm.view.progress;

import dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.InjectViewModel;

import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.*;

public class ProgressController {

    @FXML
    private Button cancelProgressButton;

    @FXML
    private Button closeProgressButton;

    @FXML
    private Text messageText;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Text statusText;

    @FXML
    private Text titleText;

    @FXML
    private Label valueLabel;

    @InjectViewModel
    private ProgressViewModel progressViewModel;
    @FXML
    private void initialize() {
        Task<Void> task = progressViewModel.getPropertyValue(TASK_PROPERTY);
        cancelProgressButton.setText(progressViewModel.getPropertyValue(CANCEL_BUTTON_TEXT_PROP));

        titleText.textProperty().bind(task.titleProperty());
        messageText.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        cancelProgressButton.disableProperty().bind(getProgressViewModel().getProperty(IS_CANCELLED_PROP));



        BooleanProperty isCancelledProp = progressViewModel.getProperty(IS_CANCELLED_PROP);
        isCancelledProp.addListener((observableValue, aBoolean, t1) -> {
            if (t1 && task != null && !task.isCancelled()) {
                task.cancel();
            }
        });

        cancelProgressButton.setOnAction(actionEvent -> {
            getProgressViewModel().setPropertyValue(IS_CANCELLED_PROP, true);
        });

        valueLabel.setText("");
        statusText.setText("");

    }
    private ProgressViewModel getProgressViewModel() {
        return progressViewModel;
    }

    public Button getCloseProgressButton() {
        return closeProgressButton;
    }

    public Button getCancelProgressButton() {
        return cancelProgressButton;
    }

    public void cleanup() {
        titleText.textProperty().unbind();
        messageText.textProperty().unbind();
        progressBar.progressProperty().unbind();
        cancelProgressButton.setOnAction(null);
    }
}
