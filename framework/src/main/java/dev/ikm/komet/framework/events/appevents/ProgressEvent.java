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

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;
import javafx.concurrent.Task;

/**
 * For event publishers and subscribers to summon progress popup panel, add and remove a panel from a list of running progress panels.
 */
public class ProgressEvent extends Evt  {

    public static final EvtType<ProgressEvent> SUMMON = new EvtType<>(Evt.ANY, "SUMMON");
    public static final EvtType<ProgressEvent> CANCEL = new EvtType<>(Evt.ANY, "CANCEL");
    private Task task;
    private String cancelButtonText;

    /**
     * Constructs a prototypical Event.
     *
     * @param source        the object on which the Event initially occurred
     * @param eventType     the event type
     * @param task         the event payload: a javafx Task.
     */
    public <V> ProgressEvent(Object source, EvtType eventType, Task<V> task) {
        super(source, eventType);
        this.task = task;
    }
    public <V> ProgressEvent(Object source, EvtType eventType, Task<V> task, String cancelButtonText) {
        super(source, eventType);
        this.task = task;
        this.cancelButtonText = cancelButtonText;
    }

    public <V> Task<V> getTask() {
        return task;
    }

    public <V> void setTask(Task<V> task) {
        this.task = task;
    }

    public String getCancelButtonText() {
        if (cancelButtonText == null) {
            return "Cancel";
        }
        return cancelButtonText;
    }
}