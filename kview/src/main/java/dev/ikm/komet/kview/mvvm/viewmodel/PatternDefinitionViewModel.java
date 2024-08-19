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
import dev.ikm.tinkar.terms.EntityFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PatternDefinitionViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternDefinitionViewModel.class);

    public static String PURPOSE_ENTITY = "purposeEntity";

    public static String MEANING_ENTITY = "meaningEntity";

    public PatternDefinitionViewModel() {
        super();
            addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addProperty(MEANING_ENTITY, (EntityFacade) null); // this is/will be the 'purpose' concept entity
    }
}
