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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import org.carlfx.cognitive.validator.ValidationResult;
import org.carlfx.cognitive.viewmodel.ViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternFieldsViewModel extends FormViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(PatternFieldsViewModel.class);

    public static String TOTAL_EXISTING_FIELDS = "totalExistingFields";

    public static String FIELD_ORDER = "fieldOrder";

    public static String DISPLAY_NAME = "displayName";

    public static String DATA_TYPE = "dataType";

    public static String PURPOSE_ENTITY = "purposeEntity";

    public static String MEANING_ENTITY = "meaningEntity";

    public static String COMMENTS = "comments";

    public static final String IS_INVALID = "isInvalid";

    public PatternFieldsViewModel() {
        super();
        addProperty(VIEW_PROPERTIES, (ViewProperties) null)
                .addProperty(FIELD_ORDER, 0) // default to 1, in create mode they will create the first one
                .addProperty(DISPLAY_NAME, "")
                .addValidator(DISPLAY_NAME, "Display Name", (ReadOnlyStringProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isEmpty().get()) {
                        validationResult.error("${%s} is required".formatted(DISPLAY_NAME));
                    }
                })
                .addProperty(DATA_TYPE, (EntityFacade) null)
                .addValidator(DATA_TYPE, "Data Type", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(DATA_TYPE));
                    }
                })
                .addProperty(PURPOSE_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addValidator(PURPOSE_ENTITY, "Purpose Entity", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(PURPOSE_ENTITY));
                    }
                })
                .addProperty(MEANING_ENTITY, (EntityFacade) null) // this is/will be the 'purpose' concept entity
                .addValidator(MEANING_ENTITY, "Meaning Entity", (ReadOnlyObjectProperty prop, ValidationResult validationResult, ViewModel viewModel) -> {
                    if (prop.isNull().get()) {
                        validationResult.error("${%s} is required".formatted(MEANING_ENTITY));
                    }
                })
                .addProperty(COMMENTS, "")
                .addProperty(IS_INVALID, true)
                .addProperty(TOTAL_EXISTING_FIELDS, 0);
        ;
    }
}
