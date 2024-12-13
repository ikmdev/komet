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
package dev.ikm.komet.kview.events.genediting;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import dev.ikm.tinkar.terms.EntityFacade;

public class PropertyPanelEvent extends Evt {

    public static final EvtType<PropertyPanelEvent> OPEN_PANEL = new EvtType<>(Evt.ANY, "OPEN_PANEL");

    public static final EvtType<PropertyPanelEvent> CLOSE_PANEL = new EvtType<>(Evt.ANY, "CLOSE_PANEL");

    /* EVENT */
    public static final EvtType<PropertyPanelEvent> SHOW_PANEL = new EvtType<>(Evt.ANY, "SHOW_PANEL");
    public static final EvtType<PropertyPanelEvent> SHOW_EDIT_SEMANTIC_FIELDS = new EvtType<>(SHOW_PANEL, "SHOW_EDIT_SEMANTIC_FIELDS");
    public static final EvtType<PropertyPanelEvent> CONFIRMATION_PANEL = new EvtType<>(SHOW_PANEL, "CONFIRMATION_PANEL");

    /*** private variables ***/
    private EntityFacade semantic;
    /**
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     type of the event
     */
    public PropertyPanelEvent(Object source, EvtType<PropertyPanelEvent> eventType) {
        super(source, eventType);
    }

    /**
     *
     * @param source
     * @param eventType
     */
    public PropertyPanelEvent(Object source, EvtType<PropertyPanelEvent> eventType, EntityFacade semantic) {
        super(source, eventType);
        this.semantic = semantic;
    }
}
