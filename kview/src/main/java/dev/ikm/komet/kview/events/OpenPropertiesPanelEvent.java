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
package dev.ikm.komet.kview.events;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;
import dev.ikm.tinkar.common.id.PublicId;

public class OpenPropertiesPanelEvent extends Evt {

    public static final EvtType<OpenPropertiesPanelEvent> OPEN_PROPERTIES_PANEL = new EvtType<>(Evt.ANY, "OPEN_PROPERTIES_PANEL");

    private PublicId fqnPublicId;

    private String conceptFQName;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     */
    public OpenPropertiesPanelEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }

    public OpenPropertiesPanelEvent(Object source, EvtType eventType, PublicId fqnPublicId,
                                    String conceptFQName) {
        super(source, eventType);
        this.fqnPublicId = fqnPublicId;
        this.conceptFQName = conceptFQName;
    }


    public PublicId getFqnPublicId() {
        return fqnPublicId;
    }

    public String getConceptFQName() {
        return conceptFQName;
    }
}
