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

import org.carlfx.axonic.StateMachine;

import java.util.UUID;

import static dev.ikm.komet.kview.mvvm.viewmodel.PatternFieldsViewModel.TOTAL_EXISTING_FIELDS;
import static dev.ikm.komet.kview.mvvm.viewmodel.PatternViewModel.PATTERN_TOPIC;

public class PatternPropertiesViewModel extends FormViewModel {

    public static String STATE_MACHINE = "stateMachine";


    public PatternPropertiesViewModel() {
        super();
            addProperty(PATTERN_TOPIC, (UUID) null)
                    .addProperty(STATE_MACHINE, (StateMachine) null)
                    .addProperty(TOTAL_EXISTING_FIELDS, 0)
        ;
    }


}
