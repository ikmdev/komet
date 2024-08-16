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

public class PatternPropertyPanelEvent extends Evt {

    public static final EvtType<PatternPropertyPanelEvent> OPEN_PANEL = new EvtType<>(Evt.ANY, "OPEN_PANEL");

    public static final EvtType<PatternPropertyPanelEvent> CLOSE_PANEL = new EvtType<>(Evt.ANY, "CLOSE_PANEL");


    /**
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     type of the event
     */
    public PatternPropertyPanelEvent(Object source, EvtType<PatternPropertyPanelEvent> eventType) {
        super(source, eventType);
    }
}
