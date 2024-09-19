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

import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;
import java.util.UUID;

public class PatternPropertiesViewModel extends FormViewModel {

    // if the user is ADDing a Pattern Definition, then this is false (default value)
    // if the user has already added a Pattern Definition by clicking DONE on the Pattern Definition bump out,
    // then this is true
    public static String DISPLAY_DEFINITION_EDIT_MODE = "definitionEditMode";

    // if the user is ADDing a Fully Qualified Name, then this is false (default value)
    // if the user has already added a Fully Qualified Name by clicking DONE on the Fully Qualified Name bump out,
    // then this is true
    public static String DISPLAY_FQN_EDIT_MODE = "fqnEditMode";

    // if the user is ADDing an Other Name, then this is false (default value)
    // if the user has already added an Other Name by clicking DONE on the Other Name bump out,
    // then this is true
    public static String DISPLAY_OTHER_NAME_EDIT_MODE = "otherNameEditMode";

    // if the user is ADDing a Field when there are currently zero fields, then this is false (default value)
    // if the user has already at least one Field, then this is true
    public static String DISPLAY_FIELDS_EDIT_MODE = "fieldsEditMode";

    public PatternPropertiesViewModel() {
        super();
                addProperty(PATTERN_TOPIC, (UUID) null)
                // the following flags allow the system to know where the user is
                // during pattern creation.  If they are adding something then these flags are false
                // if they have already added the information on that part of the pattern then these flags are true
                // NOTE these flags do not convey the state of edit or add, they capture the state of the display
                // buttons in the UI and when they need to say "ADD" or "EDIT"
                .addProperty(DISPLAY_DEFINITION_EDIT_MODE, false)
                .addProperty(DISPLAY_FQN_EDIT_MODE, false)
                .addProperty(DISPLAY_OTHER_NAME_EDIT_MODE, false)
                .addProperty(DISPLAY_FIELDS_EDIT_MODE, false)
        ;
    }

    /**
     * when we definitions haven't been added then display the form chooser field
     * @return true if the above condition is satisfied
     */
    public boolean shouldShowFormChooser() {
        return !(boolean) getPropertyValue(DISPLAY_DEFINITION_EDIT_MODE);
    }

    /**
     * when we are in add mode in all four states except the definitions then show description chooser
     * (e.g. button form for Fully Qualified Name|Other Name)
     * OR
     * we are in edit mode for the definitions and only one of FQN or OT is in edit mode (XOR)
     * and when the bump out is not specified, i.e. an OPEN event
     * @return true if the above condition is satisfied
     */
    public boolean shouldShowDescriptionChooser() {
        return (boolean) getPropertyValue(DISPLAY_DEFINITION_EDIT_MODE)
                && (!(boolean) getPropertyValue(DISPLAY_FQN_EDIT_MODE) && !(boolean) getPropertyValue(DISPLAY_OTHER_NAME_EDIT_MODE))
                || (((boolean) getPropertyValue(DISPLAY_FQN_EDIT_MODE) ^ (boolean) getPropertyValue(DISPLAY_OTHER_NAME_EDIT_MODE)));
    }

    /**
     * when the following display flags are in edit mode Definition|FQN|OT
     * and when the display fields flag is in add mode then show the fields
     * when the bump out is not specified, i.e. an OPEN event
     * @return true if the above condition is satisfied
     */
    public boolean shouldShowFields() {
        return (boolean) getPropertyValue(DISPLAY_DEFINITION_EDIT_MODE)
                && (boolean) getPropertyValue(DISPLAY_FQN_EDIT_MODE)
                && (boolean) getPropertyValue(DISPLAY_OTHER_NAME_EDIT_MODE)
                && !(boolean) getPropertyValue(DISPLAY_FIELDS_EDIT_MODE);
    }


}
