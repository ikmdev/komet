package dev.ikm.komet.layout.coordinate;

import dev.ikm.komet.framework.view.ObservableLogicCoordinate;
import dev.ikm.tinkar.coordinate.logic.LogicCoordinateRecord;

/**
 * Marker interface for logic coordinate plugin panes. This interface designates that a plugin pane
 * is specifically related to logic coordinates within the system, providing a more specific type
 * of coordinate-related plugin pane.
 *
 * Extends the KlCoordinate interface to provide type safety and specific handling for
 * ObservableLogicCoordinate and LogicCoordinateRecord.
 */
public interface KlLogicCoordinate extends KlCoordinate<ObservableLogicCoordinate, LogicCoordinateRecord> {
}
