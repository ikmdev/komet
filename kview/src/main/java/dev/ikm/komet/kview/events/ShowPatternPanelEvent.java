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

public class ShowPatternPanelEvent extends Evt {

    public static final EvtType<ShowPatternPanelEvent> SHOW_ADD_DEFINITION = new EvtType<>(Evt.ANY, "SHOW_ADD_DEFINITION");

    public static final EvtType<ShowPatternPanelEvent> SHOW_EDIT_FIELDS = new EvtType<>(Evt.ANY, "SHOW_EDIT_FIELDS");

    //TODO future: other EvtTypes like show History, show Timeline etc

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public ShowPatternPanelEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
