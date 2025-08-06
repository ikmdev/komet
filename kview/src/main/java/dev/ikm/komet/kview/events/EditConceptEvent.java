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

import dev.ikm.komet.kview.mvvm.model.DescrName;
import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

public class EditConceptEvent extends Evt  {

    public static final EvtType<EditConceptEvent> ADD_FQN = new EvtType<>(Evt.ANY, "ADD_FQN");
    public static final EvtType<EditConceptEvent> ADD_OTHER_NAME = new EvtType<>(Evt.ANY, "ADD_OTHER_NAME");

    public static final EvtType<EditConceptEvent> EDIT_FQN = new EvtType<>(Evt.ANY, "EDIT_FQN");


    private DescrName model;

    /**
     * Constructs a prototypical Event.
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     the event type
     * @param model         the event payload: a record of type DescrName
     */
    public EditConceptEvent(Object source, EvtType eventType, DescrName model) {
        super(source, eventType);
        this.model = model;
    }

    public DescrName getModel() {
        return model;
    }

    public void setModel(DescrName model) {
        this.model = model;
    }
}
