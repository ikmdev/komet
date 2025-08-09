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
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityFacade;

public class MakeConceptWindowEvent extends Evt {

    public static final EvtType<MakeConceptWindowEvent> OPEN_ENTITY_COMPONENT = new EvtType<>(Evt.ANY, "OPEN_ENTITY_COMPONENT");

    public static final EvtType<MakeConceptWindowEvent> OPEN_CONCEPT_FROM_CONCEPT = new EvtType<>(Evt.ANY, "OPEN_CONCEPT_FROM_CONCEPT");

    private EntityFacade entityFacade;


    /**
     * Constructs a prototypical Event.
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType
     * @param entityFacade
     * @throws IllegalArgumentException if source is null
     */
    public MakeConceptWindowEvent(Object source, EvtType eventType, EntityFacade entityFacade) {
        super(source, eventType);
        this.entityFacade = entityFacade;
    }

    public EntityFacade getEntityFacade() {
        return entityFacade;
    }

}
