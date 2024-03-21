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
package dev.ikm.komet.amplify.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.tinkar.common.id.PublicId;

public class AddOtherNameToConceptEvent extends Evt {

    public static final EvtType<AddOtherNameToConceptEvent> ADD_DESCRIPTION = new EvtType<>(Evt.ANY, "ADD_DESCRIPTION");

    private PublicId publicId;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    source of the event
     * @param eventType     type of the event
     * @param publicId  payload needed to for the Edit Other Name Form
     */
    public AddOtherNameToConceptEvent(Object source, EvtType eventType, PublicId publicId) {
        super(source, eventType);
        this.publicId = publicId;
    }

    /**
     * Constructs a prototypical Event.
     *
     * @param source    source of the event
     * @param eventType     type of the event
     */
    public AddOtherNameToConceptEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }

    public PublicId getPublicId() {
        return publicId;
    }

    public void setPublicId(PublicId publicId) {
        this.publicId = publicId;
    }
}