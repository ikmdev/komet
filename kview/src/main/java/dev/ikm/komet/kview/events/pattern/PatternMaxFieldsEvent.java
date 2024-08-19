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
package dev.ikm.komet.kview.events.pattern;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class PatternMaxFieldsEvent extends Evt {

    public static final EvtType<PatternMaxFieldsEvent> PATTERN_MAX_FIELDS = new EvtType<>(Evt.ANY, "PATTERN_MAX_FIELDS");

    private int maxFields;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public PatternMaxFieldsEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }

    public int getMaxFields() {
        return maxFields;
    }

    public void setMaxFields(int maxFields) {
        this.maxFields = maxFields;
    }
}
