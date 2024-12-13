package dev.ikm.komet.layout.event;

import dev.ikm.komet.layout.component.KlComponentPaneSingle;
import dev.ikm.komet.layout.selection.Selection;

/**
 * Represents a request event within the Kl framework. This event carries
 * necessary information including the recipient component that handles
 * the event.
 */
public interface KlRequestEvent extends KlEvent {
    /**
     * Retrieves the recipient of the KlRequestEvent.
     *
     * @return the recipient component associated with this event.
     */
    KlComponentPaneSingle<?,?> recipient();

    /**
     * Retrieves the selection upon which the event's requested action is targeted.
     *
     * @return a Selection instance for the requested event
     */
    Selection requestSelection();
}
