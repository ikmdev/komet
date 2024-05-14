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
package dev.ikm.komet.framework.events;

public class ChangeSetTypeEvent extends Evt {

    public static final EvtType<ChangeSetTypeEvent> CHANGE_NECESSARY_SET = new EvtType<>(Evt.ANY, "CHANGE_NECESSARY_SET");

    public static final EvtType<ChangeSetTypeEvent> CHANGE_SUFFICIENT_SET = new EvtType<>(Evt.ANY, "CHANGE_SUFFICIENT_SET");

    // this is if we don't care what we are changing from or to
    public static final EvtType<ChangeSetTypeEvent> ANY_CHANGE = new EvtType<>(Evt.ANY, "ANY_CHANGE");

    /**
     * Constructs a prototypical Event.
     *
     * @param source    the object on which the Event initially occurred
     * @param eventType
     * @throws IllegalArgumentException if source is null
     */
    public ChangeSetTypeEvent(Object source, EvtType eventType) {
        super(source, eventType);
    }
}
