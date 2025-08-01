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
package dev.ikm.komet.kview.lidr.events;

import dev.ikm.komet.kview.lidr.mvvm.model.LidrRecord;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class AddResultInterpretationEvent extends Evt {

    public static final EvtType<AddResultInterpretationEvent> ADD_ANALYTE_GROUP = new EvtType<>(Evt.ANY, "ADD_ANALYTE_GROUP");

    private final LidrRecord lidrRecord;
    /**
     * Constructs a prototypical Event.
     *
     * @param source         the object on which the Event initially occurred
     * @param eventType
     */
    public AddResultInterpretationEvent(Object source, EvtType eventType, LidrRecord lidrRecord) {
        super(source, eventType);
        this.lidrRecord = lidrRecord;
    }

    public LidrRecord getLidrRecord() {
        return lidrRecord;
    }
}
