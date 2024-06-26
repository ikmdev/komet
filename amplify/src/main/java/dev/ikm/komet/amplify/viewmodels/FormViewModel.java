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
package dev.ikm.komet.amplify.viewmodels;

import dev.ikm.komet.framework.view.ViewProperties;
import org.carlfx.cognitive.viewmodel.ValidationViewModel;

import java.util.UUID;

public class FormViewModel extends ValidationViewModel {
    public static String CONCEPT_TOPIC = "conceptTopic";
    public static String VIEW_PROPERTIES = "viewProperties";
    public static String MODE = "mode";

    // Create or Edit mode value
    public static String CREATE = "CREATE";
    public static String EDIT = "EDIT";
    public static String VIEW = "VIEW";

    public FormViewModel() {
        // Default Form View
        addProperty(MODE, EDIT);

        addProperty(CONCEPT_TOPIC, (UUID) null);
        addProperty(VIEW_PROPERTIES, (ViewProperties) null);
    }
}
