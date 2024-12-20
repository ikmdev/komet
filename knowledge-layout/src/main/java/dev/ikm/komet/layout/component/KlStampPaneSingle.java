package dev.ikm.komet.layout.component;

import dev.ikm.komet.framework.observable.ObservableStamp;
import dev.ikm.komet.framework.observable.ObservableStampVersion;
import dev.ikm.tinkar.entity.StampVersionRecord;

/**
 * Represents a single pane component in the layout system that deals with
 * a specific observable stamp and its version.
 */
public interface KlStampPaneSingle extends KlComponentPaneSingle<ObservableStampVersion, StampVersionRecord> {
    ObservableStamp observableEntity();
}

