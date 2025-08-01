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

import java.util.UUID;

/**
 * An event that represents a request to delete a journal.
 * <p>
 * This class extends the base {@link Evt} class and provides specific functionality
 * for handling journal deletion events within the application. The event carries
 * information about which journal topic should be deleted.
 *
 * @see dev.ikm.tinkar.events.Evt
 * @see dev.ikm.tinkar.events.EvtType
 */
public class DeleteJournalEvent extends Evt {

    /**
     * The event type constant for journal deletion events.
     * This type can be used to register event handlers specifically for journal deletion.
     */
    public static final EvtType<DeleteJournalEvent> DELETE_JOURNAL = new EvtType<>(Evt.ANY, "DELETE");

    /**
     * The topic identifier of the journal to be deleted.
     */
    private UUID journalTopic;

    /**
     * Constructs a new delete journal event.
     *
     * @param source       the object that triggered the event
     * @param evtType      the type of the event
     * @param journalTopic the topic identifier of the journal to be deleted
     */
    public DeleteJournalEvent(Object source, EvtType<? extends Evt> evtType, UUID journalTopic) {
        super(source, evtType);
        this.journalTopic = journalTopic;
    }

    /**
     * Returns the topic identifier of the journal to be deleted.
     *
     * @return the journal topic identifier
     */
    public UUID getJournalTopic() {
        return journalTopic;
    }

    /**
     * Sets the topic identifier of the journal to be deleted.
     *
     * @param journalTopic the journal topic identifier to set
     */
    public void setJournalTopic(UUID journalTopic) {
        this.journalTopic = journalTopic;
    }
}