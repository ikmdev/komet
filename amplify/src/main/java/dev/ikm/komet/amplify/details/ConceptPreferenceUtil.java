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
package dev.ikm.komet.amplify.details;

import dev.ikm.komet.preferences.NidTextEnum;
import java.util.List;

public class ConceptPreferenceUtil {

    public static ConceptPreference findElement(NidTextEnum nidTextEnum, Integer nid, List<ConceptPreference> conceptPreferenceList){
        ConceptPreference element = null;
        for (ConceptPreference conceptPreference : conceptPreferenceList) {
            if (conceptPreference.getNidType().equals(nidTextEnum) && conceptPreference.getNid().equals(nid)) {
                element = conceptPreference;
                break;
            }
        }
        return element;
    }
}