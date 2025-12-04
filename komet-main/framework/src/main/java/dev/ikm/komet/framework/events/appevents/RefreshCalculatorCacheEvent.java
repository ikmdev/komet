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
package dev.ikm.komet.framework.events.appevents;

import dev.ikm.tinkar.events.Evt;
import dev.ikm.tinkar.events.EvtType;

/**
 * Event representing refreshing view calculator cache.
 *
 * @see Evt
 * @see EvtType
 */
public class RefreshCalculatorCacheEvent extends Evt {

    public static final EvtType<RefreshCalculatorCacheEvent> REFRESH_CACHE = new EvtType<>(Evt.ANY, "REFRESH_CACHE");
    public static final EvtType<RefreshCalculatorCacheEvent> GLOBAL_REFRESH = new EvtType<>(REFRESH_CACHE, "GLOBAL_REFRESH");

    /**
     * Constructs a new {@code RefreshCalculatorCacheEvent}.
     *
     * @param source  The source object that generated the event.
     * @param evtType The type of the event, typically {@link #GLOBAL_REFRESH}.
     */
    public RefreshCalculatorCacheEvent(Object source, EvtType<? extends Evt> evtType) {
        super(source, evtType);
    }

}