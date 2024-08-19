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

public class PatternFieldsViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsViewModel.class);

    public static String FIELD_ORDER = "fieldOrder";

    public static String DISPLAY_NAME = "displayName";

    public static String DATA_TYPE = "dataType";

    public static String PURPOSE_ENTITY = "purposeEntity";

    public static String MEANING_ENTITY = "meaningEntity";

    public static String COMMENTS = "comments";

    public PatternFieldsViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(FIELD_ORDER, (Integer) 1) // default to 1, in create mode they will create the first one
                .addProperty(DISPLAY_NAME, "")
                .addProperty(DATA_TYPE, (EntityFacade) null)
                .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addProperty(MEANING_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addProperty(COMMENTS, "")
        ;
    }
}
