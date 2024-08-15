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
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

import java.util.UUID;

public class FormViewModel extends ValidationViewModel {
    public static String CURRENT_JOURNAL_WINDOW_TOPIC = "CURRENT_JOURNAL_WINDOW_TOPIC";
    public static String CONCEPT_TOPIC = "conceptTopic";
    public static String VIEW_PROPERTIES = "viewProperties";
    public static String MODE = "mode";

    // Create or Edit mode value
    public static String CREATE = "CREATE";
    public static String EDIT = "EDIT";
    public static String VIEW = "VIEW";
    public enum FormMode {
        CREATE,
        EDIT,
        VIEW
    }
    public FormViewModel() {
        // Default Form View
        addProperty(MODE, EDIT);

        addProperty(CURRENT_JOURNAL_WINDOW_TOPIC, (UUID) null); // events within the current journal window's scope
        addProperty(CONCEPT_TOPIC, (UUID) null);                // events within the concept window's scope
        addProperty(VIEW_PROPERTIES, (ViewProperties) null);
    }
    public FormMode getMode() {
        return FormMode.valueOf(getPropertyValue(MODE));
    }
}
