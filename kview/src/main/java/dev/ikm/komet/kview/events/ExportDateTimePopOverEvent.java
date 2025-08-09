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

public class ExportDateTimePopOverEvent extends Evt {

    public static final EvtType<ExportDateTimePopOverEvent> CANCEL_POP_OVER = new EvtType<>(Evt.ANY, "CANCEL_POP_OVER");
    public static final EvtType<ExportDateTimePopOverEvent> APPLY_POP_OVER = new EvtType<>(Evt.ANY, "APPLY_POP_OVER");

    public static final int FROM_DATE = 1;
    public static final int TO_DATE = 2;
    private long epochMillis;

    private int rangeType;

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public ExportDateTimePopOverEvent(Object source, EvtType eventType, int rangeType) {
        super(source, eventType);
        this.rangeType = rangeType;
    }

    public ExportDateTimePopOverEvent(Object source, EvtType eventType, int rangeType, long epochMillis) {
        super(source, eventType);
        this.rangeType = rangeType;
        this.epochMillis = epochMillis;
    }

    public long getEpochMillis() {
        return epochMillis;
    }

    public int getRangeType() {
        return rangeType;
    }
}
