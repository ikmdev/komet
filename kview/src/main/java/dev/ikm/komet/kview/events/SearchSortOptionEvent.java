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

public class SearchSortOptionEvent extends Evt {

    public static final EvtType<SearchSortOptionEvent> SORT_BY_COMPONENT = new EvtType<>(Evt.ANY, "SORT_BY_COMPONENT");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_COMPONENT_ALPHA = new EvtType<>(Evt.ANY, "SORT_BY_COMPONENT_ALPHA");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_SEMANTIC = new EvtType<>(Evt.ANY, "SORT_BY_SEMANTIC");

    public static final EvtType<SearchSortOptionEvent> SORT_BY_SEMANTIC_ALPHA = new EvtType<>(Evt.ANY, "SORT_BY_SEMANTIC_ALPHA");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public SearchSortOptionEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
