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

import dev.ikm.tinkar.events.EvtBusFactory;
import dev.ikm.tinkar.events.Subscriber;
import dev.ikm.komet.framework.events.appevents.ProgressEvent;
import dev.ikm.komet.framework.progress.ProgressHelper;
import dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import org.carlfx.cognitive.loader.InjectViewModel;

import static dev.ikm.tinkar.events.FrameworkTopics.PROGRESS_TOPIC;
import static dev.ikm.komet.framework.events.appevents.ProgressEvent.CANCEL;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.CANCEL_BUTTON_TEXT_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.IS_CANCELLED_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.SHOW_CANCEL_BUTTON_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.SHOW_CLOSE_BUTTON_PROP;
import static dev.ikm.komet.kview.mvvm.viewmodel.ProgressViewModel.TASK_PROPERTY;

/**
 * <p>Controller for the FXML-based progress view. This class manages UI bindings to a
 * {@link ProgressViewModel} and handles user interactions (cancel, close) and event
 * subscriptions related to progress updates.</p>
 *
 * <p>When the bound task's state changes, this controller updates the UI elements to
 * reflect the current status (running, canceled, succeeded, etc.). It also listens for
 * external events indicating progress cancellation, and performs clean-up tasks when
 * the work is completed or canceled.</p>
 *
 * @see ProgressViewModel
 * @see Task
 */
public class ProgressController {

    /**
     * The button that requests cancellation of the current task's progress.
     */
    @FXML
    private Button cancelProgressButton;

    /**
     * The button that closes the progress view after the task is completed or failed.
     */
    @FXML
    private Button closeProgressButton;

    /**
     * Displays the title of the running task.
     */
    @FXML
    private Text titleText;

    /**
     * Displays the message associated with the current state of the task.
     */
    @FXML
    private Text messageText;

    /**
     * Displays the progress of the running task.
     */
    @FXML
    private ProgressBar progressBar;

    /**
     * Displays the percentage of progress for the running task.
     */
    @FXML
    private Text valueText;

    /**
     * Displays the status of the task upon completion (e.g., "completed", "failed").
     */
    @FXML
    private Text statusText;

    /**
     * The view model providing properties and bindings for the progress view.
     */
    @InjectViewModel
    private ProgressViewModel progressViewModel;

    /**
     * Handles cancellation events coming from the event bus.
     */
    private Subscriber<ProgressEvent> cancelPressListener;

    /**
     * Initializes the controller after its root element has been processed. Binds UI components
     * to the properties in the {@link ProgressViewModel} and sets up event listeners for
     * handling task state changes and user interactions.
     */
    @FXML
    private void initialize() {
        Task<Object> task = progressViewModel.getPropertyValue(TASK_PROPERTY);
        cancelProgressButton.setText(progressViewModel.getPropertyValue(CANCEL_BUTTON_TEXT_PROP));

        // Bind UI elements to the task's properties
        titleText.textProperty().bind(task.titleProperty());
        messageText.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        valueText.textProperty().bind(Bindings
                .when(task.progressProperty().lessThan(0))
                .then("0%")
                .otherwise(task.progressProperty().multiply(100).asString("%.0f%%")));

        cancelProgressButton.disableProperty().bind(progressViewModel.getProperty(IS_CANCELLED_PROP));
        cancelProgressButton.visibleProperty().bind(progressViewModel.getProperty(SHOW_CANCEL_BUTTON_PROP));
        closeProgressButton.visibleProperty().bind(progressViewModel.getProperty(SHOW_CLOSE_BUTTON_PROP));

        // if user clicks cancel on another progress indicator close this one.
        cancelPressListener = (progressEvent) -> {
            // if task is the same and a cancel event type then update property (property will trigger actual cancel).
            if (progressEvent.getTask().equals(task) && progressEvent.getEventType().equals(CANCEL)) {
                progressViewModel.setPropertyValue(IS_CANCELLED_PROP, true);
            }
        };
        EvtBusFactory.getDefaultEvtBus().subscribe(PROGRESS_TOPIC, ProgressEvent.class, cancelPressListener);

        // Respond to changes in the task's state
        task.stateProperty().addListener((src, ov, nv) -> {
            switch (nv) {
                case CANCELLED:
                    progressViewModel.setPropertyValue(SHOW_CANCEL_BUTTON_PROP, false);
                    progressViewModel.setPropertyValue(SHOW_CLOSE_BUTTON_PROP, true);
                    cleanup();
                    if (task.getProgress() == -1) {
                        progressBar.setProgress(0);
                        valueText.setText("0%");
                    } else {
                        progressBar.setProgress(task.getProgress());
                        valueText.setText(String.format("%.0f%%", task.getProgress() * 100));
                    }
                    messageText.setText("Task cancelled");
                    break;
                case FAILED:
                    progressViewModel.setPropertyValue(SHOW_CANCEL_BUTTON_PROP, false);
                    progressViewModel.setPropertyValue(SHOW_CLOSE_BUTTON_PROP, true);
                    cleanup();
                    messageText.setText("Task failed");
                    valueText.setText("");
                    statusText.setText("");
                    break;
                case READY, SCHEDULED, RUNNING:
                    statusText.setText("completed");
                    break;
                case SUCCEEDED:
                    progressViewModel.setPropertyValue(SHOW_CANCEL_BUTTON_PROP, false);
                    progressViewModel.setPropertyValue(SHOW_CLOSE_BUTTON_PROP, true);
                    cleanup();
                    break;
            }
        });

        // Cancel the task if the "isCancelled" property changes to true
        BooleanProperty isCancelledProp = progressViewModel.getProperty(IS_CANCELLED_PROP);
        isCancelledProp.addListener((observableValue, oldValue, newValue) -> {
            if (newValue && !task.isCancelled()) {
                task.cancel(true);
            }
        });

        // Set up a handler for the cancel button
        cancelProgressButton.setOnAction(actionEvent -> ProgressHelper.cancel(task));

        // Initialize status text to empty
        statusText.setText("");
    }

    /**
     * Returns the current {@link ProgressViewModel} for this controller.
     *
     * @return the current ProgressViewModel instance
     */
    private ProgressViewModel getProgressViewModel() {
        return progressViewModel;
    }

    /**
     * Returns the button used to close the progress view.
     *
     * @return the close button
     */
    public Button getCloseProgressButton() {
        return closeProgressButton;
    }

    /**
     * Returns the button used to cancel the running task.
     *
     * @return the cancel button
     */
    public Button getCancelProgressButton() {
        return cancelProgressButton;
    }

    /**
     * Cleans up bindings and subscriptions to prevent resource leaks. Unbinds
     * text and progress properties, unregisters event subscribers, and removes
     * any attached action handlers.
     */
    public void cleanup() {
        titleText.textProperty().unbind();
        messageText.textProperty().unbind();
        progressBar.progressProperty().unbind();
        valueText.textProperty().unbind();
        cancelProgressButton.setOnAction(null);
        cancelProgressButton.visibleProperty().unbind();
        cancelProgressButton.disableProperty().unbind();
        EvtBusFactory.getDefaultEvtBus().unsubscribe(PROGRESS_TOPIC, ProgressEvent.class, cancelPressListener);
    }
}
