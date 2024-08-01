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
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PatternViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternViewModel.class);


    // --------------------------------------------
    // Known properties
    // --------------------------------------------
    public static String STAMP_VIEW_MODEL = "stampViewModel";

    public static String DEFINITION_VIEW_MODEL = "definitionViewModel";

    public PatternViewModel() {
        super();
            addProperty(CONCEPT_TOPIC, (UUID) null)
                    .addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                    .addProperty(STAMP_VIEW_MODEL, (ViewModel) null)
                    .addProperty(DEFINITION_VIEW_MODEL, (ViewModel) null);
    }
}
