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

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.tinkar.common.id.PublicId;

public class EditOtherNameConceptEvent extends Evt {

    public static final EvtType<EditOtherNameConceptEvent> EDIT_OTHER_NAME = new EvtType<>(Evt.ANY, "EDIT_OTHER_NAME");

    private final PublicId publicId;

    private final DescrName descrName;

    /**
     * Constructs EditOtherNameConceptEvent with needed PublicId payload
     * @param source    source of the event
     * @param eventType     type of the event
     * @param publicId  payload needed to for the Edit Other Name Form
     */
    public EditOtherNameConceptEvent(Object source, EvtType eventType, PublicId publicId) {
        super(source, eventType);
        this.publicId = publicId;
        this.descrName = null;
    }

    /**
     * Constructs EditOtherNameConceptEvent with needed PublicId payload
     * @param source    source of the event
     * @param eventType     type of the event
     * @param descrName the model object.
     */
    public EditOtherNameConceptEvent(Object source, EvtType eventType, DescrName descrName) {
        super(source, eventType);
        this.publicId = null;
        this.descrName =descrName;
    }

    public PublicId getPublicId() {
        return publicId;
    }

    public DescrName getDescrName() {
        return descrName;
    }
}
