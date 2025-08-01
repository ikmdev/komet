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

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.komet.kview.mvvm.model.PatternField;

public class PatternFieldsPanelEvent extends Evt {

    public static final EvtType<PatternFieldsPanelEvent> PATTERN_FIELDS = new EvtType<>(Evt.ANY, "PATTERN_FIELDS");

    public static final EvtType<PatternFieldsPanelEvent> EDIT_FIELD = new EvtType<>(PATTERN_FIELDS, "EDIT_FIELD");

    public static final EvtType<PatternFieldsPanelEvent> ADD_FIELD = new EvtType<>(PATTERN_FIELDS, "ADD_FIELD");

    private final PatternField patternField;
    private final PatternField previousPatternField;
    private final int currentFieldOrder;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public PatternFieldsPanelEvent(Object source, EvtType eventType, PatternField patternField, PatternField previousPatternField, int currentFieldOrder) {
        super(source, eventType);
        this.patternField = patternField;
        this.previousPatternField = previousPatternField;
        this.currentFieldOrder = currentFieldOrder;
    }

    public PatternField getPatternField() {
        return patternField;
    }

    public PatternField getPreviousPatternField(){
        return previousPatternField;
    }

    public int getCurrentFieldOrder(){
        return currentFieldOrder;
    }

}
