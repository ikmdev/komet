package dev.ikm.komet.layout.event;

import java.util.UUID;

public interface KlEvent {
    /**
     * Retrieves the unique identifier for the KlEvent.
     *
     * @return the UUID of the event.
     */
    UUID klEventUuid();

}
