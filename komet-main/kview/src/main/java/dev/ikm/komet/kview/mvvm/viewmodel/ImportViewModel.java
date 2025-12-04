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

import dev.ikm.komet.framework.view.ViewProperties;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;

import java.io.File;

import static dev.ikm.tinkar.events.FrameworkTopics.PROGRESS_TOPIC;
import static dev.ikm.komet.kview.mvvm.viewmodel.ImportViewModel.ImportField.*;

/**
 * The {@code ImportViewModel} class is responsible for handling the import functionality
 * within the application. It extends {@link FormViewModel} to leverage form-based interactions
 * and validation mechanisms.
 *
 * <p>This ViewModel manages the selection and validation of a changeset file to be imported.
 * It ensures that the selected file exists and is readable before proceeding with the import process.</p>
 */
public class ImportViewModel extends FormViewModel {

    /**
     * The {@code ImportField} enum defines the fields used within the {@code ImportViewModel}.
     * Each field represents a specific input or property required for the import functionality.
     */
    public enum ImportField {
        /**
         * Represents the file selected by the user for import.
         */
        SELECTED_FILE("Selected File"),

        /**
         * Represents the PROGRESS_TOPIC or LANDING_PAGE_TOPIC
         */
        DESTINATION_TOPIC("Destination Topic");

        /**
         * The display name of the import field.
         */
        public final String name;

        /**
         * Constructs an {@code ImportField} with a specified display name.
         *
         * @param name the display name of the field
         */
        ImportField(String name) {
            this.name = name;
        }

        /**
         * Constructs an {@code ImportField} using the enum constant's name as the display name.
         */
        ImportField() {
            this.name = name();
        }
    }

    /**
     * Constructs a new {@code ImportViewModel} instance.
     * Initializes the necessary properties and validators for the import form.
     */
    public ImportViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null);

        addProperty(SELECTED_FILE, (File) null)
                .addValidator(SELECTED_FILE, SELECTED_FILE.name, (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel vm) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(SELECTED_FILE));
                    } else {
                        final File file = (File) prop.get();
                        if (!file.exists()) {
                            validationResult.error("${%s} does not exist".formatted(SELECTED_FILE));
                        }

                        if (!file.canRead()) {
                            validationResult.error("${%s} is not readable".formatted(SELECTED_FILE));
                        }
                    }
                });
        // Default value for DESTINATION_TOPIC would be PROGRESS_TOPIC.
        // Progress popUp (PROGRESS_TOPIC) will be shown in the Journal windows by default.
        // but When we import the changeSet from the landing page, Progress popup (LANDING_PAGE_TOPIC) will be only shown in the landing page.
        addProperty(DESTINATION_TOPIC, PROGRESS_TOPIC);
    }
}
