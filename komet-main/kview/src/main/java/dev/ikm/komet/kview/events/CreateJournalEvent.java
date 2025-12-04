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
import dev.ikm.komet.framework.preferences.PrefX;

public class CreateJournalEvent extends Evt {

    public static final EvtType<CreateJournalEvent> CREATE_JOURNAL = new EvtType<>(Evt.ANY, "CREATE");

    private PrefX journalWindowSettingsObjectMap;

    public CreateJournalEvent(Object source, EvtType<? extends Evt> evtType, PrefX journalWindowSettingsObjectMap) {
        super(source, evtType);
        this.journalWindowSettingsObjectMap = journalWindowSettingsObjectMap;
    }

    public PrefX getWindowSettingsObjectMap() {
        return journalWindowSettingsObjectMap;
    }

    public void setWindowSettingsObjectMap(PrefX journalWindowSettingsObjectMap) {
        this.journalWindowSettingsObjectMap = journalWindowSettingsObjectMap;
    }
}
