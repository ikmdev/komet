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
package dev.ikm.komet.kview.mvvm.viewmodel;

import javafx.concurrent.Task;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

/**
 * View model for tracking the progress of a {@link Task} and related UI properties.
 * It extends {@link ValidationViewModel} to leverage property management, making it
 * easier to bind UI elements in a JavaFX application to these properties.</p>
 * <p>
 * This class defines several property keys that represent the state of a
 * long-running operation, such as whether the operation can be canceled, the text
 * for a cancel button, and whether the progress or close buttons are shown.</p>
 *
 * @see ValidationViewModel
 * @see Task
 */
public class ProgressViewModel extends ValidationViewModel {

    /**
     * Property key for the {@link Task} associated with this progress model.
     * The property value is expected to be a {@link Task};.
     */
    public static String TASK_PROPERTY = "taskProperty";

    /**
     * Property key indicating whether the task has been canceled.
     * The property value is expected to be a {@code boolean}.
     */
    public static String IS_CANCELLED_PROP = "cancelState";

    /**
     * Property key for the text displayed on a button used to cancel the task.
     * The property value is expected to be a {@code String}.
     */
    public static String CANCEL_BUTTON_TEXT_PROP = "cancelButtonText";

    /**
     * Property key indicating whether to show the cancel button in the UI.
     * The property value is expected to be a {@code boolean}.
     */
    public static String SHOW_CANCEL_BUTTON_PROP = "showCancelButton";

    /**
     * Property key indicating whether to show the close button in the UI.
     * The property value is expected to be a {@code boolean}.
     */
    public static String SHOW_CLOSE_BUTTON_PROP = "showCloseButton";

    /**
     * Constructs a new {@code ProgressViewModel} with default property values.
     */
    public ProgressViewModel() {
        super();
        addProperty(TASK_PROPERTY, (Task<Void>) null);
        addProperty(IS_CANCELLED_PROP, false);
        addProperty(CANCEL_BUTTON_TEXT_PROP, "Cancel");
        addProperty(SHOW_CANCEL_BUTTON_PROP, true);
        addProperty(SHOW_CLOSE_BUTTON_PROP, false);
    }
}
