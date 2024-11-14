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
import dev.ikm.komet.kview.mvvm.model.DescrName;

public class PatternDescriptionEvent extends Evt {

    public static final EvtType<PatternDescriptionEvent> PATTERN_ADD_FQN = new EvtType<>(Evt.ANY, "PATTERN_ADD_FQN");

    public static final EvtType<PatternDescriptionEvent> PATTERN_ADD_OTHER_NAME = new EvtType<>(Evt.ANY, "PATTERN_ADD_OTHER_NAME");

    public static final EvtType<PatternDescriptionEvent> PATTERN_EDIT_OTHER_NAME = new EvtType<>(Evt.ANY, "PATTERN_ADD_OTHER_NAME");

    private DescrName descrName;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public PatternDescriptionEvent(Object source, EvtType eventType, DescrName descrName) {
        super(source, eventType);
        this.descrName = descrName;
    }

    public DescrName getDescrName() {
        return descrName;
    }

    public void setDescrName(DescrName descrName) {
        this.descrName = descrName;
    }
}
